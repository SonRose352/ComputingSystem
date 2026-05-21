package com.example.computingsystem.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MathKeyboardMode {
    BASIC,
    ADVANCED,
    TRIG
}

@Composable
fun MathKeyboard(
    tokens: List<String> = emptyList(),
    cursorPosition: Int = 0,
    onInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onSetCursor: (Int) -> Unit = {},
    onCalculate: () -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    var currentMode by remember { mutableStateOf(MathKeyboardMode.BASIC) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Строка отображения выражения с кликабельным курсором
            ExpressionPreview(
                tokens = tokens,
                cursorPosition = cursorPosition,
                onSetCursor = onSetCursor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Верхняя панель управления (очистить / backspace / стрелки)
            TopControlBar(
                onClear = onClear,
                onBackspace = onBackspace,
                onMoveCursorLeft = onMoveCursorLeft,
                onMoveCursorRight = onMoveCursorRight,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Основная клавиатура с переключателями режимов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Левая панель переключения режимов
                ModeSelector(
                    currentMode = currentMode,
                    onModeChange = { currentMode = it },
                    modifier = Modifier.weight(0.15f)
                )

                // Сетка кнопок (4x4)
                KeyboardGrid(
                    mode = currentMode,
                    onInput = onInput,
                    onCalculate = onCalculate,
                    modifier = Modifier.weight(0.85f)
                )
            }
        }
    }
}

@Composable
private fun ExpressionPreview(
    tokens: List<String>,
    cursorPosition: Int,
    onSetCursor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    Surface(
        modifier = modifier
            .height(40.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (tokens.isEmpty()) {
                item {
                    Text(
                        text = if (cursorVisible) "|" else " ",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onSetCursor(0) }
                    )
                }

                item {
                    Text(
                        text = "Введите выражение...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(tokens.size) { index ->
                    // Курсор перед токеном
                    if (index == cursorPosition) {
                        Text(
                            text = if (cursorVisible) "|" else " ",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onSetCursor(index) }
                        )
                    }

                    // Сам токен
                    Text(
                        text = tokens[index],
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onSetCursor(index + 1) }
                    )
                }

                // Курсор в конце
                if (cursorPosition == tokens.size) {
                    item {
                        Text(
                            text = if (cursorVisible) "|" else " ",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onSetCursor(tokens.size) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopControlBar(
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Очистить всё
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Очистить",
                tint = MaterialTheme.colorScheme.error
            )
        }

        // Удалить символ перед курсором
        OutlinedButton(
            onClick = onBackspace,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("⌫", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Стрелка влево
        OutlinedButton(
            onClick = onMoveCursorLeft,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Курсор влево"
            )
        }

        // Стрелка вправо
        OutlinedButton(
            onClick = onMoveCursorRight,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Курсор вправо"
            )
        }
    }
}

@Composable
private fun ModeSelector(
    currentMode: MathKeyboardMode,
    onModeChange: (MathKeyboardMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModeButton(
            label = "123",
            isSelected = currentMode == MathKeyboardMode.BASIC,
            onClick = { onModeChange(MathKeyboardMode.BASIC) },
            modifier = Modifier.weight(1f)
        )
        ModeButton(
            label = "f(x)",
            isSelected = currentMode == MathKeyboardMode.ADVANCED,
            onClick = { onModeChange(MathKeyboardMode.ADVANCED) },
            modifier = Modifier.weight(1f)
        )
        ModeButton(
            label = "sin",
            isSelected = currentMode == MathKeyboardMode.TRIG,
            onClick = { onModeChange(MathKeyboardMode.TRIG) },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KeyboardGrid(
    mode: MathKeyboardMode,
    onInput: (String) -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttons = getButtonsForMode(mode)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        buttons.chunked(4).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { buttonData ->
                    if (buttonData != null) {
                        // Кнопка "=" — вычисляет результат, не вставляет символ
                        if (buttonData.value == "=") {
                            KeyboardButton(
                                label = buttonData.label,
                                value = buttonData.value,
                                onInput = { onCalculate() },
                                isEquals = true,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            KeyboardButton(
                                label = buttonData.label,
                                value = buttonData.value,
                                onInput = onInput,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

private data class ButtonData(
    val label: String,
    val value: String
)

private fun getButtonsForMode(mode: MathKeyboardMode): List<ButtonData?> {
    return when (mode) {
        MathKeyboardMode.BASIC -> listOf(
            ButtonData("7", "7"),
            ButtonData("8", "8"),
            ButtonData("9", "9"),
            ButtonData("÷", "÷"),

            ButtonData("4", "4"),
            ButtonData("5", "5"),
            ButtonData("6", "6"),
            ButtonData("×", "×"),

            ButtonData("1", "1"),
            ButtonData("2", "2"),
            ButtonData("3", "3"),
            ButtonData("−", "-"),

            ButtonData("0", "0"),
            ButtonData(".", "."),
            ButtonData("=", "="),
            ButtonData("+", "+")
        )

        MathKeyboardMode.ADVANCED -> listOf(
            ButtonData("(", "("),
            ButtonData(")", ")"),
            ButtonData("^", "^"),
            ButtonData("√", "sqrt("),

            ButtonData("π", "π"),
            ButtonData("e", "e"),
            ButtonData("%", "%"),
            ButtonData("!", "!"),

            ButtonData("x⁻¹", "^(-1)"),
            ButtonData("xʸ", "^"),
            ButtonData("ln", "ln("),
            ButtonData("lg", "lg("),

            null, null, null, null
        )

        MathKeyboardMode.TRIG -> listOf(
            ButtonData("sin", "sin("),
            ButtonData("cos", "cos("),
            ButtonData("tan", "tan("),
            null,

            ButtonData("asin", "asin("),
            ButtonData("acos", "acos("),
            ButtonData("atan", "atan("),
            null,

            ButtonData("sinh", "sinh("),
            ButtonData("cosh", "cosh("),
            ButtonData("tanh", "tanh("),
            null,

            null, null, null, null
        )
    }
}

@Composable
private fun KeyboardButton(
    label: String,
    value: String,
    onInput: (String) -> Unit,
    isEquals: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onInput(value) },
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEquals)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isEquals) FontWeight.Bold else FontWeight.Normal,
            color = if (isEquals)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}