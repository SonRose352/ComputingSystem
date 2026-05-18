package com.example.computingsystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.computingsystem.domain.model.Expression
import java.time.format.DateTimeFormatter

@Composable
fun HistoryDialog(
    history: List<Expression>,
    onDismiss: () -> Unit,
    onUseExpression: (Expression) -> Unit,
    onDeleteExpression: (Expression) -> Unit,
    onClearAll: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Заголовок + кнопки
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "История",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Очистить всё
                            IconButton(onClick = onClearAll) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Очистить историю",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }

                            // Закрыть
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        }
                    }

                    Divider()

                    // Список истории
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "История пуста",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(history.reversed()) { expression ->
                                HistoryItem(
                                    expression = expression,
                                    onUse = { onUseExpression(expression) },
                                    onDelete = { onDeleteExpression(expression) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    expression: Expression,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Выражение и результат
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expression.input,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "= ${expression.result}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = expression.createdAt.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Действия (показываются при клике)
        if (showActions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Использовать
                OutlinedButton(
                    onClick = {
                        onUse()
                        showActions = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Использовать", fontSize = 12.sp)
                }

                // Удалить
                OutlinedButton(
                    onClick = {
                        onDelete()
                        showActions = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Удалить", fontSize = 12.sp)
                }
            }
        }

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}