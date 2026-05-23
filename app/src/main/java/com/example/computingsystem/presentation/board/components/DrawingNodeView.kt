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
    onStrokesUpdated: (List<List<Pair<Float, Float>>>) -> Unit
) {
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize by remember(node.id) { mutableStateOf(node.size) }
    // Локальные штрихи — работаем с ними сразу, сохраняем с задержкой
    var localStrokes by remember(node.id) { mutableStateOf(node.strokes) }
    var currentStroke by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

    val density = LocalDensity.current

    val scaledBorder    = (2  * scale).dp
    val scaledHandle    = (24 * scale).dp
    val scaledCorner    = (8  * scale).dp
    val scaledElevation = (4  * scale).dp
    val scaledMenuIcon  = (32 * scale).dp

    LaunchedEffect(node.position) { localPosition = node.position }
    LaunchedEffect(node.size)     { localSize = node.size }
    LaunchedEffect(node.strokes)  { localStrokes = node.strokes }

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
                .pointerInput(node.id, isActive, scale) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        if (isActive) {
                            // Активная нода: обычный tap/drag — рисуем
                            var stroke = mutableListOf<Pair<Float, Float>>()
                            stroke.add(Pair(down.position.x, down.position.y))
                            currentStroke = stroke.toList()

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    // Штрих завершён — сохраняем
                                    if (stroke.size > 1) {
                                        val newStrokes = localStrokes + listOf(stroke.toList())
                                        localStrokes = newStrokes
                                        currentStroke = emptyList()
                                        onStrokesUpdated(newStrokes)
                                    } else {
                                        currentStroke = emptyList()
                                    }
                                    break
                                }
                                stroke.add(Pair(change.position.x, change.position.y))
                                currentStroke = stroke.toList()
                                change.consume()
                            }
                        } else {
                            // Неактивная нода: долгое нажатие — перемещение, обычный тап — активация
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
                        if (stroke.size < 2) return@forEach
                        val path = Path()
                        path.moveTo(stroke[0].first, stroke[0].second)
                        stroke.drop(1).forEach { (x, y) ->
                            path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF1C1B1F),
                            style = Stroke(width = 4f / scale)
                        )
                    }
                    // Текущий незавершённый штрих
                    if (currentStroke.size >= 2) {
                        val path = Path()
                        path.moveTo(currentStroke[0].first, currentStroke[0].second)
                        currentStroke.drop(1).forEach { (x, y) ->
                            path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF1C1B1F),
                            style = Stroke(width = 4f / scale)
                        )
                    }
                    // Подсказка если нода пустая и неактивная
                }

                // Подсказка поверх канваса
                if (localStrokes.isEmpty() && currentStroke.isEmpty()) {
                    Text(
                        text = if (isActive) "Рисуйте здесь..." else "Нажмите для рисования",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                // Меню опций
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NodeOptionsMenu(
                        onCopy = onCopy,
                        onDelete = onDelete,
                        iconSizeDp = scaledMenuIcon
                    )
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