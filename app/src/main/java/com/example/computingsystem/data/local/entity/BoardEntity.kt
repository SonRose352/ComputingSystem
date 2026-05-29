package com.example.computingsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float,
    val createdAt: Long,
    val updatedAt: Long
)