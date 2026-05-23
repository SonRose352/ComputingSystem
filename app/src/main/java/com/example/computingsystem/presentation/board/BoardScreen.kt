package com.example.computingsystem.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.presentation.board.components.ContextAddMenu
import com.example.computingsystem.presentation.board.components.InfiniteCanvas
import com.example.computingsystem.presentation.board.components.MergeDialog
import com.example.computingsystem.presentation.calculator.CalculatorAction
import com.example.computingsystem.presentation.calculator.CalculatorViewModel
import com.example.computingsystem.presentation.components.HistoryDialog
import com.example.computingsystem.presentation.components.MathKeyboard

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

    var contextMenuScreenOffset by remember { mutableStateOf(Offset.Zero) }
    var contextMenuCanvasOffset by remember { mutableStateOf(Offset.Zero) }
    var showContextMenu by remember { mutableStateOf(false) }

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
            onCanvasDoubleTap = { canvasOffset, screenOffset ->
                contextMenuCanvasOffset = canvasOffset
                contextMenuScreenOffset = screenOffset
                showContextMenu = true
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
            onCopyNode = { nodeId ->
                boardViewModel.onAction(BoardAction.CopyNode(nodeId))
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
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_text_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.TEXT)) }
                )
                DropdownMenuItem(
                    text = { Text("Математическое выражение") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_math_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
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

        val placingHintText = when {
            boardState.pendingCopyNodeId != null -> "Нажмите на доску, чтобы вставить копию"
            boardState.selectedNodeType != null  -> "Нажмите на доску, чтобы разместить блок"
            else                                 -> null
        }

        if (placingHintText != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = placingHintText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        if (showContextMenu) {
            ContextAddMenu(
                screenOffset = contextMenuScreenOffset,
                onDismiss = { showContextMenu = false },
                onSelectText = {
                    showContextMenu = false
                    boardViewModel.onAction(BoardAction.PlaceNodeOfType(NodeType.TEXT, contextMenuCanvasOffset))
                },
                onSelectMath = {
                    showContextMenu = false
                    boardViewModel.onAction(BoardAction.PlaceNodeOfType(NodeType.MATH, contextMenuCanvasOffset))
                }
            )
        }

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