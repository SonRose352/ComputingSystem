package com.example.computingsystem.presentation.board

import androidx.compose.ui.geometry.Offset
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size

sealed class BoardAction {
    data object ToggleAddMenu : BoardAction()
    data class SelectNodeType(val type: NodeType) : BoardAction()
    data class PlaceNode(val canvasOffset: Offset) : BoardAction()
    data class UpdateTextNode(val nodeId: String, val text: String) : BoardAction()
    data class UpdateScale(val scale: Float) : BoardAction()
    data class UpdateOffset(val offset: Offset) : BoardAction()
    data class SetActiveNode(val nodeId: String?) : BoardAction()
    data object ClearActiveNode : BoardAction()
    data class MoveNode(val nodeId: String, val newPosition: Position) : BoardAction()
    data class ResizeNode(val nodeId: String, val newSize: Size) : BoardAction()
    data class DeleteNode(val nodeId: String) : BoardAction()

    data class PlaceMathNodeFromHistory(
        val expression: String,
        val result: String
    ) : BoardAction()

    data class InitMathNode(val nodeId: String, val expression: String) : BoardAction()
    data class MathKeyboardInput(val value: String) : BoardAction()
    data object MathKeyboardBackspace : BoardAction()
    data object MathKeyboardClear : BoardAction()
    data object MathKeyboardMoveCursorLeft : BoardAction()
    data object MathKeyboardMoveCursorRight : BoardAction()
    data class MathKeyboardSetCursor(val position: Int) : BoardAction()
    data object MathKeyboardCalculate : BoardAction()

    data class PinNode(val nodeId: String) : BoardAction()
    data class UnpinNode(val nodeId: String) : BoardAction()
    data class TriggerMerge(val dialogScreenOffset: Offset) : BoardAction()
    data class SetMergeOperator(val operator: String) : BoardAction()
    data object SwapMergeValues : BoardAction()
    data object ConfirmMerge : BoardAction()
    data object DismissMerge : BoardAction()

    data class CopyNode(val nodeId: String) : BoardAction()
}