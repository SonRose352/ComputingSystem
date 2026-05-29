package com.example.computingsystem.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "map_pins",
    foreignKeys = [
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["id"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boardId")]
)
data class MapPinEntity(
    @PrimaryKey
    val id: String,
    val boardId: String,
    val name: String,
    val x: Float,
    val y: Float,
    val isVisible: Boolean
)