package com.example.computingsystem.presentation.board

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.usecase.AddBoardNodeUseCase
import com.example.computingsystem.domain.usecase.GetBoardNodesUseCase
import com.example.computingsystem.domain.usecase.UpdateBoardNodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    getNodes: GetBoardNodesUseCase,
    private val addNode: AddBoardNodeUseCase,
    private val updateNode: UpdateBoardNodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    val nodes: StateFlow<List<BoardNode>> = getNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAction(action: BoardAction) {
        when (action) {
            is BoardAction.ToggleAddMenu -> toggleAddMenu()
            is BoardAction.SelectNodeType -> selectNodeType(action.type)
            is BoardAction.PlaceNode -> placeNode(action.canvasOffset)
            is BoardAction.UpdateTextNode -> updateTextNode(action.nodeId, action.text)
            is BoardAction.UpdateMathNode -> updateMathNode(action.nodeId, action.expression)
            is BoardAction.UpdateScale -> updateScale(action.scale)
            is BoardAction.UpdateOffset -> updateOffset(action.offset)
        }
    }

    private fun toggleAddMenu() {
        _uiState.update { it.copy(showAddMenu = !it.showAddMenu) }
    }

    private fun selectNodeType(type: NodeType) {
        _uiState.update { it.copy(selectedNodeType = type, showAddMenu = false) }
    }

    private fun placeNode(canvasOffset: Offset) {
        val nodeType = _uiState.value.selectedNodeType ?: return

        val node = when (nodeType) {
            NodeType.TEXT -> BoardNode.TextNode(
                position = Position(canvasOffset.x, canvasOffset.y)
            )
            NodeType.MATH -> BoardNode.MathNode(
                position = Position(canvasOffset.x, canvasOffset.y)
            )
        }

        viewModelScope.launch {
            addNode(node)
            _uiState.update { it.copy(selectedNodeType = null) }
        }
    }

    private fun updateTextNode(nodeId: String, text: String) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.TextNode ?: return@launch
            updateNode(node.copy(text = text))
        }
    }

    private fun updateMathNode(nodeId: String, expression: String) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } as? BoardNode.MathNode ?: return@launch
            val result = evaluateExpression(expression)
            updateNode(node.copy(expression = expression, result = result))
        }
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            // TODO: Подключить EvaluateExpressionUseCase
            "результат"
        } catch (e: Exception) {
            "ошибка"
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
}