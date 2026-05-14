package com.example.computingsystem.presentation.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 8.dp)
    ) {
        // ── Дисплей ──────────────────────────────────────────────
        DisplayPanel(
            input = state.displayInput,
            result = state.result,
            isError = state.isError,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ── Клавиатура ───────────────────────────────────────────
        KeypadPanel(
            onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ── Дисплей ─────────────────────────────────────────────────────

@Composable
private fun DisplayPanel(
    input: String,
    result: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Строка ввода
        Text(
            text = input,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Light,
                fontSize = 40.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Строка результата
        if (result.isNotEmpty()) {
            Text(
                text = if (isError) result else "= $result",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ── Клавиатура ───────────────────────────────────────────────────

@Composable
private fun KeypadPanel(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Ряд функций
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("sin(",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("sin(")) }
            CalcButton("cos(",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("cos(")) }
            CalcButton("tan(",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("tan(")) }
            CalcButton("√(",    Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("sqrt(")) }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("xʸ",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator("^")) }
            CalcButton("(",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator("(")) }
            CalcButton(")",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator(")")) }
            CalcButton("π",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Constant("π")) }
        }

        // Ряды цифр и операторов
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("C",  Modifier.weight(1f), ButtonStyle.Utility) { onAction(CalculatorAction.Clear) }
            CalcButton("⌫",  Modifier.weight(1f), ButtonStyle.Utility) { onAction(CalculatorAction.Backspace) }
            CalcButton("%",  Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("%")) }
            CalcButton("÷",  Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("÷")) }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("7",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("7")) }
            CalcButton("8",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("8")) }
            CalcButton("9",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("9")) }
            CalcButton("×",  Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("×")) }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("4",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("4")) }
            CalcButton("5",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("5")) }
            CalcButton("6",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("6")) }
            CalcButton("−",  Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("-")) }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("1",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("1")) }
            CalcButton("2",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("2")) }
            CalcButton("3",  Modifier.weight(1f)) { onAction(CalculatorAction.Number("3")) }
            CalcButton("+",  Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("+")) }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("0",  Modifier.weight(2f)) { onAction(CalculatorAction.Number("0")) }
            CalcButton(".",  Modifier.weight(1f)) { onAction(CalculatorAction.Decimal) }
            CalcButton("=",  Modifier.weight(1f), ButtonStyle.Equals) { onAction(CalculatorAction.Calculate) }
        }
    }
}

// ── Переиспользуемая кнопка ──────────────────────────────────────

private enum class ButtonStyle { Default, Operator, Function, Utility, Equals }

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
    onClick: () -> Unit
) {
    val containerColor = when (style) {
        ButtonStyle.Default  -> MaterialTheme.colorScheme.surfaceVariant
        ButtonStyle.Operator -> MaterialTheme.colorScheme.secondaryContainer
        ButtonStyle.Function -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonStyle.Utility  -> MaterialTheme.colorScheme.errorContainer
        ButtonStyle.Equals   -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when (style) {
        ButtonStyle.Equals   -> MaterialTheme.colorScheme.onPrimary
        ButtonStyle.Utility  -> MaterialTheme.colorScheme.onErrorContainer
        else                 -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = if (style == ButtonStyle.Equals) FontWeight.Bold else FontWeight.Normal
        )
    }
}