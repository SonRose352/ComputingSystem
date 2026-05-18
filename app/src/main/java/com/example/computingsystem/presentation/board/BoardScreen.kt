package com.example.computingsystem.presentation.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.computingsystem.R
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.presentation.calculator.CalculatorAction
import com.example.computingsystem.presentation.calculator.CalculatorViewModel
import com.example.computingsystem.presentation.components.HistoryDialog

//@Composable
//fun BoardScreen(
//    viewModel: CalculatorViewModel = hiltViewModel()
//) {
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    var showHistory by remember { mutableStateOf(false) }
//
//    val history by viewModel.history.collectAsState()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        // Бесконечный Canvas
//        Canvas(
//            modifier = Modifier
//                .fillMaxSize()
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, zoom, _ ->
//                        scale = (scale * zoom).coerceIn(0.1f, 5f)
//                        offset += pan
//                    }
//                }
//        ) {
//            translate(offset.x, offset.y) {
//                // Сетка
//                val gridSize = 50f * scale
//                val startX = (-offset.x / gridSize).toInt() - 1
//                val endX = ((size.width - offset.x) / gridSize).toInt() + 1
//                val startY = (-offset.y / gridSize).toInt() - 1
//                val endY = ((size.height - offset.y) / gridSize).toInt() + 1
//
//                val gridColor = Color.Gray.copy(alpha = 0.2f)
//
//                // Вертикальные линии
//                for (i in startX..endX) {
//                    drawLine(
//                        color = gridColor,
//                        start = Offset(i * gridSize, startY * gridSize),
//                        end = Offset(i * gridSize, endY * gridSize),
//                        strokeWidth = 1f
//                    )
//                }
//
//                // Горизонтальные линии
//                for (i in startY..endY) {
//                    drawLine(
//                        color = gridColor,
//                        start = Offset(startX * gridSize, i * gridSize),
//                        end = Offset(endX * gridSize, i * gridSize),
//                        strokeWidth = 1f
//                    )
//                }
//            }
//        }
//
//        // Кнопка истории в правом верхнем углу
//        Icon(
//            painter = painterResource(R.drawable.ic_history),
//            contentDescription = "История",
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//                .size(36.dp)
//                .clickable { showHistory = true }
//                .padding(6.dp)
//        )
//    }
//
//    // Диалог истории
//    if (showHistory) {
//        HistoryDialog(
//            history = history,
//            onDismiss = { showHistory = false },
//            onUseExpression = { expr ->
//                // Пока ничего не делаем — функционал добавления на доску будет позже
//                showHistory = false
//            },
//            onDeleteExpression = { expr ->
//                viewModel.onAction(CalculatorAction.DeleteFromHistory(expr))
//            },
//            onClearAll = {
//                viewModel.onAction(CalculatorAction.ClearHistory)
//            }
//        )
//    }
//}

@Composable
fun BoardScreen(
    calculatorViewModel: CalculatorViewModel = hiltViewModel(),
    boardViewModel: BoardViewModel = hiltViewModel()
) {
    val boardState by boardViewModel.uiState.collectAsState()
    val nodes by boardViewModel.nodes.collectAsState()
    val history by calculatorViewModel.history.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Бесконечный Canvas с узлами
        InfiniteCanvas(
            scale = boardState.scale,
            offset = boardState.offset,
            nodes = nodes,
            isPlacingMode = boardState.selectedNodeType != null,
            onScaleChange = { boardViewModel.onAction(BoardAction.UpdateScale(it)) },
            onOffsetChange = { boardViewModel.onAction(BoardAction.UpdateOffset(it)) },
            onCanvasTap = { offset ->
                if (boardState.selectedNodeType != null) {
                    boardViewModel.onAction(BoardAction.PlaceNode(offset))
                }
            },
            onTextNodeUpdate = { nodeId, text ->
                boardViewModel.onAction(BoardAction.UpdateTextNode(nodeId, text))
            },
            onMathNodeUpdate = { nodeId, expr ->
                boardViewModel.onAction(BoardAction.UpdateMathNode(nodeId, expr))
            }
        )

        // Кнопка "+" в левом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { boardViewModel.onAction(BoardAction.ToggleAddMenu) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (boardState.selectedNodeType != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить объект",
                    tint = if (boardState.selectedNodeType != null)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Выпадающее меню
            DropdownMenu(
                expanded = boardState.showAddMenu,
                onDismissRequest = { boardViewModel.onAction(BoardAction.ToggleAddMenu) },
                offset = DpOffset(0.dp, 8.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Текстовое поле") },
                    onClick = {
                        boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.TEXT))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Математическое выражение") },
                    onClick = {
                        boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.MATH))
                    }
                )
            }
        }

        // Кнопка истории в правом верхнем углу
        Icon(
            painter = painterResource(R.drawable.ic_history),
            contentDescription = "История",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
                .clickable { showHistory = true }
                .padding(6.dp)
        )
    }

    // Диалог истории
    if (showHistory) {
        HistoryDialog(
            history = history,
            onDismiss = { showHistory = false },
            onUseExpression = { expr ->
                // Пока просто закрываем
                showHistory = false
            },
            onDeleteExpression = { expr ->
                calculatorViewModel.onAction(CalculatorAction.DeleteFromHistory(expr))
            },
            onClearAll = {
                calculatorViewModel.onAction(CalculatorAction.ClearHistory)
            }
        )
    }
}

@Composable
private fun InfiniteCanvas(
    scale: Float,
    offset: Offset,
    nodes: List<BoardNode>,
    isPlacingMode: Boolean,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onCanvasTap: (Offset) -> Unit,
    onTextNodeUpdate: (String, String) -> Unit,
    onMathNodeUpdate: (String, String) -> Unit
) {
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->

        val newScale = (scale * zoomChange)
            .coerceIn(0.1f, 5f)

        val newOffset = offset + panChange

        onScaleChange(newScale)
        onOffsetChange(newOffset)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Сетка с жестами
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isPlacingMode) {
                        // В режиме размещения — только тапы
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val canvasX = (tapOffset.x - offset.x) / scale
                                val canvasY = (tapOffset.y - offset.y) / scale
                                onCanvasTap(Offset(canvasX, canvasY))
                            }
                        }
                    } else {
                        // В обычном режиме — масштабирование и перемещение
                        Modifier.transformable(state = transformState)
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
                    drawLine(
                        color = gridColor,
                        start = Offset(i * gridSize, startY * gridSize),
                        end = Offset(i * gridSize, endY * gridSize),
                        strokeWidth = 1f
                    )
                }

                for (i in startY..endY) {
                    drawLine(
                        color = gridColor,
                        start = Offset(startX * gridSize, i * gridSize),
                        end = Offset(endX * gridSize, i * gridSize),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Узлы поверх сетки
        val density = LocalDensity.current

        nodes.forEach { node ->

            val screenXPx = node.position.x * scale + offset.x
            val screenYPx = node.position.y * scale + offset.y

            val screenX = with(density) { screenXPx.toDp() }
            val screenY = with(density) { screenYPx.toDp() }

            when (node) {
                is BoardNode.TextNode -> {
                    TextNodeView(
                        node = node,
                        screenX = screenX,
                        screenY = screenY,
                        scale = scale,
                        onTextChange = { onTextNodeUpdate(node.id, it) }
                    )
                }

                is BoardNode.MathNode -> {
                    MathNodeView(
                        node = node,
                        screenX = screenX,
                        screenY = screenY,
                        scale = scale,
                        onExpressionChange = { onMathNodeUpdate(node.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextNodeView(
    node: BoardNode.TextNode,
    screenX: Dp,
    screenY: Dp,
    scale: Float,
    onTextChange: (String) -> Unit
) {
    var text by remember(node.id, node.text) {
        mutableStateOf(node.text)
    }

    Card(
        modifier = Modifier
            .offset(x = screenX, y = screenY)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .width(200.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                onTextChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Введите текст...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun MathNodeView(
    node: BoardNode.MathNode,
    screenX: Dp,
    screenY: Dp,
    scale: Float,
    onExpressionChange: (String) -> Unit
) {
    var expression by remember(node.id, node.expression) {
        mutableStateOf(node.expression)
    }

    Card(
        modifier = Modifier
            .offset(x = screenX, y = screenY)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .width(300.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TextField(
                value = expression,
                onValueChange = {
                    expression = it
                    onExpressionChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите выражение...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (node.result.isNotEmpty()) {
                Text(
                    text = "= ${node.result}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
