package com.example.computingsystem.domain.model

import androidx.compose.ui.geometry.Offset
import java.util.UUID

data class Board(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Доска",
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)