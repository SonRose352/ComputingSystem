package com.example.computingsystem.presentation.board

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size
import com.example.computingsystem.domain.usecase.AddBoardNodeUseCase
import com.example.computingsystem.domain.usecase.DeleteBoardNodeUseCase
import com.example.computingsystem.domain.usecase.EvaluateExpressionUseCase
import com.example.computingsystem.domain.usecase.GetBoardNodesUseCase
import com.example.computingsystem.domain.usecase.SaveExpressionUseCase
import com.example.computingsystem.domain.usecase.UpdateBoardNodeUseCase
import com.example.computingsystem.presentation.calculator.AngleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    getNodes: GetBoardNodesUseCase,
    private val addNode: AddBoardNodeUseCase,
    private val updateNode: UpdateBoardNodeUseCase,
    private val deleteNode: DeleteBoardNodeUseCase,
    private val evaluate: EvaluateExpressionUseCase,
    private val saveExpression: SaveExpressionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    val nodes: StateFlow<List<BoardNode>> = getNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val functionTokens = setOf(
        "sin(", "cos(", "tan(", "sqrt(",
        "asin(", "acos(", "atan(",
        "ln(", "lg(", "^(-1)"
    )

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
        }
    }

    private fun copyNode(nodeId: String) {
        val node = nodes.value.find { it.id == nodeId } ?: return
        val nodeType = when (node) {
            is BoardNode.TextNode -> NodeType.TEXT
            is BoardNode.MathNode -> NodeType.MATH
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
            }
            viewModelScope.launch {
                addNode(copy)
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
        }
        viewModelScope.launch {
            addNode(node)
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
            updateNode(node.copy(text = text))
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
        _uiState.update { it.copy(activeNodeId = nodeId) }
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
            it.copy(activeNodeId = null, mathTokens = emptyList(), mathCursorPosition = 0)
        }
    }

    private fun moveNode(nodeId: String, newPosition: Position) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            updateNode(
                when (node) {
                    is BoardNode.TextNode -> node.copy(position = newPosition)
                    is BoardNode.MathNode -> node.copy(position = newPosition)
                }
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
                }
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
                    addNode(newNode)

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
                        )
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
            updateNode(node.copy(expression = "", result = ""))
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
                    updateNode(node.copy(expression = final, result = result))
                    saveExpression(Expression(input = final, result = result))
                }
            },
            onFailure = {
                viewModelScope.launch {
                    val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode ?: return@launch
                    updateNode(node.copy(expression = final, result = "Ошибка"))
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
            updateNode(node.copy(expression = expression, result = result))
        }
    }
}