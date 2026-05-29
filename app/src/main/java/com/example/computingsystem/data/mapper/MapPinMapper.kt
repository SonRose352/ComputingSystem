package com.example.computingsystem.data.mapper

import com.example.computingsystem.data.local.entity.MapPinEntity
import com.example.computingsystem.domain.model.MapPin

object MapPinMapper {

    fun toDomain(entity: MapPinEntity) = MapPin(
        id = entity.id,
        name = entity.name,
        x = entity.x,
        y = entity.y,
        isVisible = entity.isVisible
    )

    fun toEntity(domain: MapPin, boardId: String) = MapPinEntity(
        id = domain.id,
        boardId = boardId,
        name = domain.name,
        x = domain.x,
        y = domain.y,
        isVisible = domain.isVisible
    )
}