package com.example.computingsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "board_nodes")
data class BoardNodeEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val content: String,
    val updatedAt: Long
)