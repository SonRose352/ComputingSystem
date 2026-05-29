package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.BoardNode
import kotlinx.coroutines.flow.Flow

interface IBoardRepository {
    fun getNodes(boardId: String): Flow<List<BoardNode>>
    suspend fun addNode(node: BoardNode, boardId: String)
    suspend fun updateNode(node: BoardNode, boardId: String)
    suspend fun deleteNode(nodeId: String)
    suspend fun deleteAll(boardId: String)
}