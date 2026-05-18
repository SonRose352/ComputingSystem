package com.example.computingsystem.presentation.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.presentation.calculator.CalculatorAction
import com.example.computingsystem.presentation.calculator.CalculatorViewModel
import com.example.computingsystem.presentation.components.HistoryDialog
import com.example.computingsystem.R


@Composable
fun BoardScreen(
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showHistory by remember { mutableStateOf(false) }

    val history by viewModel.history.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Бесконечный Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.1f, 5f)
                        offset += pan
                    }
                }
        ) {
            translate(offset.x, offset.y) {
                // Сетка
                val gridSize = 50f * scale
                val startX = (-offset.x / gridSize).toInt() - 1
                val endX = ((size.width - offset.x) / gridSize).toInt() + 1
                val startY = (-offset.y / gridSize).toInt() - 1
                val endY = ((size.height - offset.y) / gridSize).toInt() + 1

                val gridColor = Color.Gray.copy(alpha = 0.2f)

                // Вертикальные линии
                for (i in startX..endX) {
                    drawLine(
                        color = gridColor,
                        start = Offset(i * gridSize, startY * gridSize),
                        end = Offset(i * gridSize, endY * gridSize),
                        strokeWidth = 1f
                    )
                }

                // Горизонтальные линии
                for (i in startY..endY) {
                    drawLine(
                        color = gridColor,
                        start = Offset(startX * gridSize, i * gridSize),
                        end = Offset(endX * gridSize, i * gridSize),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Кнопка истории в правом верхнем углу
        Icon(
            painter = painterResource(R.drawable.ic_history),
            contentDescription = "История",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
                .clickable { showHistory = true }
                .padding(6.dp)
        )
    }

    // Диалог истории
    if (showHistory) {
        HistoryDialog(
            history = history,
            onDismiss = { showHistory = false },
            onUseExpression = { expr ->
                // Пока ничего не делаем — функционал добавления на доску будет позже
                showHistory = false
            },
            onDeleteExpression = { expr ->
                viewModel.onAction(CalculatorAction.DeleteFromHistory(expr))
            },
            onClearAll = {
                viewModel.onAction(CalculatorAction.ClearHistory)
            }
        )
    }
}