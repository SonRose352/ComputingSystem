package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size
import kotlin.collections.forEach
import kotlin.collections.set

@Composable
fun InfiniteCanvas(
    scale: Float,
    offset: Offset,
    nodes: List<BoardNode>,
    activeNodeId: String?,
    pinnedNodeId: String?,
    secondPinnedNodeId: String?,
    isPlacingMode: Boolean,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onCanvasTap: (Offset) -> Unit,
    onCanvasDoubleTap: (canvasOffset: Offset, screenOffset: Offset) -> Unit,
    onNodeClick: (String) -> Unit,
    onTextNodeUpdate: (String, String) -> Unit,
    onDrawingNodeUpdate: (String, List<List<Pair<Float, Float>>>) -> Unit,
    onNodeMove: (String, Position) -> Unit,
    onNodeResize: (String, Size) -> Unit,
    onDeleteNode: (String) -> Unit,
    onNodePinned: (String) -> Unit,
    onNodeUnpinned: (String) -> Unit,
    onMergeTrigger: (screenOffset: Offset) -> Unit,
    onCopyNode: (String) -> Unit,
    onShowDrawingToolbar: (String) -> Unit,
    onRecognizeDrawing: (String) -> Unit,
    currentStrokeWidth: Float,
    currentStrokeColor: Color,
    onSplitMathNode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.1f, 5f)
        val newOffset = offset + panChange
        onScaleChange(newScale)
        onOffsetChange(newOffset)
    }

    Box(modifier = modifier) {

        val livePositions = remember {
            mutableStateMapOf<String, Position>()
        }

        LaunchedEffect(
            pinnedNodeId,
            secondPinnedNodeId,
            livePositions[pinnedNodeId],
            livePositions[secondPinnedNodeId]
        ) {

            if (pinnedNodeId == null || secondPinnedNodeId == null) return@LaunchedEffect

            val nodeA = nodes.find { it.id == pinnedNodeId } as? BoardNode.MathNode
            val nodeB = nodes.find { it.id == secondPinnedNodeId } as? BoardNode.MathNode

            if (nodeA == null || nodeB == null) return@LaunchedEffect

            val posA = livePositions[nodeA.id] ?: nodeA.position
            val posB = livePositions[nodeB.id] ?: nodeB.position

            val leftA = posA.x
            val rightA = posA.x + nodeA.size.width
            val topA = posA.y
            val bottomA = posA.y + nodeA.size.height

            val leftB = posB.x
            val rightB = posB.x + nodeB.size.width
            val topB = posB.y
            val bottomB = posB.y + nodeB.size.height

            val horizontalDistance = when {
                rightA < leftB -> leftB - rightA
                rightB < leftA -> leftA - rightB
                else -> 0f
            }

            val verticalDistance = when {
                bottomA < topB -> topB - bottomA
                bottomB < topA -> topA - bottomB
                else -> 0f
            }

            val distance = kotlin.math.sqrt(
                horizontalDistance * horizontalDistance +
                        verticalDistance * verticalDistance
            )

            if (distance < 40f) {

                val centerX = (
                        (posA.x + nodeA.size.width / 2f) +
                                (posB.x + nodeB.size.width / 2f)
                        ) / 2f

                val centerY = (
                        (posA.y + nodeA.size.height / 2f) +
                                (posB.y + nodeB.size.height / 2f)
                        ) / 2f

                val screenX = centerX * scale + offset.x
                val screenY = centerY * scale + offset.y

                onMergeTrigger(
                    Offset(screenX, screenY)
                )
            }
        }
        // Сетка
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isPlacingMode) {
                        Modifier.pointerInput(offset, scale) {
                            detectTapGestures { tapOffset ->
                                val canvasX = (tapOffset.x - offset.x) / scale
                                val canvasY = (tapOffset.y - offset.y) / scale
                                onCanvasTap(Offset(canvasX, canvasY))
                            }
                        }
                    } else {
                        Modifier
                            .transformable(state = transformState)
                            .pointerInput(offset, scale) {
                                detectTapGestures(
                                    onTap = { onCanvasTap(Offset.Zero) },
                                    onDoubleTap = { tapOffset ->
                                        val canvasX = (tapOffset.x - offset.x) / scale
                                        val canvasY = (tapOffset.y - offset.y) / scale
                                        onCanvasDoubleTap(
                                            Offset(canvasX, canvasY),
                                            tapOffset
                                        )
                                    }
                                )
                            }
                    }
                )
        ) {
            translate(offset.x, offset.y) {
                val gridSize = 50f * scale
                val startX = (-offset.x / gridSize).toInt() - 1
                val endX = ((size.width - offset.x) / gridSize).toInt() + 1
                val startY = (-offset.y / gridSize).toInt() - 1
                val endY = ((size.height - offset.y) / gridSize).toInt() + 1
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in startX..endX) {
                    drawLine(gridColor, Offset(i * gridSize, startY * gridSize), Offset(i * gridSize, endY * gridSize), 1f)
                }
                for (i in startY..endY) {
                    drawLine(gridColor, Offset(startX * gridSize, i * gridSize), Offset(endX * gridSize, i * gridSize), 1f)
                }
            }
        }

        // Ноды
        nodes.forEach { node ->
            if (livePositions[node.id] == null) {
                livePositions[node.id] = node.position
            }
            when (node) {
                is BoardNode.TextNode -> TextNodeView(
                    node = node,
                    offset = offset,
                    scale = scale,
                    isActive = node.id == activeNodeId,
                    onTextChange = { onTextNodeUpdate(node.id, it) },
                    onClick = { onNodeClick(node.id) },
                    onMoveFinished = { onNodeMove(node.id, it) },
                    onResizeFinished = { onNodeResize(node.id, it) },
                    onDelete = { onDeleteNode(node.id) },
                    onCopy = { onCopyNode(node.id) },
                )
                is BoardNode.MathNode -> MathNodeView(
                    node = node,
                    offset = offset,
                    scale = scale,
                    isActive = node.id == activeNodeId,
                    isPinned = node.id == pinnedNodeId || node.id == secondPinnedNodeId,
                    onClick = { onNodeClick(node.id) },
                    onMove = { pos ->
                        livePositions[node.id] = pos
                    },
                    onMoveFinished = { onNodeMove(node.id, it) },
                    onResizeFinished = { onNodeResize(node.id, it) },
                    onDelete = { onDeleteNode(node.id) },
                    onPinned = { onNodePinned(node.id) },
                    onUnpinned = { onNodeUnpinned(node.id) },
                    onCopy = { onCopyNode(node.id) },
                    onSplitRequested = { onSplitMathNode(node.id) },
                )
                is BoardNode.DrawingNode -> DrawingNodeView(
                    node = node,
                    offset = offset,
                    scale = scale,
                    isActive = node.id == activeNodeId,
                    onClick = { onNodeClick(node.id) },
                    onMove = { pos -> livePositions[node.id] = pos },
                    onMoveFinished = { onNodeMove(node.id, it) },
                    onResizeFinished = { onNodeResize(node.id, it) },
                    onDelete = { onDeleteNode(node.id) },
                    onCopy = { onCopyNode(node.id) },
                    onStrokesUpdated = { strokes -> onDrawingNodeUpdate(node.id, strokes) },
                    onShowToolbar = { onShowDrawingToolbar(node.id) },
                    onRecognize = { onRecognizeDrawing(node.id) },
                    currentStrokeWidth = currentStrokeWidth,
                    currentStrokeColor = currentStrokeColor,
                )
            }
        }
    }
}