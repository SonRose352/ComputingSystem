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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size
import com.example.computingsystem.presentation.calculator.CalculatorAction
import com.example.computingsystem.presentation.calculator.CalculatorViewModel
import com.example.computingsystem.presentation.components.HistoryDialog
import com.example.computingsystem.presentation.components.MathKeyboard
import kotlin.math.roundToInt

@Composable
fun BoardScreen(
    calculatorViewModel: CalculatorViewModel = hiltViewModel(),
    boardViewModel: BoardViewModel = hiltViewModel()
) {
    val boardState by boardViewModel.uiState.collectAsState()
    val nodes by boardViewModel.nodes.collectAsState()
    val history by calculatorViewModel.history.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    val activeMathNode = remember(boardState.activeNodeId, nodes) {
        nodes.find { it.id == boardState.activeNodeId } as? BoardNode.MathNode
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        InfiniteCanvas(
            scale = boardState.scale,
            offset = boardState.offset,
            nodes = nodes,
            activeNodeId = boardState.activeNodeId,
            pinnedNodeId = boardState.pinnedNodeId,
            secondPinnedNodeId = boardState.secondPinnedNodeId,
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
            onNodeMove = { nodeId, newPos ->
                boardViewModel.onAction(BoardAction.MoveNode(nodeId, newPos))
            },
            onNodeResize = { nodeId, newSize ->
                boardViewModel.onAction(BoardAction.ResizeNode(nodeId, newSize))
            },
            onDeleteNode = { nodeId ->
                boardViewModel.onAction(BoardAction.DeleteNode(nodeId))
            },
            onNodePinned = { nodeId ->
                boardViewModel.onAction(BoardAction.PinNode(nodeId))
            },
            onNodeUnpinned = { nodeId ->
                boardViewModel.onAction(BoardAction.UnpinNode(nodeId))
            },
            onMergeTrigger = { screenOffset ->
                boardViewModel.onAction(BoardAction.TriggerMerge(screenOffset))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (activeMathNode != null) 300.dp else 0.dp)
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
                    onClick = { boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.TEXT)) }
                )
                DropdownMenuItem(
                    text = { Text("Математическое выражение") },
                    onClick = { boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.MATH)) }
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

        // Математическая клавиатура снизу
        if (activeMathNode != null) {
            MathKeyboard(
                tokens = boardState.mathTokens,
                cursorPosition = boardState.mathCursorPosition,
                onInput = { boardViewModel.onAction(BoardAction.MathKeyboardInput(it)) },
                onBackspace = { boardViewModel.onAction(BoardAction.MathKeyboardBackspace) },
                onClear = { boardViewModel.onAction(BoardAction.MathKeyboardClear) },
                onMoveCursorLeft = { boardViewModel.onAction(BoardAction.MathKeyboardMoveCursorLeft) },
                onMoveCursorRight = { boardViewModel.onAction(BoardAction.MathKeyboardMoveCursorRight) },
                onSetCursor = { boardViewModel.onAction(BoardAction.MathKeyboardSetCursor(it)) },
                onCalculate = { boardViewModel.onAction(BoardAction.MathKeyboardCalculate) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(300.dp)
            )
        }

        // Диалог слияния — всплывает над точкой стыка двух нод
        if (boardState.showMergeDialog) {
            MergeDialog(
                valueA = boardState.mergeValueA,
                valueB = boardState.mergeValueB,
                operator = boardState.mergeOperator,
                screenOffset = boardState.mergeDialogOffset,
                onOperatorSelected = { boardViewModel.onAction(BoardAction.SetMergeOperator(it)) },
                onSwap = { boardViewModel.onAction(BoardAction.SwapMergeValues) },
                onConfirm = { boardViewModel.onAction(BoardAction.ConfirmMerge) },
                onDismiss = { boardViewModel.onAction(BoardAction.DismissMerge) }
            )
        }
    }

    if (showHistory) {
        HistoryDialog(
            history = history,
            onDismiss = { showHistory = false },
            onUseExpression = { expr ->
                showHistory = false
                boardViewModel.onAction(
                    BoardAction.PlaceMathNodeFromHistory(
                        expression = expr.input,
                        result = expr.result
                    )
                )
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

// ─── MergeDialog ─────────────────────────────────────────────────────────────
// Всплывающий диалог выбора оператора и порядка операндов.
// Появляется прямо в точке стыка двух пальцев.

@Composable
private fun MergeDialog(
    valueA: String,
    valueB: String,
    operator: String,
    screenOffset: Offset,
    onOperatorSelected: (String) -> Unit,
    onSwap: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current

    Popup(
        offset = IntOffset(
            x = (screenOffset.x - with(density) { 140.dp.toPx() }).roundToInt(),
            y = (screenOffset.y - with(density) { 100.dp.toPx() }).roundToInt()
        ),
        properties = PopupProperties(focusable = true),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Верхняя строка: число1  оператор  число2 ──────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Число A
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = valueA,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Оператор
                    Text(
                        text = " $operator ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Число B
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = valueB,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                HorizontalDivider()

                // ── Строка с операторами + кнопки действий ────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Операторы
                    listOf("+", "-", "×", "÷").forEach { op ->
                        val isSelected = op == operator
                        OutlinedButton(
                            onClick = {
                                onOperatorSelected(op)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent,
                                contentColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = op,
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Кнопка «поменять местами»
                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_swap),
                            contentDescription = "Поменять местами",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Кнопка подтверждения
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── InfiniteCanvas ───────────────────────────────────────────────────────────

@Composable
private fun InfiniteCanvas(
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
    onNodeClick: (String) -> Unit,
    onTextNodeUpdate: (String, String) -> Unit,
    onNodeMove: (String, Position) -> Unit,
    onNodeResize: (String, Size) -> Unit,
    onDeleteNode: (String) -> Unit,
    onNodePinned: (String) -> Unit,
    onNodeUnpinned: (String) -> Unit,
    // Вызывается когда оба пальца зажаты и расстояние между ними достаточно сократилось.
    // screenOffset — точка между двумя пальцами в экранных px.
    onMergeTrigger: (screenOffset: Offset) -> Unit,
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
                                detectTapGestures { onCanvasTap(Offset.Zero) }
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
                    onDelete = { onDeleteNode(node.id) }
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
                )
            }
        }
    }
}

// ─── NodeOptionsMenu ──────────────────────────────────────────────────────────

@Composable
private fun NodeOptionsMenu(
    onDelete: () -> Unit,
    iconSizeDp: androidx.compose.ui.unit.Dp
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(iconSizeDp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Настройки",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = { expanded = false; onDelete() }
            )
        }
    }
}

// ─── TextNodeView ─────────────────────────────────────────────────────────────

@Composable
private fun TextNodeView(
    node: BoardNode.TextNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    onTextChange: (String) -> Unit,
    onClick: () -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember(node.id) { mutableStateOf(node.text) }
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize by remember(node.id) { mutableStateOf(node.size) }
    val density = LocalDensity.current

    val scaledPadding   = (12 * scale).dp
    val scaledFont      = (14 * scale).sp
    val scaledBorder    = (2  * scale).dp
    val scaledHandle    = (24 * scale).dp
    val scaledCorner    = (8  * scale).dp
    val scaledElevation = (4  * scale).dp
    val scaledMenuIcon  = (32 * scale).dp

    LaunchedEffect(node.position) { localPosition = node.position }
    LaunchedEffect(node.size)     { localSize = node.size }
    LaunchedEffect(text) {
        if (text != node.text) {
            kotlinx.coroutines.delay(300)
            onTextChange(text)
        }
    }

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
                .pointerInput(isActive) { detectTapGestures { onClick() } }
                .then(
                    if (isActive) Modifier.pointerInput(scale) {
                        detectDragGestures(onDragEnd = { onMoveFinished(localPosition) }) { change, dragAmount ->
                            change.consume()
                            localPosition = Position(
                                localPosition.x + dragAmount.x / scale,
                                localPosition.y + dragAmount.y / scale
                            )
                        }
                    } else Modifier
                ),
            shape = RoundedCornerShape(scaledCorner),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(scaledElevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    enabled = isActive,
                    textStyle = TextStyle(
                        fontSize = scaledFont,
                        lineHeight = scaledFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = scaledPadding, end = scaledPadding, top = scaledMenuIcon + scaledPadding / 2, bottom = scaledPadding),
                    decorationBox = { innerTextField ->
                        Box {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Введите текст...",
                                    style = TextStyle(
                                        fontSize = scaledFont, lineHeight = scaledFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NodeOptionsMenu(onDelete = onDelete, iconSizeDp = scaledMenuIcon)
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
                        detectDragGestures(onDragEnd = { onResizeFinished(localSize) }) { change, dragAmount ->
                            change.consume()
                            localSize = Size(
                                (localSize.width  + dragAmount.x / scale).coerceAtLeast(100f),
                                (localSize.height + dragAmount.y / scale).coerceAtLeast(50f)
                            )
                        }
                    }
            )
        }
    }
}

// ─── MathNodeView ─────────────────────────────────────────────────────────────

@Composable
private fun MathNodeView(
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
) {
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize     by remember(node.id) { mutableStateOf(node.size) }
    val density = LocalDensity.current

    val scaledPadding       = (12 * scale).dp
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

    // Отслеживаем расстояние между двумя пальцами чтобы поймать pinch-in
    // Храним начальное расстояние в момент второго касания
    var initialDistance by remember { mutableStateOf<Float?>(null) }

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
                    awaitEachGesture {

                        val down = awaitFirstDown()

                        val longPress = awaitLongPressOrCancellation(down.id)

                        // LONG PRESS
                        if (longPress != null) {

                            if (node.result.isNotEmpty() && node.result != "Ошибка") {
                                onPinned()
                            }

                            var pointerId = longPress.id

                            while (true) {

                                val event = awaitPointerEvent()

                                val change = event.changes.find {
                                    it.id == pointerId
                                } ?: continue

                                // Палец отпущен
                                if (!change.pressed) {
                                    onMoveFinished(localPosition)
                                    onUnpinned()
                                    break
                                }

                                // Drag
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
                        }

                        // Обычный tap
                        else {
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
                            end = scaledPadding,
                            top = scaledMenuIcon + scaledPadding / 2,
                            bottom = scaledPadding
                        )
                ) {
                    if (node.expression.isEmpty()) {
                        Text(
                            text = if (isActive) "Используйте клавиатуру снизу..." else "Введите выражение...",
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
                                color = if (node.result == "Ошибка") MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            modifier = Modifier.padding(top = scaledResultSpacing)
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NodeOptionsMenu(onDelete = onDelete, iconSizeDp = scaledMenuIcon)
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