package com.example.computingsystem.presentation.board

import androidx.compose.ui.geometry.Offset

data class BoardUiState(
    val showAddMenu: Boolean = false,
    val selectedNodeType: NodeType? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero
)

enum class NodeType {
    TEXT, MATH
}