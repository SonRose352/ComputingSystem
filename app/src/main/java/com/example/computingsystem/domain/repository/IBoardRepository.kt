package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.BoardNode
import kotlinx.coroutines.flow.Flow

interface IBoardRepository {
    fun getNodes(): Flow<List<BoardNode>>
    suspend fun addNode(node: BoardNode)
    suspend fun updateNode(node: BoardNode)
    suspend fun deleteNode(nodeId: String)
    suspend fun deleteAll()
}