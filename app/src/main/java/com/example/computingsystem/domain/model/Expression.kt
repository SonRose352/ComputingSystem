package com.example.computingsystem.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class Expression @RequiresApi(Build.VERSION_CODES.O) constructor(
    val id: Long = 0,
    val input: String,
    val result: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)