package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.Expression
import kotlinx.coroutines.flow.Flow

interface IExpressionRepository {
    fun getHistory(): Flow<List<Expression>>
    suspend fun save(expression: Expression)
    suspend fun deleteAll()
}