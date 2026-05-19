package com.example.computingsystem.data.mapper

import com.example.computingsystem.data.local.entity.BoardNodeEntity
import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.model.Position
import com.example.computingsystem.domain.model.Size

object BoardNodeMapper {

    fun toDomain(entity: BoardNodeEntity): BoardNode {
        return when (entity.type) {
            "text" -> {
                BoardNode.TextNode(
                    id = entity.id,
                    position = Position(entity.x, entity.y),
                    size = Size(entity.width, entity.height),
                    text = entity.content
                )
            }
            "math" -> {
                val parts = entity.content.split("|||")
                BoardNode.MathNode(
                    id = entity.id,
                    position = Position(entity.x, entity.y),
                    size = Size(entity.width, entity.height),
                    expression = parts.getOrNull(0) ?: "",
                    result = parts.getOrNull(1) ?: ""
                )
            }
            else -> throw IllegalArgumentException("Unknown node type: ${entity.type}")
        }
    }

    fun toEntity(domain: BoardNode): BoardNodeEntity {
        return when (domain) {
            is BoardNode.TextNode -> BoardNodeEntity(
                id = domain.id,
                type = "text",
                x = domain.position.x,
                y = domain.position.y,
                width = domain.size.width,
                height = domain.size.height,
                content = domain.text,
                updatedAt = System.currentTimeMillis()
            )
            is BoardNode.MathNode -> BoardNodeEntity(
                id = domain.id,
                type = "math",
                x = domain.position.x,
                y = domain.position.y,
                width = domain.size.width,
                height = domain.size.height,
                content = "${domain.expression}|||${domain.result}",
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}