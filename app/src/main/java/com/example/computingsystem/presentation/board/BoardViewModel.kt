package com.example.computingsystem.presentation.board

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.computingsystem.domain.model.Board
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.model.MapPin
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size
import com.example.computingsystem.domain.service.InkRecognitionService
import com.example.computingsystem.domain.usecase.board.CreateBoardUseCase
import com.example.computingsystem.domain.usecase.board.GetBoardsUseCase
import com.example.computingsystem.domain.usecase.boardnode.AddBoardNodeUseCase
import com.example.computingsystem.domain.usecase.boardnode.DeleteBoardNodeUseCase
import com.example.computingsystem.domain.usecase.expression.EvaluateExpressionUseCase
import com.example.computingsystem.domain.usecase.boardnode.GetBoardNodesUseCase
import com.example.computingsystem.domain.usecase.expression.SaveExpressionUseCase
import com.example.computingsystem.domain.usecase.boardnode.UpdateBoardNodeUseCase
import com.example.computingsystem.domain.usecase.mappin.AddMapPinUseCase
import com.example.computingsystem.domain.usecase.mappin.DeleteMapPinUseCase
import com.example.computingsystem.domain.usecase.mappin.GetMapPinsUseCase
import com.example.computingsystem.domain.usecase.mappin.UpdateMapPinUseCase
import com.example.computingsystem.presentation.calculator.AngleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val getBoards: GetBoardsUseCase,
    private val createBoard: CreateBoardUseCase,
    getNodes: GetBoardNodesUseCase,
    private val addNode: AddBoardNodeUseCase,
    private val updateNode: UpdateBoardNodeUseCase,
    private val deleteNode: DeleteBoardNodeUseCase,
    private val evaluate: EvaluateExpressionUseCase,
    private val saveExpression: SaveExpressionUseCase,
    private val inkRecognitionService: InkRecognitionService,
    getPins: GetMapPinsUseCase,
    private val addPin: AddMapPinUseCase,
    private val updatePin: UpdateMapPinUseCase,
    private val deletePin: DeleteMapPinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val _currentBoardId = MutableStateFlow<String>("")
    val currentBoardId: StateFlow<String> = _currentBoardId.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val nodes: StateFlow<List<BoardNode>> = _currentBoardId
        .filterNotNull()
        .flatMapLatest { boardId -> getNodes(boardId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val mapPins: StateFlow<List<MapPin>> = _currentBoardId
        .filterNotNull()
        .flatMapLatest { boardId -> getPins(boardId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val functionTokens = setOf(
        "sin(", "cos(", "tan(", "sqrt(",
        "asin(", "acos(", "atan(",
        "ln(", "lg(", "^(-1)"
    )

    init {
        viewModelScope.launch {
            inkRecognitionService.downloadModelIfNeeded()
        }

        viewModelScope.launch {
            initializeBoard()
        }
    }

    private suspend fun initializeBoard() {
        try {
            val boards = getBoards().first()

            if (boards.isEmpty()) {
                // Создаём дефолтную доску, если нет ни одной
                val defaultBoard = Board()
                createBoard(defaultBoard)
                _currentBoardId.value = defaultBoard.id
                Log.d("BoardViewModel", "Created default board: ${defaultBoard.id}")
            } else {
                // Берём первую (самую свежую) доску
                _currentBoardId.value = boards.first().id
                Log.d("BoardViewModel", "Loaded board: ${boards.first().id}")
            }
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Error initializing board", e)
        }
    }

    fun onAction(action: BoardAction) {
        when (action) {
            is BoardAction.ToggleAddMenu               -> toggleAddMenu()
            is BoardAction.SelectNodeType              -> selectNodeType(action.type)
            is BoardAction.PlaceNode                   -> placeNode(action.canvasOffset)
            is BoardAction.PlaceMathNodeFromHistory    -> placeMathNodeFromHistory(action.expression, action.result)
            is BoardAction.UpdateTextNode              -> updateTextNode(action.nodeId, action.text)
            is BoardAction.UpdateScale                 -> updateScale(action.scale)
            is BoardAction.UpdateOffset                -> updateOffset(action.offset)
            is BoardAction.SetActiveNode               -> setActiveNode(action.nodeId)
            is BoardAction.ClearActiveNode             -> clearActiveNode()
            is BoardAction.MoveNode                    -> moveNode(action.nodeId, action.newPosition)
            is BoardAction.ResizeNode                  -> resizeNode(action.nodeId, action.newSize)
            is BoardAction.DeleteNode                  -> deleteNodeAction(action.nodeId)
            is BoardAction.InitMathNode                -> initMathNode(action.nodeId, action.expression)
            is BoardAction.MathKeyboardInput           -> mathInput(action.value)
            is BoardAction.MathKeyboardBackspace       -> mathBackspace()
            is BoardAction.MathKeyboardClear           -> mathClear()
            is BoardAction.MathKeyboardMoveCursorLeft  -> mathMoveCursorLeft()
            is BoardAction.MathKeyboardMoveCursorRight -> mathMoveCursorRight()
            is BoardAction.MathKeyboardSetCursor       -> mathSetCursor(action.position)
            is BoardAction.MathKeyboardCalculate       -> mathCalculate()
            is BoardAction.PinNode                     -> pinNode(action.nodeId)
            is BoardAction.UnpinNode                   -> unpinNode(action.nodeId)
            is BoardAction.TriggerMerge                -> triggerMerge(action.dialogScreenOffset)
            is BoardAction.SetMergeOperator            -> setMergeOperator(action.operator)
            is BoardAction.SwapMergeValues             -> swapMergeValues()
            is BoardAction.ConfirmMerge                -> confirmMerge()
            is BoardAction.DismissMerge                -> dismissMerge()
            is BoardAction.CopyNode                    -> copyNode(action.nodeId)
            is BoardAction.PlaceNodeOfType             -> placeNodeOfType(action.type, action.canvasOffset)
            is BoardAction.UpdateDrawingNode           -> updateDrawingNode(action.nodeId, action.strokes)
            is BoardAction.ShowDrawingToolbar          -> showDrawingToolbar(action.nodeId)
            is BoardAction.HideDrawingToolbar          -> hideDrawingToolbar()
            is BoardAction.SetDrawingStrokeWidth       -> setDrawingStrokeWidth(action.width)
            is BoardAction.SetDrawingStrokeColor       -> setDrawingStrokeColor(action.color)
            is BoardAction.ClearDrawing                -> clearDrawing(action.nodeId)
            is BoardAction.UndoLastStroke              -> undoLastStroke(action.nodeId)
            is BoardAction.RecognizeDrawingNode        -> recognizeDrawingNode(action.nodeId)
            is BoardAction.DismissRecognitionWarning   -> dismissRecognitionWarning()
            is BoardAction.ToggleMapPinMenu        -> toggleMapPinMenu()
            is BoardAction.StartPlacingMapPin      -> startPlacingMapPin()
            is BoardAction.OnCanvasTapForMapPin    -> onCanvasTapForMapPin(action.canvasOffset)
            is BoardAction.ConfirmMapPinName       -> confirmMapPinName(action.name)
            is BoardAction.DismissMapPinNameDialog -> dismissMapPinNameDialog()
            is BoardAction.ToggleMapPinVisibility  -> toggleMapPinVisibility(action.pinId)
            is BoardAction.NavigateToMapPin        -> navigateToMapPin(action.pinId)
            is BoardAction.DeleteMapPin            -> deleteMapPin(action.pinId)
            is BoardAction.UpdateScreenSize        -> updateScreenSize(action.width, action.height)
            is BoardAction.StartSplitMathNode     -> startSplitMathNode(action.nodeId)
            is BoardAction.ConfirmSplitMathNode   -> confirmSplitMathNode(action.firstPercent)
            is BoardAction.DismissSplitDialog     -> dismissSplitDialog()
            is BoardAction.UpdateSplitPercent -> updateSplitPercent(action.percent)
        }
    }

    private fun updateSplitPercent(percent: Float) {
        _uiState.update {
            it.copy(splitFirstPercent = percent)
        }
    }

    private fun startSplitMathNode(nodeId: String) {
        val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return

        when {
            node.expression.isEmpty() -> {
                _uiState.update {
                    it.copy(
                        showSplitDialog = true,
                        splitNodeId = nodeId,
                        splitErrorType = SplitErrorType.EMPTY_BLOCK
                    )
                }
            }
            node.result.isEmpty() || node.result == "Ошибка" -> {
                _uiState.update {
                    it.copy(
                        showSplitDialog = true,
                        splitNodeId = nodeId,
                        splitErrorType = SplitErrorType.ERROR_RESULT
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        showSplitDialog = true,
                        splitNodeId = nodeId,
                        splitFirstPercent = 50f,
                        splitErrorType = null
                    )
                }
            }
        }
    }

    private fun confirmSplitMathNode(firstPercent: Float) {
        val state = _uiState.value
        val nodeId = state.splitNodeId ?: return
        val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return

        if (firstPercent !in 0f..100f) {
            _uiState.update {
                it.copy(splitErrorType = SplitErrorType.INVALID_PERCENT)
            }
            return
        }

        if (state.splitErrorType != null) {
            return
        }

        val originalValue = node.result.toDoubleOrNull() ?: run {
            dismissSplitDialog()
            return
        }

        val firstValue = originalValue * (firstPercent / 100.0)
        val secondValue = originalValue * ((100f - firstPercent) / 100.0)

        val firstFormatted = if (firstValue % 1.0 == 0.0) {
            firstValue.toLong().toString()
        } else {
            firstValue.toBigDecimal().stripTrailingZeros().toPlainString()
        }

        val secondFormatted = if (secondValue % 1.0 == 0.0) {
            secondValue.toLong().toString()
        } else {
            secondValue.toBigDecimal().stripTrailingZeros().toPlainString()
        }

        viewModelScope.launch {
            val offset = 100f

            val firstNode = BoardNode.MathNode(
                position = Position(
                    x = node.position.x - offset,
                    y = node.position.y
                ),
                size = node.size,
                expression = firstFormatted,
                result = firstFormatted
            )

            val secondNode = BoardNode.MathNode(
                position = Position(
                    x = node.position.x + offset,
                    y = node.position.y
                ),
                size = node.size,
                expression = secondFormatted,
                result = secondFormatted
            )

            deleteNode(nodeId)

            addNode(firstNode, currentBoardId.value)
            addNode(secondNode, currentBoardId.value)
        }

        dismissSplitDialog()
    }

    private fun dismissSplitDialog() {
        _uiState.update {
            it.copy(
                showSplitDialog = false,
                splitNodeId = null,
                splitFirstPercent = 50f,
                splitErrorType = null
            )
        }
    }

    private fun updateScreenSize(width: Float, height: Float) {
        _uiState.update {
            it.copy(screenWidth = width, screenHeight = height)
        }
    }

    private fun toggleMapPinMenu() {
        _uiState.update { it.copy(showMapPinMenu = !it.showMapPinMenu) }
    }

    private fun startPlacingMapPin() {
        _uiState.update {
            it.copy(
                showMapPinMenu = false,
                isPlacingMapPin = true
            )
        }
    }

    private fun onCanvasTapForMapPin(canvasOffset: Offset) {
        _uiState.update {
            it.copy(
                isPlacingMapPin = false,
                pendingMapPinPosition = canvasOffset,
                showMapPinNameDialog = true
            )
        }
    }

    private fun confirmMapPinName(name: String) {
        val pos = _uiState.value.pendingMapPinPosition ?: return
        val newPin = MapPin(
            name = name.ifBlank { "Точка" },
            x = pos.x,
            y = pos.y
        )
        viewModelScope.launch {
            addPin(newPin, currentBoardId.value)
        }
        _uiState.update {
            it.copy(
                pendingMapPinPosition = null,
                showMapPinNameDialog = false
            )
        }
    }

    private fun dismissMapPinNameDialog() {
        _uiState.update {
            it.copy(
                pendingMapPinPosition = null,
                showMapPinNameDialog = false
            )
        }
    }

    private fun toggleMapPinVisibility(pinId: String) {
        val pin = mapPins.value.find { it.id == pinId } ?: return
        viewModelScope.launch {
            updatePin(pin.copy(isVisible = !pin.isVisible), currentBoardId.value)
        }
    }

    private fun navigateToMapPin(pinId: String) {
        val pin = mapPins.value.find { it.id == pinId } ?: return
        val state = _uiState.value
        val screenCenterX = state.screenWidth / 2f
        val screenCenterY = state.screenHeight / 2f
        val newOffset = Offset(
            x = screenCenterX - pin.x * state.scale,
            y = screenCenterY - pin.y * state.scale
        )
        _uiState.update { it.copy(offset = newOffset, showMapPinMenu = false) }
    }

    private fun deleteMapPin(pinId: String) {
        viewModelScope.launch {
            deletePin(pinId)

            if (mapPins.value.size <= 1) {
                _uiState.update { it.copy(showMapPinMenu = false) }
            }
        }
    }

    private fun dismissRecognitionWarning() {
        _uiState.update { it.copy(showRecognitionWarning = false, recognitionError = null) }
    }

    private fun recognizeDrawingNode(nodeId: String) {
        val node = nodes.value.find { it.id == nodeId } as? BoardNode.DrawingNode ?: return
        if (node.strokes.isEmpty()) return

        _uiState.update { it.copy(isRecognizing = true, recognitionError = null) }

        viewModelScope.launch {
            try {
                // Скачиваем модель при необходимости
                val ready = inkRecognitionService.downloadModelIfNeeded()
                if (!ready) {
                    _uiState.update {
                        it.copy(isRecognizing = false, recognitionError = "Не удалось загрузить модель распознавания")
                    }
                    return@launch
                }

                // Извлекаем только координаты точек из штрихов (без метаданных цвета/ширины)
                val rawStrokes = extractRawStrokes(node.strokes)

                val result = inkRecognitionService.recognize(rawStrokes)

                // Создаём MathNode на месте DrawingNode
                val mathNode = BoardNode.MathNode(
                    position = node.position,
                    size = node.size,
                    expression = result.expression,
                    result = ""
                )

                // Вычисляем результат если возможно
                evaluate(result.expression, AngleMode.RAD).fold(
                    onSuccess = { evalResult ->
                        viewModelScope.launch {
                            deleteNode(nodeId)
                            addNode(mathNode.copy(result = evalResult), currentBoardId.value)
                            if (result.hasUncertainSymbols) {
                                _uiState.update {
                                    it.copy(
                                        isRecognizing = false,
                                        showRecognitionWarning = true
                                    )
                                }
                            } else {
                                _uiState.update { it.copy(isRecognizing = false) }
                            }
                        }
                    },
                    onFailure = {
                        viewModelScope.launch {
                            deleteNode(nodeId)
                            addNode(mathNode, currentBoardId.value)
                            _uiState.update {
                                it.copy(
                                    isRecognizing = false,
                                    showRecognitionWarning = result.hasUncertainSymbols
                                )
                            }
                        }
                    }
                )

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRecognizing = false,
                        recognitionError = "Ошибка распознавания: ${e.message}"
                    )
                }
            }
        }
    }

    // Извлекаем чистые точки, пропуская заголовки штрихов (формат из DrawingNodeView)
    private fun extractRawStrokes(
        serialized: List<List<Pair<Float, Float>>>
    ): List<List<Pair<Float, Float>>> {
        return serialized.map { stroke ->
            if (stroke.isNotEmpty() && stroke.first().first == -1f) {
                // Новый формат: первые 3 элемента — метаданные
                stroke.drop(3)
            } else {
                stroke
            }
        }.filter { it.isNotEmpty() }
    }

    private fun showDrawingToolbar(nodeId: String) {
        _uiState.update { state ->
            val alreadyOpen = state.showDrawingToolbar && state.drawingToolbarNodeId == nodeId
            state.copy(
                showDrawingToolbar = !alreadyOpen,
                drawingToolbarNodeId = if (alreadyOpen) null else nodeId
            )
        }
    }

    private fun hideDrawingToolbar() {
        _uiState.update {
            it.copy(
                showDrawingToolbar = false,
                drawingToolbarNodeId = null
            )
        }
    }

    private fun setDrawingStrokeWidth(width: Float) {
        _uiState.update { it.copy(drawingStrokeWidth = width) }
    }

    private fun setDrawingStrokeColor(color: Color) {
        _uiState.update { it.copy(drawingStrokeColor = color) }
    }

    private fun clearDrawing(nodeId: String) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.DrawingNode ?: return@launch
            updateNode(node.copy(strokes = emptyList()), currentBoardId.value)
        }
    }

    private fun undoLastStroke(nodeId: String) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.DrawingNode ?: return@launch
            if (node.strokes.isNotEmpty()) {
                updateNode(node.copy(strokes = node.strokes.dropLast(1)), currentBoardId.value)
            }
        }
    }

    private fun updateDrawingNode(nodeId: String, strokes: List<List<Pair<Float, Float>>>) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.DrawingNode ?: return@launch
            updateNode(node.copy(strokes = strokes), currentBoardId.value)
        }
    }

    private fun placeNodeOfType(type: NodeType, canvasOffset: Offset) {
        val node = when (type) {
            NodeType.TEXT -> BoardNode.TextNode(position = Position(canvasOffset.x, canvasOffset.y))
            NodeType.MATH -> BoardNode.MathNode(position = Position(canvasOffset.x, canvasOffset.y))
            NodeType.DRAWING -> BoardNode.DrawingNode(position = Position(canvasOffset.x, canvasOffset.y))
        }
        viewModelScope.launch { addNode(node, currentBoardId.value) }
    }

    private fun copyNode(nodeId: String) {
        val node = nodes.value.find { it.id == nodeId } ?: return
        val nodeType = when (node) {
            is BoardNode.TextNode -> NodeType.TEXT
            is BoardNode.MathNode -> NodeType.MATH
            is BoardNode.DrawingNode -> NodeType.DRAWING
        }
        _uiState.update {
            it.copy(
                selectedNodeType = nodeType,
                pendingCopyNodeId = nodeId,
                activeNodeId = null,
                mathTokens = emptyList(),
                mathCursorPosition = 0
            )
        }
    }

    private fun toggleAddMenu() {
        _uiState.update { it.copy(showAddMenu = !it.showAddMenu) }
    }

    private fun selectNodeType(type: NodeType) {
        _uiState.update {
            it.copy(
                selectedNodeType = type,
                showAddMenu = false,
                pendingExpression = null,
                pendingResult = null
            )
        }
    }

    private fun placeMathNodeFromHistory(expression: String, result: String) {
        _uiState.update {
            it.copy(
                selectedNodeType = NodeType.MATH,
                pendingExpression = expression,
                pendingResult = result,
                showAddMenu = false
            )
        }
    }

    private fun placeNode(canvasOffset: Offset) {
        val state = _uiState.value
        val nodeType = state.selectedNodeType ?: return

        if (state.pendingCopyNodeId != null) {
            val original = nodes.value.find { it.id == state.pendingCopyNodeId } ?: run {
                _uiState.update { it.copy(selectedNodeType = null, pendingCopyNodeId = null) }
                return
            }
            val copy = when (original) {
                is BoardNode.TextNode -> original.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    position = Position(canvasOffset.x, canvasOffset.y)
                )
                is BoardNode.MathNode -> original.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    position = Position(canvasOffset.x, canvasOffset.y)
                )
                is BoardNode.DrawingNode -> original.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    position = Position(canvasOffset.x, canvasOffset.y)
                )

            }
            viewModelScope.launch {
                addNode(copy, currentBoardId.value)
                _uiState.update {
                    it.copy(
                        selectedNodeType = null,
                        pendingCopyNodeId = null,
                        pendingExpression = null,
                        pendingResult = null
                    )
                }
            }
            return
        }

        val node = when (nodeType) {
            NodeType.TEXT -> BoardNode.TextNode(position = Position(canvasOffset.x, canvasOffset.y))
            NodeType.MATH -> BoardNode.MathNode(
                position = Position(canvasOffset.x, canvasOffset.y),
                expression = state.pendingExpression ?: "",
                result = state.pendingResult ?: ""
            )
            NodeType.DRAWING -> BoardNode.DrawingNode(
                position = Position(canvasOffset.x, canvasOffset.y)
            )
        }
        viewModelScope.launch {
            addNode(node, currentBoardId.value)
            _uiState.update {
                it.copy(
                    selectedNodeType = null,
                    pendingExpression = null,
                    pendingResult = null
                )
            }
        }
    }

    private fun updateTextNode(nodeId: String, text: String) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.TextNode ?: return@launch
            updateNode(node.copy(text = text), currentBoardId.value)
        }
    }

    private fun updateScale(scale: Float) {
        _uiState.update { it.copy(scale = scale) }
        Log.i("BoardViewModel", "Scale updated: $scale")
    }

    private fun updateOffset(offset: Offset) {
        _uiState.update { it.copy(offset = offset) }
        Log.i("BoardViewModel", "Offset updated: $offset")
    }

    private fun setActiveNode(nodeId: String?) {
        _uiState.update {
            it.copy(
                activeNodeId = nodeId,
                showDrawingToolbar = false,
                drawingToolbarNodeId = null
            )
        }
        if (nodeId != null) {
            val node = nodes.value.find { it.id == nodeId }
            if (node is BoardNode.MathNode) initMathNode(nodeId, node.expression)
        }
    }

    private fun clearActiveNode() {
        val activeId = _uiState.value.activeNodeId
        if (activeId != null) {
            val node = nodes.value.find { it.id == activeId }
            if (node is BoardNode.MathNode) {
                val expression = _uiState.value.mathTokens.joinToString("")
                if (expression != node.expression) saveMathExpression(activeId, expression)
            }
        }
        _uiState.update {
            it.copy(
                activeNodeId = null,
                mathTokens = emptyList(),
                mathCursorPosition = 0,
                showDrawingToolbar = false,
                drawingToolbarNodeId = null
            )
        }
    }

    private fun moveNode(nodeId: String, newPosition: Position) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            updateNode(
                when (node) {
                    is BoardNode.TextNode -> node.copy(position = newPosition)
                    is BoardNode.MathNode -> node.copy(position = newPosition)
                    is BoardNode.DrawingNode -> node.copy(position = newPosition)
                },
                currentBoardId.value
            )
        }
    }

    private fun resizeNode(nodeId: String, newSize: Size) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            updateNode(
                when (node) {
                    is BoardNode.TextNode -> node.copy(size = newSize)
                    is BoardNode.MathNode -> node.copy(size = newSize)
                    is BoardNode.DrawingNode -> node.copy(size = newSize)
                },
                currentBoardId.value
            )
        }
    }

    private fun deleteNodeAction(nodeId: String) {
        viewModelScope.launch {
            deleteNode(nodeId)
            clearActiveNode()
        }
    }

    private fun pinNode(nodeId: String) {
        val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return
        // Нода должна иметь вычисленный результат, чтобы её можно было слить
        if (node.result.isEmpty() || node.result == "Ошибка") return

        _uiState.update { state ->
            when {
                // Уже зажата эта же нода — игнорируем
                state.pinnedNodeId == nodeId || state.secondPinnedNodeId == nodeId -> state
                // Первая нода ещё не выбрана
                state.pinnedNodeId == null -> state.copy(pinnedNodeId = nodeId)
                // Первая уже есть — ставим вторую
                else -> state.copy(secondPinnedNodeId = nodeId)
            }
        }
    }

    private fun unpinNode(nodeId: String) {
        _uiState.update { state ->
            if (state.showMergeDialog) return@update state
            state.copy(
                pinnedNodeId   = if (state.pinnedNodeId   == nodeId) null else state.pinnedNodeId,
                secondPinnedNodeId = if (state.secondPinnedNodeId == nodeId) null else state.secondPinnedNodeId
            )
        }
    }

    private fun triggerMerge(dialogScreenOffset: Offset) {
        val state = _uiState.value
        val idA = state.pinnedNodeId ?: return
        val idB = state.secondPinnedNodeId ?: return

        val nodeA = nodes.value.find { it.id == idA } as? BoardNode.MathNode ?: return
        val nodeB = nodes.value.find { it.id == idB } as? BoardNode.MathNode ?: return

        _uiState.update {
            it.copy(
                showMergeDialog  = true,
                mergeValueA      = nodeA.result,
                mergeValueB      = nodeB.result,
                mergeOperator    = "+",
                mergeDialogOffset = dialogScreenOffset
            )
        }
    }

    private fun setMergeOperator(operator: String) {
        _uiState.update { it.copy(mergeOperator = operator) }
    }

    private fun swapMergeValues() {
        _uiState.update { it.copy(mergeValueA = it.mergeValueB, mergeValueB = it.mergeValueA) }
    }

    private fun confirmMerge() {
        val state = _uiState.value
        val idA = state.pinnedNodeId ?: return
        val idB = state.secondPinnedNodeId ?: return

        val nodeA = nodes.value.find { it.id == idA } as? BoardNode.MathNode ?: return
        val nodeB = nodes.value.find { it.id == idB } as? BoardNode.MathNode ?: return

        val expression = "${state.mergeValueA}${state.mergeOperator}${state.mergeValueB}"

        val midX = (nodeA.position.x + nodeB.position.x) / 2f
        val midY = (nodeA.position.y + nodeB.position.y) / 2f

        evaluate(expression, AngleMode.RAD).fold(
            onSuccess = { result ->
                viewModelScope.launch {
                    deleteNode(idA)
                    deleteNode(idB)

                    val newNode = BoardNode.MathNode(
                        position   = Position(midX, midY),
                        expression = expression,
                        result     = result
                    )
                    addNode(newNode, currentBoardId.value)

                    saveExpression(Expression(input = expression, result = result))
                }
            },
            onFailure = {
                viewModelScope.launch {
                    deleteNode(idA)
                    deleteNode(idB)
                    addNode(
                        BoardNode.MathNode(
                            position   = Position(midX, midY),
                            expression = expression,
                            result     = "Ошибка"
                        ),
                        currentBoardId.value
                    )
                }
            }
        )

        resetMergeState()
    }

    private fun dismissMerge() {
        resetMergeState()
    }

    private fun resetMergeState() {
        _uiState.update {
            it.copy(
                pinnedNodeId       = null,
                secondPinnedNodeId = null,
                showMergeDialog    = false,
                mergeValueA        = "",
                mergeValueB        = "",
                mergeOperator      = "+",
                mergeDialogOffset  = Offset.Zero
            )
        }
    }

    private fun tokenizeExpression(expression: String): List<String> {
        if (expression.isEmpty()) return emptyList()
        val tokens = mutableListOf<String>()
        var i = 0
        val knownFunctions = listOf(
            "asin(", "acos(", "atan(",
            "sin(", "cos(", "tan(",
            "sqrt(", "ln(", "lg(", "^(-1)"
        )
        while (i < expression.length) {
            val matched = knownFunctions.firstOrNull { expression.startsWith(it, i) }
            if (matched != null) { tokens.add(matched); i += matched.length; continue }
            if (expression[i].isDigit() || expression[i] == '.') {
                val start = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) i++
                tokens.add(expression.substring(start, i)); continue
            }
            tokens.add(expression[i].toString()); i++
        }
        return tokens
    }

    private fun initMathNode(nodeId: String, expression: String) {
        val tokens = tokenizeExpression(expression)
        _uiState.update { it.copy(mathTokens = tokens, mathCursorPosition = tokens.size) }
    }

    private fun mathInput(value: String) {
        _uiState.update { state ->
            val pos = state.mathCursorPosition
            val newTokens = state.mathTokens.toMutableList().apply { add(pos, value) }
            state.copy(mathTokens = newTokens, mathCursorPosition = pos + 1)
        }
        scheduleEvaluation()
    }

    private fun mathBackspace() {
        _uiState.update { state ->
            if (state.mathTokens.isEmpty() || state.mathCursorPosition == 0) return@update state
            val pos = state.mathCursorPosition
            val token = state.mathTokens[pos - 1]
            val newTokens = state.mathTokens.toMutableList()
            if (token in functionTokens) {
                newTokens.removeAt(pos - 1)
            } else if (token.length > 1) {
                newTokens[pos - 1] = token.dropLast(1)
                return@update state.copy(mathTokens = newTokens)
            } else {
                newTokens.removeAt(pos - 1)
            }
            state.copy(mathTokens = newTokens, mathCursorPosition = pos - 1)
        }
        scheduleEvaluation()
    }

    private fun mathClear() {
        _uiState.update { it.copy(mathTokens = emptyList(), mathCursorPosition = 0) }
        val activeId = _uiState.value.activeNodeId ?: return
        viewModelScope.launch {
            val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode ?: return@launch
            updateNode(node.copy(expression = "", result = ""), currentBoardId.value)
        }
    }

    private fun mathMoveCursorLeft() {
        _uiState.update { state ->
            if (state.mathCursorPosition > 0) state.copy(mathCursorPosition = state.mathCursorPosition - 1)
            else state
        }
    }

    private fun mathMoveCursorRight() {
        _uiState.update { state ->
            if (state.mathCursorPosition < state.mathTokens.size) state.copy(mathCursorPosition = state.mathCursorPosition + 1)
            else state
        }
    }

    private fun mathSetCursor(position: Int) {
        _uiState.update { state ->
            state.copy(mathCursorPosition = position.coerceIn(0, state.mathTokens.size))
        }
    }

    private fun mathCalculate() {
        val state = _uiState.value
        val activeId = state.activeNodeId ?: return
        var expression = state.mathTokens.joinToString("")
        if (expression.isEmpty()) return
        val missing = expression.count { it == '(' } - expression.count { it == ')' }
        if (missing > 0) expression += ")".repeat(missing)
        val final = expression
        evaluate(final, AngleMode.RAD).fold(
            onSuccess = { result ->
                viewModelScope.launch {
                    val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode ?: return@launch
                    updateNode(node.copy(expression = final, result = result), currentBoardId.value)
                    saveExpression(Expression(input = final, result = result))
                }
            },
            onFailure = {
                viewModelScope.launch {
                    val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode ?: return@launch
                    updateNode(node.copy(expression = final, result = "Ошибка"), currentBoardId.value)
                }
            }
        )
    }

    private fun scheduleEvaluation() {
        val state = _uiState.value
        val activeId = state.activeNodeId ?: return
        var expression = state.mathTokens.joinToString("")
        if (expression.isEmpty()) { saveMathExpression(activeId, "", ""); return }
        val missing = expression.count { it == '(' } - expression.count { it == ')' }
        if (missing > 0) expression += ")".repeat(missing)
        evaluate(expression, AngleMode.RAD).fold(
            onSuccess = { result -> saveMathExpression(activeId, state.mathTokens.joinToString(""), result) },
            onFailure = { saveMathExpression(activeId, state.mathTokens.joinToString(""), "") }
        )
    }

    private fun saveMathExpression(nodeId: String, expression: String, result: String = "") {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return@launch
            updateNode(node.copy(expression = expression, result = result), currentBoardId.value)
        }
    }
}