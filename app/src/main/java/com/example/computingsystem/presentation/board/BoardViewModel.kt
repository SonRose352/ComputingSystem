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
        "ln(", "lg(",
        "^(-1)"
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
        }
    }

    // ─── Базовые действия ────────────────────────────────────────────────────

    private fun toggleAddMenu() {
        _uiState.update { it.copy(showAddMenu = !it.showAddMenu) }
    }

    private fun selectNodeType(type: NodeType) {
        _uiState.update {
            it.copy(
                selectedNodeType = type,
                showAddMenu = false,
                // Сбрасываем pending при ручном выборе типа
                pendingExpression = null,
                pendingResult = null
            )
        }
    }

    /**
     * Переводит доску в режим размещения MathNode с готовым выражением из истории.
     * Пользователь видит курсор размещения, тапает — нода появляется заполненной.
     */
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

        val node = when (nodeType) {
            NodeType.TEXT -> BoardNode.TextNode(
                position = Position(canvasOffset.x, canvasOffset.y)
            )
            NodeType.MATH -> BoardNode.MathNode(
                position = Position(canvasOffset.x, canvasOffset.y),
                // Если размещаем из истории — подставляем готовые значения
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
            if (node is BoardNode.MathNode) {
                initMathNode(nodeId, node.expression)
            }
        }
    }

    private fun clearActiveNode() {
        val activeId = _uiState.value.activeNodeId
        if (activeId != null) {
            val node = nodes.value.find { it.id == activeId }
            if (node is BoardNode.MathNode) {
                val expression = _uiState.value.mathTokens.joinToString("")
                if (expression != node.expression) {
                    saveMathExpression(activeId, expression)
                }
            }
        }
        _uiState.update {
            it.copy(
                activeNodeId = null,
                mathTokens = emptyList(),
                mathCursorPosition = 0
            )
        }
    }

    private fun moveNode(nodeId: String, newPosition: Position) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            val updatedNode = when (node) {
                is BoardNode.TextNode -> node.copy(position = newPosition)
                is BoardNode.MathNode -> node.copy(position = newPosition)
            }
            updateNode(updatedNode)
        }
    }

    private fun resizeNode(nodeId: String, newSize: Size) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            val updatedNode = when (node) {
                is BoardNode.TextNode -> node.copy(size = newSize)
                is BoardNode.MathNode -> node.copy(size = newSize)
            }
            updateNode(updatedNode)
        }
    }

    private fun deleteNodeAction(nodeId: String) {
        viewModelScope.launch {
            deleteNode(nodeId)
            clearActiveNode()
        }
    }

    // ─── Математическая клавиатура ───────────────────────────────────────────

    private fun tokenizeExpression(expression: String): List<String> {
        if (expression.isEmpty()) return emptyList()

        val tokens = mutableListOf<String>()
        var i = 0
        val knownFunctions = listOf(
            "asin(", "acos(", "atan(",
            "sin(", "cos(", "tan(",
            "sqrt(", "ln(", "lg(",
            "^(-1)"
        )

        while (i < expression.length) {
            val matchedFunction = knownFunctions.firstOrNull { func ->
                expression.startsWith(func, i)
            }
            if (matchedFunction != null) {
                tokens.add(matchedFunction)
                i += matchedFunction.length
                continue
            }
            if (expression[i].isDigit() || expression[i] == '.') {
                val numStart = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    i++
                }
                tokens.add(expression.substring(numStart, i))
                continue
            }
            tokens.add(expression[i].toString())
            i++
        }

        return tokens
    }

    private fun initMathNode(nodeId: String, expression: String) {
        val tokens = tokenizeExpression(expression)
        _uiState.update {
            it.copy(
                mathTokens = tokens,
                mathCursorPosition = tokens.size
            )
        }
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
            val tokenToDelete = state.mathTokens[pos - 1]
            val newTokens = state.mathTokens.toMutableList()

            if (tokenToDelete in functionTokens) {
                newTokens.removeAt(pos - 1)
            } else if (tokenToDelete.length > 1) {
                newTokens[pos - 1] = tokenToDelete.dropLast(1)
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
            if (state.mathCursorPosition > 0)
                state.copy(mathCursorPosition = state.mathCursorPosition - 1)
            else state
        }
    }

    private fun mathMoveCursorRight() {
        _uiState.update { state ->
            if (state.mathCursorPosition < state.mathTokens.size)
                state.copy(mathCursorPosition = state.mathCursorPosition + 1)
            else state
        }
    }

    private fun mathSetCursor(position: Int) {
        _uiState.update { state ->
            state.copy(mathCursorPosition = position.coerceIn(0, state.mathTokens.size))
        }
    }

    /**
     * Вычисляет результат, сохраняет его в ноду и пишет запись в историю вычислений.
     */
    private fun mathCalculate() {
        val state = _uiState.value
        val activeId = state.activeNodeId ?: return
        var expression = state.mathTokens.joinToString("")

        if (expression.isEmpty()) return

        val missing = expression.count { it == '(' } - expression.count { it == ')' }
        if (missing > 0) expression += ")".repeat(missing)

        val finalExpression = expression

        evaluate(finalExpression, AngleMode.RAD).fold(
            onSuccess = { result ->
                viewModelScope.launch {
                    // 1. Обновляем ноду
                    val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode
                        ?: return@launch
                    updateNode(node.copy(expression = finalExpression, result = result))

                    // 2. Сохраняем в историю вычислений
                    saveExpression(Expression(input = finalExpression, result = result))
                }
            },
            onFailure = {
                viewModelScope.launch {
                    val node = nodes.value.find { it.id == activeId } as? BoardNode.MathNode
                        ?: return@launch
                    updateNode(node.copy(expression = finalExpression, result = "Ошибка"))
                }
            }
        )
    }

    private fun scheduleEvaluation() {
        val state = _uiState.value
        val activeId = state.activeNodeId ?: return
        var expression = state.mathTokens.joinToString("")

        if (expression.isEmpty()) {
            saveMathExpression(activeId, "", "")
            return
        }

        val missing = expression.count { it == '(' } - expression.count { it == ')' }
        if (missing > 0) expression += ")".repeat(missing)

        evaluate(expression, AngleMode.RAD).fold(
            onSuccess = { result ->
                saveMathExpression(activeId, state.mathTokens.joinToString(""), result)
            },
            onFailure = {
                saveMathExpression(activeId, state.mathTokens.joinToString(""), "")
            }
        )
    }

    private fun saveMathExpression(nodeId: String, expression: String, result: String = "") {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return@launch
            updateNode(node.copy(expression = expression, result = result))
        }
    }
}