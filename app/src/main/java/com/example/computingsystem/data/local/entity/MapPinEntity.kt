package com.example.computingsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_pins")
data class MapPinEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val isVisible: Boolean
)