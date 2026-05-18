package com.example.computingsystem.domain.model

import java.util.UUID

sealed class BoardNode {
    abstract val id: String
    abstract val position: Position

    data class TextNode(
        override val id: String = UUID.randomUUID().toString(),
        override val position: Position,
        val text: String = ""
    ) : BoardNode()

    data class MathNode(
        override val id: String = UUID.randomUUID().toString(),
        override val position: Position,
        val expression: String = "",
        val result: String = ""
    ) : BoardNode()
}

data class Position(
    val x: Float,
    val y: Float
)