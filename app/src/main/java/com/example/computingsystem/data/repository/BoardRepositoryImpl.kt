package com.example.computingsystem.data.repository

import com.example.computingsystem.data.local.dao.BoardNodeDao
import com.example.computingsystem.data.mapper.BoardNodeMapper
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.repository.IBoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor(
    private val dao: BoardNodeDao
) : IBoardRepository {

    override fun getNodes(boardId: String): Flow<List<BoardNode>> =
        dao.getByBoard(boardId).map { entities ->
            entities.map(BoardNodeMapper::toDomain)
        }

    override suspend fun addNode(node: BoardNode, boardId: String) =
        dao.insert(BoardNodeMapper.toEntity(node, boardId))

    override suspend fun updateNode(node: BoardNode, boardId: String) =
        dao.update(BoardNodeMapper.toEntity(node, boardId))

    override suspend fun deleteNode(nodeId: String) =
        dao.deleteById(nodeId)

    override suspend fun deleteAll(boardId: String) =
        dao.deleteAllByBoard(boardId)
}