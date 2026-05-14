package com.example.computingsystem.presentation.calculator

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

    // Функции, которые должны удаляться целиком
    private val functionTokens = setOf("sin(", "cos(", "tan(", "sqrt(")

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number    -> appendToken(action.value)
            is CalculatorAction.Operator  -> appendToken(action.symbol)
            is CalculatorAction.Decimal   -> appendDecimal()
            is CalculatorAction.Function  -> appendToken(action.symbol)
            is CalculatorAction.Constant  -> appendToken(action.symbol)
            is CalculatorAction.Backspace -> backspace()
            is CalculatorAction.Clear     -> clear()
            is CalculatorAction.Calculate -> calculate()
        }
    }

    private fun appendToken(token: String) {
        _uiState.update { state ->
            val updatedTokens = if (state.tokens.isEmpty() && token.toIntOrNull() != null) {
                // Первое число — заменяет "0"
                listOf(token)
            } else {
                state.tokens + token
            }
            state.copy(tokens = updatedTokens, result = "", isError = false)
        }
    }

    private fun appendDecimal() {
        _uiState.update { state ->
            if (state.tokens.isEmpty()) {
                // Если пусто — начинаем с "0."
                return@update state.copy(tokens = listOf("0", "."))
            }

            val lastToken = state.tokens.last()

            // Точка только после цифры
            if (!lastToken.last().isDigit()) {
                return@update state
            }

            // Находим все токены последнего числа (могут быть раздельные: ["1", "2", "3"])
            val lastNumberTokens = state.tokens.takeLastWhile { token ->
                token.all { it.isDigit() || it == '.' }
            }

            // Если уже есть точка — не добавляем
            if (lastNumberTokens.any { "." in it }) {
                state
            } else {
                state.copy(tokens = state.tokens + ".")
            }
        }
    }

    private fun backspace() {
        _uiState.update { state ->
            if (state.tokens.isEmpty()) return@update state

            val lastToken = state.tokens.last()

            // Если токен — функция (sin(, cos( и т.д.), удаляем целиком
            if (lastToken in functionTokens) {
                state.copy(
                    tokens = state.tokens.dropLast(1),
                    result = "",
                    isError = false
                )
            }
            // Если токен — многосимвольное число/оператор, удаляем посимвольно
            else if (lastToken.length > 1) {
                val trimmed = lastToken.dropLast(1)
                state.copy(
                    tokens = state.tokens.dropLast(1) + trimmed,
                    result = "",
                    isError = false
                )
            }
            // Если токен — один символ, удаляем весь токен
            else {
                val updatedTokens = state.tokens.dropLast(1)
                state.copy(
                    tokens = updatedTokens,
                    result = "",
                    isError = false
                )
            }
        }
    }

    private fun clear() {
        _uiState.value = CalculatorUiState()
    }

    private fun calculate() {
        val tokens = _uiState.value.tokens
        if (tokens.isEmpty()) return

        var input = tokens.joinToString("")

        // Считаем незакрытые скобки
        val openCount = input.count { it == '(' }
        val closeCount = input.count { it == ')' }
        val missing = openCount - closeCount

        // Автоматически закрываем недостающие скобки
        if (missing > 0) {
            input += ")".repeat(missing)
        }

        evaluate(input).fold(
            onSuccess = { result ->
                _uiState.update { it.copy(result = result, isError = false) }
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