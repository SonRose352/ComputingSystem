package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.computingsystem.R
import kotlin.math.roundToInt

@Composable
fun ContextAddMenu(
    screenOffset: Offset,
    onDismiss: () -> Unit,
    onSelectText: () -> Unit,
    onSelectMath: () -> Unit,
    onSelectDrawing: () -> Unit,
) {
    val density = LocalDensity.current

    Popup(
        offset = IntOffset(
            x = screenOffset.x.roundToInt(),
            y = screenOffset.y.roundToInt()
        ),
        properties = PopupProperties(focusable = true),
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.width(220.dp)) {
                DropdownMenuItem(
                    text = { Text("Текстовое поле") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_text_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onSelectText
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Математическое выражение") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_math_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onSelectMath
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Рисунок") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_drawing_node),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onSelectDrawing
                )
            }
        }
    }
}