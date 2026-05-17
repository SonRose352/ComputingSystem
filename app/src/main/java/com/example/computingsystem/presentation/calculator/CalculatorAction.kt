package com.example.computingsystem.presentation.calculator

sealed class CalculatorAction {
    data class Number(val value: String)    : CalculatorAction()
    data class Operator(val symbol: String) : CalculatorAction()
    data class Function(val symbol: String) : CalculatorAction()
    data class Constant(val symbol: String) : CalculatorAction()
    data object Decimal                     : CalculatorAction()
    data object Backspace                   : CalculatorAction()
    data object Clear                       : CalculatorAction()
    data object Calculate                   : CalculatorAction()
    data object ToggleExpanded              : CalculatorAction()
    data object ToggleInverse               : CalculatorAction()
    data object ToggleAngleMode             : CalculatorAction()
    data object MoveCursorLeft              : CalculatorAction()
    data object MoveCursorRight             : CalculatorAction()
    data class SetCursorPosition(val position: Int) : CalculatorAction()
}