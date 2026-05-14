package com.example.computingsystem.data.repository

import com.example.computingsystem.data.local.dao.ExpressionDao
import com.example.computingsystem.data.mapper.ExpressionMapper
import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.repository.IExpressionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpressionRepositoryImpl @Inject constructor(
    private val dao: ExpressionDao
) : IExpressionRepository {

    override fun getHistory(): Flow<List<Expression>> =
        dao.getAll().map { list -> list.map(ExpressionMapper::toDomain) }

    override suspend fun save(expression: Expression) =
        dao.insert(ExpressionMapper.toEntity(expression))

    override suspend fun deleteAll() = dao.deleteAll()
}