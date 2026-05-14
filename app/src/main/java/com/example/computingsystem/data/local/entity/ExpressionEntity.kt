package com.example.computingsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expressions")
data class ExpressionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val input: String,
    val result: String,
    val createdAtTimestamp: Long
)