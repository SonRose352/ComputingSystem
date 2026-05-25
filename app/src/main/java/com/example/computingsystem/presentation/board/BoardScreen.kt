package com.example.computingsystem.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.presentation.board.components.ContextAddMenu
import com.example.computingsystem.presentation.board.components.DrawingToolbar
import com.example.computingsystem.presentation.board.components.InfiniteCanvas
import com.example.computingsystem.presentation.board.components.MapPinNameDialog
import com.example.computingsystem.presentation.board.components.MapPinOverlay
import com.example.computingsystem.presentation.board.components.MergeDialog
import com.example.computingsystem.presentation.board.components.SplitErrorDialog
import com.example.computingsystem.presentation.board.components.SplitMathNodeDialog
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
    val mapPins by boardViewModel.mapPins.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    val activeMathNode = remember(boardState.activeNodeId, nodes) {
        nodes.find { it.id == boardState.activeNodeId } as? BoardNode.MathNode
    }

    val activeDrawingNode = remember(boardState.activeNodeId, nodes) {
        nodes.find { it.id == boardState.activeNodeId } as? BoardNode.DrawingNode
    }

    var contextMenuScreenOffset by remember { mutableStateOf(Offset.Zero) }
    var contextMenuCanvasOffset by remember { mutableStateOf(Offset.Zero) }
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { size ->
                boardViewModel.onAction(
                    BoardAction.UpdateScreenSize(
                        width = size.width.toFloat(),
                        height = size.height.toFloat()
                    )
                )
            }
    ) {
        InfiniteCanvas(
            scale = boardState.scale,
            offset = boardState.offset,
            nodes = nodes,
            activeNodeId = boardState.activeNodeId,
            pinnedNodeId = boardState.pinnedNodeId,
            secondPinnedNodeId = boardState.secondPinnedNodeId,
            isPlacingMode = boardState.selectedNodeType != null || boardState.isPlacingMapPin,
            onScaleChange = { boardViewModel.onAction(BoardAction.UpdateScale(it)) },
            onOffsetChange = { boardViewModel.onAction(BoardAction.UpdateOffset(it)) },
            onCanvasTap = { offset ->
                when {
                    boardState.isPlacingMapPin -> {
                        boardViewModel.onAction(BoardAction.OnCanvasTapForMapPin(offset))
                    }
                    boardState.selectedNodeType != null -> {
                        boardViewModel.onAction(BoardAction.PlaceNode(offset))
                    }
                    else -> {
                        boardViewModel.onAction(BoardAction.ClearActiveNode)
                    }
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
            onDrawingNodeUpdate = { nodeId, strokes ->
                boardViewModel.onAction(BoardAction.UpdateDrawingNode(nodeId, strokes))
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
            onShowDrawingToolbar = { nodeId ->
                boardViewModel.onAction(BoardAction.ShowDrawingToolbar(nodeId))
            },
            onRecognizeDrawing = { nodeId ->
                boardViewModel.onAction(BoardAction.RecognizeDrawingNode(nodeId))
            },
            currentStrokeWidth = boardState.drawingStrokeWidth,
            currentStrokeColor = boardState.drawingStrokeColor,
            onSplitMathNode = { nodeId ->
                boardViewModel.onAction(BoardAction.StartSplitMathNode(nodeId))
            },
            modifier = Modifier
                .fillMaxSize()
        )

        MapPinOverlay(
            pins = mapPins,
            canvasOffset = boardState.offset,
            scale = boardState.scale
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
                DropdownMenuItem(
                    text = { Text("Рисунок") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_drawing_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { boardViewModel.onAction(BoardAction.SelectNodeType(NodeType.DRAWING)) }
                )
            }
        }

        // Кнопка навигации по точкам
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 72.dp)
        ) {
            IconButton(
                onClick = { boardViewModel.onAction(BoardAction.ToggleMapPinMenu) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_map_pin_area),
                    contentDescription = "Навигация",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            DropdownMenu(
                expanded = boardState.showMapPinMenu,
                onDismissRequest = { boardViewModel.onAction(BoardAction.ToggleMapPinMenu) },
                offset = DpOffset(0.dp, 8.dp)
            ) {
                // Список существующих пинов
                mapPins.forEach { pin ->
                    DropdownMenuItem(
                        text = { Text(pin.name) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_map_pin),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            // ИЗМЕНИТЬ: Обернуть в Row для двух кнопок
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Кнопка видимости
                                IconButton(
                                    onClick = {
                                        boardViewModel.onAction(BoardAction.ToggleMapPinVisibility(pin.id))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (pin.isVisible) R.drawable.ic_eye
                                            else R.drawable.ic_eye_slash
                                        ),
                                        contentDescription = if (pin.isVisible) "Скрыть" else "Показать",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // НОВОЕ: Кнопка удаления
                                IconButton(
                                    onClick = {
                                        boardViewModel.onAction(BoardAction.DeleteMapPin(pin.id))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить точку",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        onClick = {
                            boardViewModel.onAction(BoardAction.NavigateToMapPin(pin.id))
                        }
                    )
                }

                // Разделитель если есть пины
                if (mapPins.isNotEmpty()) {
                    HorizontalDivider()
                }

                // Кнопка добавления
                DropdownMenuItem(
                    text = { Text("Добавить") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_map_pin_plus),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        boardViewModel.onAction(BoardAction.StartPlacingMapPin)
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

        val placingHintText = when {
            boardState.isPlacingMapPin           -> "Нажмите на доску, чтобы разместить точку"
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
                },
                onSelectDrawing = {
                    showContextMenu = false
                    boardViewModel.onAction(BoardAction.PlaceNodeOfType(NodeType.DRAWING, contextMenuCanvasOffset))
                },
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

        if (boardState.showDrawingToolbar && activeDrawingNode != null) {
            DrawingToolbar(
                strokeWidth = boardState.drawingStrokeWidth,
                strokeColor = boardState.drawingStrokeColor,
                onStrokeWidthChange = {
                    boardViewModel.onAction(BoardAction.SetDrawingStrokeWidth(it))
                },
                onStrokeColorChange = {
                    boardViewModel.onAction(BoardAction.SetDrawingStrokeColor(it))
                },
                onClearAll = {
                    boardViewModel.onAction(BoardAction.ClearDrawing(activeDrawingNode.id))
                },
                onUndoLast = {
                    boardViewModel.onAction(BoardAction.UndoLastStroke(activeDrawingNode.id))
                },
                onClose = {
                    boardViewModel.onAction(BoardAction.HideDrawingToolbar)
                },
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

        // Индикатор распознавания
        if (boardState.isRecognizing) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Распознавание...")
                }
            }
        }

        // Предупреждение о частичном распознавании
        if (boardState.showRecognitionWarning) {
            AlertDialog(
                onDismissRequest = { boardViewModel.onAction(BoardAction.DismissRecognitionWarning) },
                title = { Text("Внимание") },
                text = { Text("Не все символы были распознаны корректно. Проверьте выражение.") },
                confirmButton = {
                    TextButton(onClick = { boardViewModel.onAction(BoardAction.DismissRecognitionWarning) }) {
                        Text("Понятно")
                    }
                }
            )
        }

        // Ошибка распознавания
        boardState.recognitionError?.let { error ->
            AlertDialog(
                onDismissRequest = { boardViewModel.onAction(BoardAction.DismissRecognitionWarning) },
                title = { Text("Ошибка") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { boardViewModel.onAction(BoardAction.DismissRecognitionWarning) }) {
                        Text("OK")
                    }
                }
            )
        }

        if (boardState.showSplitDialog) {
            if (boardState.splitErrorType != null) {
                val errorMessage = when (boardState.splitErrorType) {
                    SplitErrorType.EMPTY_BLOCK -> "Математический блок пустой. Заполните выражение перед разбиением."
                    SplitErrorType.ERROR_RESULT -> "Результат вычисления содержит ошибку. Исправьте выражение перед разбиением."
                    SplitErrorType.INVALID_PERCENT -> "Процент должен быть в диапазоне от 0 до 100."
                    null -> ""
                }

                SplitErrorDialog(
                    errorMessage = errorMessage,
                    onDismiss = {
                        boardViewModel.onAction(BoardAction.DismissSplitDialog)
                    }
                )
            }
            else {
                SplitMathNodeDialog(
                    firstPercent = boardState.splitFirstPercent,
                    onFirstPercentChange = { newPercent ->
                        boardViewModel.onAction(
                            BoardAction.UpdateSplitPercent(newPercent)
                        )
                    },
                    onConfirm = {
                        boardViewModel.onAction(
                            BoardAction.ConfirmSplitMathNode(boardState.splitFirstPercent)
                        )
                    },
                    onDismiss = {
                        boardViewModel.onAction(BoardAction.DismissSplitDialog)
                    }
                )
            }
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

    if (boardState.showMapPinNameDialog) {
        MapPinNameDialog(
            onConfirm = { name ->
                boardViewModel.onAction(BoardAction.ConfirmMapPinName(name))
            },
            onDismiss = {
                boardViewModel.onAction(BoardAction.DismissMapPinNameDialog)
            }
        )
    }
}