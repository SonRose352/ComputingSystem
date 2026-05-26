package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size

@Composable
fun MathNodeView(
    node: BoardNode.MathNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onMove: (Position) -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit,
    onDelete: () -> Unit,
    onPinned: () -> Unit,
    onUnpinned: () -> Unit,
    onCopy: () -> Unit,
    onSplitRequested: () -> Unit,
) {
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize     by remember(node.id) { mutableStateOf(node.size) }
    val density = LocalDensity.current

    val scaledPadding       = (14 * scale).dp
    val scaledFont          = (14 * scale).sp
    val scaledResultFont    = (16 * scale).sp
    val scaledBorder        = (2  * scale).dp
    val scaledHandle        = (24 * scale).dp
    val scaledCorner        = (8  * scale).dp
    val scaledElevation     = (4  * scale).dp
    val scaledResultSpacing = (6  * scale).dp
    val scaledMenuIcon      = (32 * scale).dp

    // Цвет контура: обычный active — primary, pinned (режим слияния) — tertiary
    val borderColor = when {
        isPinned -> MaterialTheme.colorScheme.tertiary
        isActive -> MaterialTheme.colorScheme.primary
        else     -> Color.Transparent
    }
    val borderWidth = if (isPinned || isActive) scaledBorder else 0.dp

    LaunchedEffect(node.position) { localPosition = node.position }
    LaunchedEffect(node.size)     { localSize = node.size }

    val screenX = with(density) { (localPosition.x * scale + offset.x).toDp() }
    val screenY = with(density) { (localPosition.y * scale + offset.y).toDp() }

    var initialPinchDistance by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier
            .offset(screenX, screenY)
            .width(with(density)  { (localSize.width  * scale).toDp() })
            .height(with(density) { (localSize.height * scale).toDp() })
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(scaledCorner))
                .pointerInput(node.id, node.result, scale) {
                    // Детектор для pinch-жеста
                    var initialDistance: Float? = null

                    detectTransformGestures { _, pan, zoom, _ ->
                        // Отслеживаем только pinch-out (zoom > 1)
                        if (zoom > 1.2f && initialDistance == null) {
                            onSplitRequested()
                            initialDistance = 0f // чтобы не вызывать повторно
                        }
                    }
                }
                .pointerInput(node.id, node.result, scale) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val longPress = awaitLongPressOrCancellation(down.id)

                        if (longPress != null) {
                            if (node.result.isNotEmpty() && node.result != "Ошибка") {
                                onPinned()
                            }

                            var pointerId = longPress.id

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == pointerId } ?: continue

                                if (!change.pressed) {
                                    onMoveFinished(localPosition)
                                    onUnpinned()
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
                },
            shape = RoundedCornerShape(scaledCorner),
            colors = CardDefaults.cardColors(
                containerColor = if (isPinned)
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(scaledElevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = scaledPadding,
                            end = scaledMenuIcon,
                            top = scaledPadding,
                            bottom = scaledPadding
                        )
                ) {
                    if (node.expression.isEmpty()) {
                        Text(
                            text = if (isActive) stringResource(R.string.math_node_placeholder_active) else stringResource(R.string.math_node_placeholder_inactive),
                            style = TextStyle(
                                fontSize = scaledFont, lineHeight = scaledFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    } else {
                        Text(
                            text = node.expression,
                            style = TextStyle(
                                fontSize = scaledFont, lineHeight = scaledFont,
                                color = MaterialTheme.colorScheme.onSurface,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    if (node.result.isNotEmpty()) {
                        Text(
                            text = "= ${node.result}",
                            style = TextStyle(
                                fontSize = scaledResultFont, lineHeight = scaledResultFont,
                                color = if (node.result == stringResource(R.string.calculator_error)) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            modifier = Modifier.padding(top = scaledResultSpacing)
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NodeOptionsMenu(
                        onCopy = onCopy,
                        onDelete = onDelete,
                        iconSizeDp = scaledMenuIcon
                    )
                }
            }
        }

        if (isActive && !isPinned) {
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
                                (localSize.height + dragAmount.y / scale).coerceAtLeast(80f)
                            )
                        }
                    }
            )
        }
    }
}