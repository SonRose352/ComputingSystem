package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size

@Composable
fun TextNodeView(
    node: BoardNode.TextNode,
    offset: Offset,
    scale: Float,
    isActive: Boolean,
    onTextChange: (String) -> Unit,
    onClick: () -> Unit,
    onMoveFinished: (Position) -> Unit,
    onResizeFinished: (Size) -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
) {
    var text by remember(node.id) { mutableStateOf(node.text) }
    var localPosition by remember(node.id) { mutableStateOf(node.position) }
    var localSize by remember(node.id) { mutableStateOf(node.size) }
    val density = LocalDensity.current

    val scaledPadding   = (14 * scale).dp
    val scaledFont      = (14 * scale).sp
    val scaledBorder    = (2  * scale).dp
    val scaledHandle    = (24 * scale).dp
    val scaledCorner    = (8  * scale).dp
    val scaledElevation = (4  * scale).dp
    val scaledMenuIcon  = (32 * scale).dp

    LaunchedEffect(node.position) { localPosition = node.position }
    LaunchedEffect(node.size)     { localSize = node.size }
    LaunchedEffect(text) {
        if (text != node.text) {
            kotlinx.coroutines.delay(300)
            onTextChange(text)
        }
    }

    val screenX = with(density) { (localPosition.x * scale + offset.x).toDp() }
    val screenY = with(density) { (localPosition.y * scale + offset.y).toDp() }

    Box(
        modifier = Modifier
            .offset(screenX, screenY)
            .width(with(density)  { (localSize.width  * scale).toDp() })
            .height(with(density) { (localSize.height * scale).toDp() })
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isActive) scaledBorder else 0.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(scaledCorner)
                )
                .pointerInput(isActive) { detectTapGestures { onClick() } }
                .then(
                    if (isActive) Modifier.pointerInput(scale) {
                        detectDragGestures(onDragEnd = { onMoveFinished(localPosition) }) { change, dragAmount ->
                            change.consume()
                            localPosition = Position(
                                localPosition.x + dragAmount.x / scale,
                                localPosition.y + dragAmount.y / scale
                            )
                        }
                    } else Modifier
                ),
            shape = RoundedCornerShape(scaledCorner),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(scaledElevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    enabled = isActive,
                    textStyle = TextStyle(
                        fontSize = scaledFont,
                        lineHeight = scaledFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = scaledPadding,
                            end = scaledMenuIcon + scaledPadding / 4,
                            top = scaledPadding,
                            bottom = scaledPadding
                        ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.text_node_placeholder),
                                    style = TextStyle(
                                        fontSize = scaledFont, lineHeight = scaledFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NodeOptionsMenu(
                        onCopy = onCopy,
                        onDelete = onDelete,
                        iconSizeDp = scaledMenuIcon
                    )
                }
            }
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(scaledHandle)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(scale) {
                        detectDragGestures(onDragEnd = { onResizeFinished(localSize) }) { change, dragAmount ->
                            change.consume()
                            localSize = Size(
                                (localSize.width  + dragAmount.x / scale).coerceAtLeast(100f),
                                (localSize.height + dragAmount.y / scale).coerceAtLeast(50f)
                            )
                        }
                    }
            )
        }
    }
}