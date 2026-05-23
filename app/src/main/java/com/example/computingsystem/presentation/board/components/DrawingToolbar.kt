package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.computingsystem.R
import kotlin.math.roundToInt

@Composable
fun DrawingToolbar(
    strokeWidth: Float,
    strokeColor: Color,
    onStrokeWidthChange: (Float) -> Unit,
    onStrokeColorChange: (Color) -> Unit,
    onClearAll: () -> Unit,
    onUndoLast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Инструменты рисования",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {

                item {
                    // Толщина линии
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Толщина",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "${strokeWidth.roundToInt()}px",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // Предпросмотр толщины
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(strokeWidth.coerceIn(2f, 40f).dp)
                                        .clip(CircleShape)
                                        .background(strokeColor)
                                )
                            }

                            // Слайдер толщины
                            Slider(
                                value = strokeWidth,
                                onValueChange = onStrokeWidthChange,
                                valueRange = 2f..20f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    // Цвет
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Цвет",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Градиентный слайдер для выбора цвета
                        ColorPicker(
                            selectedColor = strokeColor,
                            onColorChange = onStrokeColorChange
                        )
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // Отменить последнее
                        OutlinedButton(
                            onClick = onUndoLast,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_backspace),
                                contentDescription = "Отменить",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Отменить")
                        }

                        // Очистить всё
                        OutlinedButton(
                            onClick = onClearAll,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Очистить",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Очистить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorChange: (Color) -> Unit
) {
    val colors = remember {
        listOf(
            Color.Black,
            Color(0xFF1C1B1F), // Темно-серый
            Color(0xFF6750A4), // Фиолетовый
            Color(0xFF0061A4), // Синий
            Color(0xFF006C4C), // Зеленый
            Color(0xFF984061), // Розовый
            Color(0xFFB3261E), // Красный
            Color(0xFF7D5260), // Коричневый
            Color(0xFFEF6C00), // Оранжевый
            Color(0xFFF9A825), // Желтый
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Предпросмотр выбранного цвета
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )

            Text(
                text = "Выбранный цвет",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Палитра цветов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 3.dp else 1.dp,
                            color = if (color == selectedColor)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(color) }
                )
            }
        }

        // Радужный градиентный слайдер
        var hue by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(selectedColor) {
            // Пытаемся извлечь hue из текущего цвета
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(
                android.graphics.Color.rgb(
                    (selectedColor.red * 255).toInt(),
                    (selectedColor.green * 255).toInt(),
                    (selectedColor.blue * 255).toInt()
                ),
                hsv
            )
            hue = hsv[0]
        }

        Column {
            Text(
                text = "Произвольный цвет",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                        )
                    )
            ) {
                Slider(
                    value = hue,
                    onValueChange = { newHue ->
                        hue = newHue
                        val hsv = floatArrayOf(newHue, 1f, 1f)
                        val rgb = android.graphics.Color.HSVToColor(hsv)
                        onColorChange(
                            Color(
                                android.graphics.Color.red(rgb),
                                android.graphics.Color.green(rgb),
                                android.graphics.Color.blue(rgb)
                            )
                        )
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }
    }
}