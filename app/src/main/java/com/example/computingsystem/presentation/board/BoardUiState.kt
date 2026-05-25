package com.example.computingsystem.presentation.board

import androidx.compose.ui.geometry.Offset
import com.example.computingsystem.domain.model.MapPin

data class BoardUiState(
    val showAddMenu: Boolean = false,
    val selectedNodeType: NodeType? = null,
    val pendingExpression: String? = null,
    val pendingResult: String? = null,
    val activeNodeId: String? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    // Состояние токенов и курсора для активной матноды
    val mathTokens: List<String> = emptyList(),
    val mathCursorPosition: Int = 0,
    val pinnedNodeId: String? = null,
    val secondPinnedNodeId: String? = null,
    val showMergeDialog: Boolean = false,
    val mergeValueA: String = "",
    val mergeValueB: String = "",
    val mergeOperator: String = "+",
    val mergeDialogOffset: Offset = Offset.Zero,
    val pendingCopyNodeId: String? = null,

    val showDrawingToolbar: Boolean = false,
    val drawingToolbarNodeId: String? = null,
    val drawingStrokeWidth: Float = 4f,
    val drawingStrokeColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF1C1B1F),

    val isRecognizing: Boolean = false,
    val recognitionError: String? = null,
    val showRecognitionWarning: Boolean = false,

    val showMapPinMenu: Boolean = false,
    val isPlacingMapPin: Boolean = false,
    val pendingMapPinPosition: Offset? = null,
    val showMapPinNameDialog: Boolean = false,
    val screenWidth: Float = 0f,
    val screenHeight: Float = 0f,

    val showSplitDialog: Boolean = false,
    val splitNodeId: String? = null,
    val splitFirstPercent: Float = 50f,
    val splitErrorType: SplitErrorType? = null,
)

enum class NodeType {
    TEXT, MATH, DRAWING
}

enum class SplitErrorType {
    EMPTY_BLOCK,
    ERROR_RESULT,
    INVALID_PERCENT
}