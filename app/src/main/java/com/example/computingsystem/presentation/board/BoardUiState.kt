package com.example.computingsystem.presentation.board

import androidx.compose.ui.geometry.Offset

data class BoardUiState(
    val showAddMenu: Boolean = false,
    val selectedNodeType: NodeType? = null,
    // Если нода размещается из истории — здесь хранятся готовые expression/result
    val pendingExpression: String? = null,
    val pendingResult: String? = null,
    val activeNodeId: String? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    // Состояние токенов и курсора для активной матноды
    val mathTokens: List<String> = emptyList(),
    val mathCursorPosition: Int = 0
)

enum class NodeType {
    TEXT, MATH
}