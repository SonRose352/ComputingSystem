package com.example.computingsystem.presentation.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.R
import com.example.computingsystem.presentation.components.HistoryDialog
import com.example.computingsystem.presentation.settings.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 8.dp)
    ) {
        DisplayPanel(
            input = state.displayInput,
            result = state.result,
            isError = state.isError,
            isExpanded = state.isExpanded,
            onToggleExpanded = { viewModel.onAction(CalculatorAction.ToggleExpanded) },
            onShowHistory = { viewModel.onAction(CalculatorAction.ShowHistory) },
            onCursorPositionChange = { pos ->
                viewModel.onAction(CalculatorAction.SetCursorPosition(pos))
            },
            tokens = state.tokens,
            cursorPosition = state.cursorPosition,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state.isExpanded) {
            ExpandedKeypad(
                onAction = viewModel::onAction,
                isInverse = state.isInverse,
                angleMode = state.angleMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else {
            BasicKeypad(
                onAction = viewModel::onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    if (state.showHistory) {
        HistoryDialog(
            history = history,
            language = settings.language,
            onDismiss = { viewModel.onAction(CalculatorAction.HideHistory) },
            onUseExpression = { expr ->
                viewModel.onAction(CalculatorAction.UseFromHistory(expr.result))
            },
            onDeleteExpression = { expr ->
                viewModel.onAction(CalculatorAction.DeleteFromHistory(expr))
            },
            onClearAll = { viewModel.onAction(CalculatorAction.ClearHistory) }
        )
    }
}

@Composable
private fun DisplayPanel(
    input: String,
    result: String,
    isError: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onShowHistory: () -> Unit,
    onCursorPositionChange: (Int) -> Unit,
    tokens: List<String>,
    cursorPosition: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Кнопка переключения режима
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка расширения
            Icon(
                painter = painterResource(
                    if (isExpanded) R.drawable.ic_resize_in else R.drawable.ic_resize_out
                ),
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onToggleExpanded)
                    .padding(6.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Кнопка истории
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = "История",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onShowHistory)
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Ввод с курсором
        ClickableInputDisplay(
            tokens = tokens,
            cursorPosition = cursorPosition,
            onCursorPositionChange = onCursorPositionChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Результат
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

@Composable
private fun ClickableInputDisplay(
    tokens: List<String>,
    cursorPosition: Int,
    onCursorPositionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            cursorVisible = !cursorVisible
        }
    }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (tokens.isEmpty()) {
            Text(
                text = if (cursorVisible) "|" else " ",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onCursorPositionChange(0) }
            )
            Text(
                text = "0",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 40.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        } else {
            tokens.forEachIndexed { index, token ->
                // Курсор перед токеном
                if (index == cursorPosition) {
                    Text(
                        text = if (cursorVisible) "|" else " ",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onCursorPositionChange(index) }
                    )
                }

                // Сам токен
                Text(
                    text = token,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 40.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable {
                        onCursorPositionChange(index + 1)
                    }
                )
            }

            // Курсор в конце
            if (cursorPosition == tokens.size) {
                Text(
                    text = if (cursorVisible) "|" else " ",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onCursorPositionChange(tokens.size) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// БАЗОВАЯ КЛАВИАТУРА
// ══════════════════════════════════════════════════════════════

@Composable
private fun BasicKeypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton("C",  Modifier.weight(1f), ButtonStyle.Utility)  { onAction(CalculatorAction.Clear) }
            CalcButton("⌫",  Modifier.weight(1f), ButtonStyle.Utility)  { onAction(CalculatorAction.Backspace) }
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

// ══════════════════════════════════════════════════════════════
// РАСШИРЕННАЯ КЛАВИАТУРА
// ══════════════════════════════════════════════════════════════

@Composable
private fun ExpandedKeypad(
    onAction: (CalculatorAction) -> Unit,
    isInverse: Boolean,
    angleMode: AngleMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Ряд 1: тригонометрия + переключатели
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton(
                if (isInverse) "sin⁻¹" else "sin",
                Modifier.weight(1f),
                ButtonStyle.Function
            ) { onAction(CalculatorAction.Function(if (isInverse) "asin(" else "sin(")) }

            CalcButton(
                if (isInverse) "cos⁻¹" else "cos",
                Modifier.weight(1f),
                ButtonStyle.Function
            ) { onAction(CalculatorAction.Function(if (isInverse) "acos(" else "cos(")) }

            CalcButton(
                if (isInverse) "tan⁻¹" else "tan",
                Modifier.weight(1f),
                ButtonStyle.Function
            ) { onAction(CalculatorAction.Function(if (isInverse) "atan(" else "tan(")) }

            CalcButton(
                angleMode.name,
                Modifier.weight(1f),
                ButtonStyle.Toggle
            ) { onAction(CalculatorAction.ToggleAngleMode) }

            CalcButton(
                "INV",
                Modifier.weight(1f),
                if (isInverse) ButtonStyle.ToggleActive else ButtonStyle.Toggle
            ) { onAction(CalculatorAction.ToggleInverse) }
        }

        // Ряд 2: логарифмы, скобки, корень
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("lg",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("lg(")) }
            CalcButton("ln",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("ln(")) }
            CalcButton("(",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator("(")) }
            CalcButton(")",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator(")")) }
            CalcButton("√",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("sqrt(")) }
        }

        // Ряд 3: степень, C, backspace, %, ÷
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("xʸ",  Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator("^")) }
            CalcButton("C",   Modifier.weight(1f), ButtonStyle.Utility)  { onAction(CalculatorAction.Clear) }
            CalcButton("⌫",   Modifier.weight(1f), ButtonStyle.Utility)  { onAction(CalculatorAction.Backspace) }
            CalcButton("%",   Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("%")) }
            CalcButton("÷",   Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("÷")) }
        }

        // Ряд 4: факториал, 7, 8, 9, ×
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("!",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Operator("!")) }
            CalcButton("7",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("7")) }
            CalcButton("8",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("8")) }
            CalcButton("9",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("9")) }
            CalcButton("×",   Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("×")) }
        }

        // Ряд 5: 1/x, 4, 5, 6, −
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("x⁻¹", Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Function("^(-1)")) }
            CalcButton("4",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("4")) }
            CalcButton("5",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("5")) }
            CalcButton("6",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("6")) }
            CalcButton("−",   Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("-")) }
        }

        // Ряд 6: π, 1, 2, 3, +
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("π",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Constant("π")) }
            CalcButton("1",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("1")) }
            CalcButton("2",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("2")) }
            CalcButton("3",   Modifier.weight(1f)) { onAction(CalculatorAction.Number("3")) }
            CalcButton("+",   Modifier.weight(1f), ButtonStyle.Operator) { onAction(CalculatorAction.Operator("+")) }
        }

        // Ряд 7: e, 0, ., =
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("e",   Modifier.weight(1f), ButtonStyle.Function) { onAction(CalculatorAction.Constant("e")) }
            CalcButton("0",   Modifier.weight(2f)) { onAction(CalculatorAction.Number("0")) }
            CalcButton(".",   Modifier.weight(1f)) { onAction(CalculatorAction.Decimal) }
            CalcButton("=",   Modifier.weight(1f), ButtonStyle.Equals) { onAction(CalculatorAction.Calculate) }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// КНОПКА
// ══════════════════════════════════════════════════════════════

private enum class ButtonStyle {
    Default, Operator, Function, Utility, Equals, Toggle, ToggleActive
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
    onClick: () -> Unit
) {
    val containerColor = when (style) {
        ButtonStyle.Default      -> MaterialTheme.colorScheme.surfaceVariant
        ButtonStyle.Operator     -> MaterialTheme.colorScheme.secondaryContainer
        ButtonStyle.Function     -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonStyle.Utility      -> MaterialTheme.colorScheme.errorContainer
        ButtonStyle.Equals       -> MaterialTheme.colorScheme.primary
        ButtonStyle.Toggle       -> MaterialTheme.colorScheme.surfaceVariant
        ButtonStyle.ToggleActive -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when (style) {
        ButtonStyle.Equals       -> MaterialTheme.colorScheme.onPrimary
        ButtonStyle.Utility      -> MaterialTheme.colorScheme.onErrorContainer
        ButtonStyle.ToggleActive -> MaterialTheme.colorScheme.primary
        else                     -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = if (style == ButtonStyle.Equals) FontWeight.Bold else FontWeight.Normal
        )
    }
}