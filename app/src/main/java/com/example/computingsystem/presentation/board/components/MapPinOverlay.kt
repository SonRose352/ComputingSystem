package com.example.computingsystem.presentation.board.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import com.example.computingsystem.R
import com.example.computingsystem.domain.model.MapPin

@Composable
fun MapPinOverlay(
    pins: List<MapPin>,
    canvasOffset: Offset,
    scale: Float
) {
    val density = LocalDensity.current

    pins.filter { it.isVisible }.forEach { pin ->
        val screenX = with(density) { (pin.x * scale + canvasOffset.x).toDp() }
        val screenY = with(density) { (pin.y * scale + canvasOffset.y).toDp() }

        Column(
            modifier = Modifier.offset(screenX - 16.dp, screenY - 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pin.name,
                fontSize = (12 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(R.drawable.ic_map_pin),
                contentDescription = pin.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size((32 * scale).dp)
            )
        }
    }
}