package com.example.computingsystem.domain.repository

import androidx.compose.ui.geometry.Offset
import com.example.computingsystem.domain.model.Board
import kotlinx.coroutines.flow.Flow

interface IBoardMetaRepository {
    fun getAll(): Flow<List<Board>>
    suspend fun getOrCreate(boardId: String): Board
    suspend fun create(board: Board)
    suspend fun updateViewport(boardId: String, offset: Offset, scale: Float)
    suspend fun delete(boardId: String)
}