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

    override fun getNodes(): Flow<List<BoardNode>> =
        dao.getAll().map { entities ->
            entities.map(BoardNodeMapper::toDomain)
        }

    override suspend fun addNode(node: BoardNode) =
        dao.insert(BoardNodeMapper.toEntity(node))

    override suspend fun updateNode(node: BoardNode) =
        dao.update(BoardNodeMapper.toEntity(node))

    override suspend fun deleteNode(nodeId: String) =
        dao.deleteById(nodeId)

    override suspend fun deleteAll() = dao.deleteAll()
}