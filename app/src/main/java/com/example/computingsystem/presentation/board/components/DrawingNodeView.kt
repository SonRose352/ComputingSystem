package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size

data class DrawingStroke(
    val points: List<Pair<Float, Float>>,
    val color: Color,
    val width: Float
)

@Composable
fun DrawingNodeView(
    node: BoardNode.DrawingNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    onClick: () -> Unit,
    onMove: (Position) -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onStrokesUpdated: (List<List<Pair<Float, Float>>>) -> Unit,
    onShowToolbar: () -> Unit,
    currentStrokeWidth: Float,
    currentStrokeColor: Color
) {
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize by remember(node.id) { mutableStateOf(node.size) }

    // Десериализуем штрихи с метаданными
    var localStrokes by remember(node.id) {
        mutableStateOf(deserializeStrokes(node.strokes))
    }
    var currentStroke by remember { mutableStateOf<DrawingStroke?>(null) }

    val density = LocalDensity.current

    val scaledBorder    = (2  * scale).dp
    val scaledHandle    = (24 * scale).dp
    val scaledCorner    = (8  * scale).dp
    val scaledElevation = (4  * scale).dp
    val scaledMenuIcon  = (32 * scale).dp

    LaunchedEffect(node.position) { localPosition = node.position }
    LaunchedEffect(node.size)     { localSize = node.size }
    LaunchedEffect(node.strokes)  { localStrokes = deserializeStrokes(node.strokes) }

    val screenX = with(density) { (localPosition.x * scale + offset.x).toDp() }
    val screenY = with(density) { (localPosition.y * scale + offset.y).toDp() }

    Box(
        modifier = Modifier
            .offset(screenX, screenY)
            .width(with(density)  { (localSize.width  * scale).toDp() })
            .height(with(density) { (localSize.height * scale).toDp() })
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isActive) scaledBorder else 0.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(scaledCorner)
                )
                .pointerInput(node.id, isActive, scale, currentStrokeWidth, currentStrokeColor) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        if (isActive) {
                            // Активная нода: рисуем
                            val points = mutableListOf<Pair<Float, Float>>()
                            points.add(Pair(down.position.x / scale, down.position.y / scale))
                            currentStroke = DrawingStroke(
                                points = points.toList(),
                                color = currentStrokeColor,
                                width = currentStrokeWidth
                            )

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    // Штрих завершён
                                    if (points.size > 1) {
                                        val newStrokes = localStrokes + currentStroke!!
                                        localStrokes = newStrokes
                                        currentStroke = null
                                        onStrokesUpdated(serializeStrokes(newStrokes))
                                    } else {
                                        currentStroke = null
                                    }
                                    break
                                }
                                points.add(Pair(change.position.x / scale, change.position.y / scale))
                                currentStroke = DrawingStroke(
                                    points = points.toList(),
                                    color = currentStrokeColor,
                                    width = currentStrokeWidth
                                )
                                change.consume()
                            }
                        } else {
                            // Неактивная нода: долгое нажатие — перемещение
                            val longPress = awaitLongPressOrCancellation(down.id)
                            if (longPress != null) {
                                var pointerId = longPress.id
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.find { it.id == pointerId } ?: continue
                                    if (!change.pressed) {
                                        onMoveFinished(localPosition)
                                        break
                                    }
                                    val drag = change.position - change.previousPosition
                                    if (drag != Offset.Zero) {
                                        localPosition = Position(
                                            localPosition.x + drag.x / scale,
                                            localPosition.y + drag.y / scale
                                        )
                                        onMove(localPosition)
                                        change.consume()
                                    }
                                }
                            } else {
                                onClick()
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(scaledCorner),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(scaledElevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Холст для рисования
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                ) {
                    // Сохранённые штрихи
                    localStrokes.forEach { stroke ->
                        if (stroke.points.size < 2) return@forEach
                        val path = Path()
                        path.moveTo(stroke.points[0].first * scale, stroke.points[0].second * scale)
                        stroke.points.drop(1).forEach { (x, y) ->
                            path.lineTo(x * scale, y * scale)
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(width = stroke.width * scale)
                        )
                    }

                    // Текущий незавершённый штрих
                    currentStroke?.let { stroke ->
                        if (stroke.points.size >= 2) {
                            val path = Path()
                            path.moveTo(stroke.points[0].first * scale, stroke.points[0].second * scale)
                            stroke.points.drop(1).forEach { (x, y) ->
                                path.lineTo(x * scale, y * scale)
                            }
                            drawPath(
                                path = path,
                                color = stroke.color,
                                style = Stroke(width = stroke.width * scale)
                            )
                        }
                    }
                }

                // Подсказка
                if (localStrokes.isEmpty() && currentStroke == null) {
                    Text(
                        text = if (isActive) "Рисуйте здесь..." else "Нажмите для рисования",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Меню опций
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NodeOptionsMenu(
                        onCopy = onCopy,
                        onDelete = onDelete,
                        iconSizeDp = scaledMenuIcon
                    )

                    // Кнопка настроек
                    if (isActive) {
                        IconButton(
                            onClick = onShowToolbar,
                            modifier = Modifier.size(scaledMenuIcon)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки рисования",
                                modifier = Modifier.fillMaxSize(0.6f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Ручка изменения размера
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(scaledHandle)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(scale) {
                        detectDragGestures(onDragEnd = { onResizeFinished(localSize) }) { change, dragAmount ->
                            change.consume()
                            localSize = Size(
                                (localSize.width  + dragAmount.x / scale).coerceAtLeast(150f),
                                (localSize.height + dragAmount.y / scale).coerceAtLeast(100f)
                            )
                        }
                    }
            )
        }
    }
}

private fun serializeStrokes(
    strokes: List<DrawingStroke>
): List<List<Pair<Float, Float>>> {

    return strokes.map { stroke ->

        val r = stroke.color.red
        val g = stroke.color.green
        val b = stroke.color.blue

        listOf(
            Pair(-1f, stroke.width),
            Pair(r, g),
            Pair(b, stroke.color.alpha)
        ) + stroke.points
    }
}

private fun deserializeStrokes(
    serialized: List<List<Pair<Float, Float>>>
): List<DrawingStroke> {

    return serialized.mapNotNull { stroke ->

        if (stroke.isEmpty()) return@mapNotNull null

        val first = stroke.first()

        // Старый формат
        if (first.first != -1f) {
            return@mapNotNull DrawingStroke(
                points = stroke,
                color = Color.Black,
                width = 4f
            )
        }

        if (stroke.size < 4) return@mapNotNull null

        val width = stroke[0].second

        val r = stroke[1].first
        val g = stroke[1].second
        val b = stroke[2].first
        val a = stroke[2].second

        DrawingStroke(
            points = stroke.drop(3),
            color = Color(r, g, b, a),
            width = width
        )
    }
}