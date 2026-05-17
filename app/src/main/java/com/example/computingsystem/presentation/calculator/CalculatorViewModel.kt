package com.example.computingsystem.presentation.calculator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.usecase.EvaluateExpressionUseCase
import com.example.computingsystem.domain.usecase.SaveExpressionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.sql.Date
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val evaluate: EvaluateExpressionUseCase,
    private val save: SaveExpressionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val functionTokens = setOf(
        "sin(", "cos(", "tan(", "sqrt(",
        "asin(", "acos(", "atan(",
        "ln(", "log(",
        "^(-1)"
    )

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number           -> insertToken(action.value)
            is CalculatorAction.Operator         -> insertToken(action.symbol)
            is CalculatorAction.Decimal          -> insertDecimal()
            is CalculatorAction.Function         -> insertToken(action.symbol)
            is CalculatorAction.Constant         -> insertToken(action.symbol)
            is CalculatorAction.Backspace        -> backspace()
            is CalculatorAction.Clear            -> clear()
            is CalculatorAction.Calculate        -> calculate()
            is CalculatorAction.ToggleExpanded   -> toggleExpanded()
            is CalculatorAction.ToggleInverse    -> toggleInverse()
            is CalculatorAction.ToggleAngleMode  -> toggleAngleMode()
            is CalculatorAction.MoveCursorLeft   -> moveCursorLeft()
            is CalculatorAction.MoveCursorRight  -> moveCursorRight()
            is CalculatorAction.SetCursorPosition -> setCursorPosition(action.position)
        }
    }

    private fun toggleExpanded() {
        _uiState.update { it.copy(isExpanded = !it.isExpanded) }
    }

    private fun toggleInverse() {
        _uiState.update { it.copy(isInverse = !it.isInverse) }
    }

    private fun toggleAngleMode() {
        _uiState.update {
            it.copy(
                angleMode = if (it.angleMode == AngleMode.RAD)
                    AngleMode.DEG else AngleMode.RAD
            )
        }
    }

    private fun insertToken(token: String) {
        _uiState.update { state ->
            // Специальная обработка для ^(-1)
            if (token == "^(-1)") {
                return@update handleReciprocal(state)
            }

            val pos = state.cursorPosition
            val newTokens = state.tokens.toMutableList().apply {
                add(pos, token)
            }

            state.copy(
                tokens = newTokens,
                cursorPosition = pos + 1,
                result = "",
                isError = false
            )
        }
    }

    private fun handleReciprocal(state: CalculatorUiState): CalculatorUiState {
        if (state.tokens.isEmpty() || state.cursorPosition == 0) return state

        val pos = state.cursorPosition
        val beforeCursor = state.tokens.take(pos)
        val lastToken = beforeCursor.lastOrNull() ?: return state

        // Если последний токен — число
        if (lastToken.all { it.isDigit() || it == '.' }) {
            val newTokens = state.tokens.toMutableList().apply {
                removeAt(pos - 1)
                add(pos - 1, "$lastToken^(-1)")
            }
            return state.copy(tokens = newTokens, result = "", isError = false)
        }

        return state
    }

    private fun insertDecimal() {
        _uiState.update { state ->
            val pos = state.cursorPosition

            if (pos == 0 || state.tokens.isEmpty()) {
                val newTokens = listOf("0", ".")
                return@update state.copy(tokens = newTokens, cursorPosition = 2)
            }

            val beforeCursor = state.tokens.take(pos)
            val lastToken = beforeCursor.lastOrNull() ?: return@update state

            if (!lastToken.last().isDigit()) {
                return@update state
            }

            val lastNumberTokens = beforeCursor.takeLastWhile { token ->
                token.all { it.isDigit() || it == '.' }
            }

            if (lastNumberTokens.any { "." in it }) {
                state
            } else {
                val newTokens = state.tokens.toMutableList().apply {
                    add(pos, ".")
                }
                state.copy(tokens = newTokens, cursorPosition = pos + 1)
            }
        }
    }

    private fun backspace() {
        _uiState.update { state ->
            if (state.tokens.isEmpty() || state.cursorPosition == 0) return@update state

            val pos = state.cursorPosition
            val tokenToDelete = state.tokens[pos - 1]

            if (tokenToDelete in functionTokens) {
                // Удаляем весь токен функции
                val newTokens = state.tokens.toMutableList().apply {
                    removeAt(pos - 1)
                }
                state.copy(
                    tokens = newTokens,
                    cursorPosition = pos - 1,
                    result = "",
                    isError = false
                )
            } else if (tokenToDelete.length > 1) {
                // Удаляем последний символ из токена
                val trimmed = tokenToDelete.dropLast(1)
                val newTokens = state.tokens.toMutableList().apply {
                    set(pos - 1, trimmed)
                }
                state.copy(
                    tokens = newTokens,
                    result = "",
                    isError = false
                )
            } else {
                // Удаляем весь односимвольный токен
                val newTokens = state.tokens.toMutableList().apply {
                    removeAt(pos - 1)
                }
                state.copy(
                    tokens = newTokens,
                    cursorPosition = pos - 1,
                    result = "",
                    isError = false
                )
            }
        }
    }

    private fun clear() {
        _uiState.update {
            it.copy(tokens = emptyList(), cursorPosition = 0, result = "", isError = false)
        }
    }

    private fun moveCursorLeft() {
        _uiState.update { state ->
            if (state.cursorPosition > 0) {
                state.copy(cursorPosition = state.cursorPosition - 1)
            } else state
        }
    }

    private fun moveCursorRight() {
        _uiState.update { state ->
            if (state.cursorPosition < state.tokens.size) {
                state.copy(cursorPosition = state.cursorPosition + 1)
            } else state
        }
    }

    private fun setCursorPosition(position: Int) {
        _uiState.update { state ->
            val validPosition = position.coerceIn(0, state.tokens.size)
            state.copy(cursorPosition = validPosition)
        }
    }

    private fun calculate() {
        val tokens = _uiState.value.tokens
        if (tokens.isEmpty()) return

        var input = tokens.joinToString("")

        val openCount = input.count { it == '(' }
        val closeCount = input.count { it == ')' }
        val missing = openCount - closeCount

        if (missing > 0) {
            input += ")".repeat(missing)
        }

        evaluate(input, _uiState.value.angleMode).fold(
            onSuccess = { result ->
                _uiState.update {
                    it.copy(
                        result = result,
                        isError = false,
                        // Курсор в конец после вычисления
                        cursorPosition = it.tokens.size
                    )
                }
                viewModelScope.launch {
                    save(Expression(input = input, result = result))
                }
            },
            onFailure = {
                _uiState.update { it.copy(isError = true, result = "Ошибка") }
            }
        )
    }
}