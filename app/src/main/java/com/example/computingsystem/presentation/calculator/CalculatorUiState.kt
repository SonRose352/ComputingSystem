package com.example.computingsystem.presentation.calculator

data class CalculatorUiState(
    val tokens: List<String> = emptyList(),
    val cursorPosition: Int = 0,
    val result: String = "",
    val isError: Boolean = false,
    val isExpanded: Boolean = false,
    val isInverse: Boolean = false,
    val angleMode: AngleMode = AngleMode.RAD,
    val showHistory: Boolean = false
) {
    val displayInput: String
        get() = if (tokens.isEmpty()) "0" else tokens.joinToString("")

    val displayInputWithCursor: String
        get() {
            if (tokens.isEmpty()) return "|0"
            val before = tokens.take(cursorPosition).joinToString("")
            val after = tokens.drop(cursorPosition).joinToString("")
            return "$before|$after"
        }
}

enum class AngleMode {
    RAD, DEG
}