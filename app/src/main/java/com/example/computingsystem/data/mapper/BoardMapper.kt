package com.example.computingsystem.data.mapper

import androidx.compose.ui.geometry.Offset
import com.example.computingsystem.data.local.entity.BoardEntity
import com.example.computingsystem.domain.model.Board

object BoardMapper {

    fun toDomain(entity: BoardEntity) = Board(
        id = entity.id,
        name = entity.name,
        offset = Offset(entity.offsetX, entity.offsetY),
        scale = entity.scale,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Board) = BoardEntity(
        id = domain.id,
        name = domain.name,
        offsetX = domain.offset.x,
        offsetY = domain.offset.y,
        scale = domain.scale,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )
}