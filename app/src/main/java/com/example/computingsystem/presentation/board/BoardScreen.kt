package com.example.computingsystem.presentation.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size
import com.example.computingsystem.presentation.calculator.CalculatorAction
import com.example.computingsystem.presentation.calculator.CalculatorViewModel
import com.example.computingsystem.presentation.components.HistoryDialog

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
            activeNodeId = boardState.activeNodeId,
            isPlacingMode = boardState.selectedNodeType != null,
            onScaleChange = { boardViewModel.onAction(BoardAction.UpdateScale(it)) },
            onOffsetChange = { boardViewModel.onAction(BoardAction.UpdateOffset(it)) },
            onCanvasTap = { offset ->
                if (boardState.selectedNodeType != null) {
                    boardViewModel.onAction(BoardAction.PlaceNode(offset))
                } else {
                    boardViewModel.onAction(BoardAction.ClearActiveNode)
                }
            },
            onNodeClick = { nodeId ->
                boardViewModel.onAction(BoardAction.SetActiveNode(nodeId))
            },
            onTextNodeUpdate = { nodeId, text ->
                boardViewModel.onAction(BoardAction.UpdateTextNode(nodeId, text))
            },
            onMathNodeUpdate = { nodeId, expr ->
                boardViewModel.onAction(BoardAction.UpdateMathNode(nodeId, expr))
            },
            onNodeMove = { nodeId, newPos ->
                boardViewModel.onAction(BoardAction.MoveNode(nodeId, newPos))
            },
            onNodeResize = { nodeId, newSize ->
                boardViewModel.onAction(BoardAction.ResizeNode(nodeId, newSize))
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

        // Панель настроек активного нода внизу экрана
        if (boardState.activeNodeId != null) {
            NodeSettingsPanel(
                onDelete = {
                    boardViewModel.onAction(BoardAction.DeleteNode(boardState.activeNodeId!!))
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    // Диалог истории
    if (showHistory) {
        HistoryDialog(
            history = history,
            onDismiss = { showHistory = false },
            onUseExpression = { expr ->
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
    activeNodeId: String?,
    isPlacingMode: Boolean,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onCanvasTap: (Offset) -> Unit,
    onNodeClick: (String) -> Unit,
    onTextNodeUpdate: (String, String) -> Unit,
    onMathNodeUpdate: (String, String) -> Unit,
    onNodeMove: (String, Position) -> Unit,
    onNodeResize: (String, Size) -> Unit
) {
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.1f, 5f)
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
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val canvasX = (tapOffset.x - offset.x) / scale
                                val canvasY = (tapOffset.y - offset.y) / scale
                                onCanvasTap(Offset(canvasX, canvasY))
                            }
                        }
                    } else {
                        Modifier
                            .transformable(state = transformState)
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    onCanvasTap(Offset.Zero)
                                }
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
                        offset = offset,
                        scale = scale,
                        isActive = node.id == activeNodeId,

                        onTextChange = {
                            onTextNodeUpdate(node.id, it)
                        },

                        onClick = {
                            onNodeClick(node.id)
                        },

                        onMoveFinished = { newPosition ->
                            onNodeMove(node.id, newPosition)
                        },

                        onResizeFinished = { newSize ->
                            onNodeResize(node.id, newSize)
                        }
                    )
                }

                is BoardNode.MathNode -> {
                    MathNodeView(
                        node = node,

                        offset = offset,

                        scale = scale,

                        isActive = node.id == activeNodeId,

                        onExpressionChange = {
                            onMathNodeUpdate(node.id, it)
                        },

                        onClick = {
                            onNodeClick(node.id)
                        },

                        onMoveFinished = { newPosition ->
                            onNodeMove(node.id, newPosition)
                        },

                        onResizeFinished = { newSize ->
                            onNodeResize(node.id, newSize)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextNodeView(
    node: BoardNode.TextNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    onTextChange: (String) -> Unit,
    onClick: () -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit
) {

    var text by remember(node.id) {
        mutableStateOf(node.text)
    }

    var localPosition by remember(node.id) {
        mutableStateOf(node.position)
    }

    var localSize by remember(node.id) {
        mutableStateOf(node.size)
    }

    val density = LocalDensity.current

    val scaledPadding = (20 * scale).dp
    val scaledFont = (14 * scale).sp
    val scaledBorder = (2 * scale).dp
    val scaledHandle = (24 * scale).dp
    val scaledCorner = (8 * scale).dp
    val scaledElevation = (4 * scale).dp

    LaunchedEffect(node.position) {
        localPosition = node.position
    }

    LaunchedEffect(node.size) {
        localSize = node.size
    }

    LaunchedEffect(text) {

        if (text != node.text) {

            kotlinx.coroutines.delay(300)

            onTextChange(text)
        }
    }

    val localScreenX = with(density) {
        (localPosition.x * scale + offset.x).toDp()
    }

    val localScreenY = with(density) {
        (localPosition.y * scale + offset.y).toDp()
    }

    Box(
        modifier = Modifier
            .offset(localScreenX, localScreenY)

            .width(
                with(density) {
                    (localSize.width * scale).toDp()
                }
            )

            .height(
                with(density) {
                    (localSize.height * scale).toDp()
                }
            )
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize()

                .border(
                    width = if (isActive) scaledBorder else 0.dp,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(scaledCorner)
                )

                .pointerInput(isActive) {

                    detectTapGestures {
                        onClick()
                    }
                }

                .then(

                    if (isActive) {

                        Modifier.pointerInput(scale) {

                            detectDragGestures(

                                onDragEnd = {
                                    onMoveFinished(localPosition)
                                }

                            ) { change, dragAmount ->

                                change.consume()

                                localPosition = Position(
                                    localPosition.x + dragAmount.x / scale,
                                    localPosition.y + dragAmount.y / scale
                                )
                            }
                        }

                    } else Modifier
                ),

            shape = RoundedCornerShape(scaledCorner),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),

            elevation = CardDefaults.cardElevation(scaledElevation)

        ) {

            BasicTextField(

                value = text,

                onValueChange = {
                    text = it
                },

                enabled = isActive,

                textStyle = TextStyle(
                    fontSize = scaledFont,

                    lineHeight = scaledFont,

                    color = MaterialTheme.colorScheme.onSurface,

                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),

                modifier = Modifier
                    .fillMaxSize(),

                decorationBox = { innerTextField ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaledPadding)
                    ) {

                        if (text.isEmpty()) {

                            Text(
                                text = "Введите текст...",

                                style = TextStyle(
                                    fontSize = scaledFont,

                                    lineHeight = scaledFont,

                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }

                        innerTextField()
                    }
                }
            )
        }

        if (isActive) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)

                    .size(scaledHandle)

                    .clip(CircleShape)

                    .background(MaterialTheme.colorScheme.primary)

                    .pointerInput(scale) {

                        detectDragGestures(

                            onDragEnd = {
                                onResizeFinished(localSize)
                            }

                        ) { change, dragAmount ->

                            change.consume()

                            localSize = Size(
                                width = (
                                        localSize.width +
                                                dragAmount.x / scale
                                        ).coerceAtLeast(100f),

                                height = (
                                        localSize.height +
                                                dragAmount.y / scale
                                        ).coerceAtLeast(50f)
                            )
                        }
                    }
            )
        }
    }
}

@Composable
private fun MathNodeView(
    node: BoardNode.MathNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    onExpressionChange: (String) -> Unit,
    onClick: () -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit
) {

    var expression by remember(node.id) {
        mutableStateOf(node.expression)
    }

    var localPosition by remember(node.id) {
        mutableStateOf(node.position)
    }

    var localSize by remember(node.id) {
        mutableStateOf(node.size)
    }

    val density = LocalDensity.current

    val scaledPadding = (20 * scale).dp
    val scaledFont = (14 * scale).sp
    val scaledResultFont = (16 * scale).sp
    val scaledBorder = (2 * scale).dp
    val scaledHandle = (24 * scale).dp
    val scaledCorner = (8 * scale).dp
    val scaledElevation = (4 * scale).dp
    val scaledResultSpacing = (6 * scale).dp

    LaunchedEffect(node.position) {
        localPosition = node.position
    }

    LaunchedEffect(node.size) {
        localSize = node.size
    }

    LaunchedEffect(expression) {

        if (expression != node.expression) {

            kotlinx.coroutines.delay(300)

            onExpressionChange(expression)
        }
    }

    val localScreenX = with(density) {
        (localPosition.x * scale + offset.x).toDp()
    }

    val localScreenY = with(density) {
        (localPosition.y * scale + offset.y).toDp()
    }

    Box(
        modifier = Modifier
            .offset(localScreenX, localScreenY)

            .width(
                with(density) {
                    (localSize.width * scale).toDp()
                }
            )

            .height(
                with(density) {
                    (localSize.height * scale).toDp()
                }
            )
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize()

                .border(
                    width = if (isActive) scaledBorder else 0.dp,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(scaledCorner)
                )

                .pointerInput(isActive) {

                    detectTapGestures {
                        onClick()
                    }
                }

                .then(

                    if (isActive) {

                        Modifier.pointerInput(scale) {

                            detectDragGestures(

                                onDragEnd = {
                                    onMoveFinished(localPosition)
                                }

                            ) { change, dragAmount ->

                                change.consume()

                                localPosition = Position(
                                    localPosition.x + dragAmount.x / scale,
                                    localPosition.y + dragAmount.y / scale
                                )
                            }
                        }

                    } else Modifier
                ),

            shape = RoundedCornerShape(scaledCorner),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme
                    .colorScheme
                    .primaryContainer
                    .copy(alpha = 0.3f)
            ),

            elevation = CardDefaults.cardElevation(scaledElevation)

        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaledPadding)
            ) {

                BasicTextField(

                    value = expression,

                    onValueChange = {
                        expression = it
                    },

                    enabled = isActive,

                    textStyle = TextStyle(
                        fontSize = scaledFont,

                        lineHeight = scaledFont,

                        color = MaterialTheme.colorScheme.onSurface,

                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),

                    modifier = Modifier.fillMaxWidth(),

                    decorationBox = { innerTextField ->

                        Box {

                            if (expression.isEmpty()) {

                                Text(
                                    text = "Введите выражение...",

                                    style = TextStyle(
                                        fontSize = scaledFont,

                                        lineHeight = scaledFont,

                                        color = MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant,

                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }

                            innerTextField()
                        }
                    }
                )

                if (node.result.isNotEmpty()) {

                    Text(
                        text = "= ${node.result}",

                        style = TextStyle(
                            fontSize = scaledResultFont,

                            lineHeight = scaledResultFont,

                            color = MaterialTheme.colorScheme.primary,

                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),

                        modifier = Modifier.padding(
                            top = scaledResultSpacing
                        )
                    )
                }
            }
        }

        if (isActive) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)

                    .size(scaledHandle)

                    .clip(CircleShape)

                    .background(MaterialTheme.colorScheme.primary)

                    .pointerInput(scale) {

                        detectDragGestures(

                            onDragEnd = {
                                onResizeFinished(localSize)
                            }

                        ) { change, dragAmount ->

                            change.consume()

                            localSize = Size(
                                width = (
                                        localSize.width +
                                                dragAmount.x / scale
                                        ).coerceAtLeast(150f),

                                height = (
                                        localSize.height +
                                                dragAmount.y / scale
                                        ).coerceAtLeast(80f)
                            )
                        }
                    }
            )
        }
    }
}

@Composable
private fun NodeSettingsPanel(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Настройки узла",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}