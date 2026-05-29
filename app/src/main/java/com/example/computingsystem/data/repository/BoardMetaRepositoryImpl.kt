package com.example.computingsystem.data.repository

import androidx.compose.ui.geometry.Offset
import com.example.computingsystem.data.local.dao.BoardDao
import com.example.computingsystem.data.mapper.BoardMapper
import com.example.computingsystem.domain.model.Board
import com.example.computingsystem.domain.repository.IBoardMetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BoardMetaRepositoryImpl @Inject constructor(
    private val dao: BoardDao
) : IBoardMetaRepository {

    override fun getAll(): Flow<List<Board>> =
        dao.getAll().map { list -> list.map(BoardMapper::toDomain) }

    override suspend fun getOrCreate(boardId: String): Board {
        return dao.getById(boardId)?.let(BoardMapper::toDomain)
            ?: Board(id = boardId).also { dao.insert(BoardMapper.toEntity(it)) }
    }

    override suspend fun create(board: Board) =
        dao.insert(BoardMapper.toEntity(board))

    override suspend fun updateViewport(boardId: String, offset: Offset, scale: Float) {
        val existing = dao.getById(boardId) ?: return
        dao.update(existing.copy(
            offsetX = offset.x,
            offsetY = offset.y,
            scale = scale,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun delete(boardId: String) = dao.deleteById(boardId)
}