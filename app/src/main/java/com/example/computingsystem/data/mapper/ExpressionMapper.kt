package com.example.computingsystem.data.mapper

import com.example.computingsystem.data.local.entity.ExpressionEntity
import com.example.computingsystem.domain.model.Expression
import java.time.LocalDateTime
import java.time.ZoneOffset

object ExpressionMapper {

    fun toDomain(entity: ExpressionEntity) = Expression(
        id = entity.id,
        input = entity.input,
        result = entity.result,
        createdAt = LocalDateTime.ofEpochSecond(
            entity.createdAtTimestamp, 0, ZoneOffset.UTC
        )
    )

    fun toEntity(domain: Expression) = ExpressionEntity(
        id = domain.id,
        input = domain.input,
        result = domain.result,
        createdAtTimestamp = domain.createdAt.toEpochSecond(ZoneOffset.UTC)
    )
}