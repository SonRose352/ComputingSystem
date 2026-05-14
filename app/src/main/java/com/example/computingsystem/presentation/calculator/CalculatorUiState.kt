package com.example.computingsystem.presentation.calculator

data class CalculatorUiState(
    val tokens: List<String> = emptyList(),
    val result: String = "",
    val isError: Boolean = false
) {
    val displayInput: String
        get() = if (tokens.isEmpty()) "0" else tokens.joinToString("")
}