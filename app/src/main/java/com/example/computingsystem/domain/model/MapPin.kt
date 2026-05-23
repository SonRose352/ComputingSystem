package com.example.computingsystem.domain.model

import java.util.UUID

data class MapPin(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val x: Float,
    val y: Float,
    val isVisible: Boolean = true
)