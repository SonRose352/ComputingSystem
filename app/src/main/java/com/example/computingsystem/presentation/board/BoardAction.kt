package com.example.computingsystem.presentation.board

import androidx.compose.ui.geometry.Offset

sealed class BoardAction {
    data object ToggleAddMenu : BoardAction()
    data class SelectNodeType(val type: NodeType) : BoardAction()
    data class PlaceNode(val canvasOffset: Offset) : BoardAction()
    data class UpdateTextNode(val nodeId: String, val text: String) : BoardAction()
    data class UpdateMathNode(val nodeId: String, val expression: String) : BoardAction()
    data class UpdateScale(val scale: Float) : BoardAction()
    data class UpdateOffset(val offset: Offset) : BoardAction()
}