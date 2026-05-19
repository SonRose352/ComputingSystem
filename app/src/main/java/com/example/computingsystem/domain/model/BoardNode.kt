package com.example.computingsystem.domain.model

import java.util.UUID

sealed class BoardNode {
    abstract val id: String
    abstract val position: Position
    abstract val size: Size

    data class TextNode(
        override val id: String = UUID.randomUUID().toString(),
        override val position: Position,
        override val size: Size = Size(600f, 300f),
        val text: String = ""
    ) : BoardNode()

    data class MathNode(
        override val id: String = UUID.randomUUID().toString(),
        override val position: Position,
        override val size: Size = Size(900f, 450f),
        val expression: String = "",
        val result: String = ""
    ) : BoardNode()
}

data class Position(
    val x: Float,
    val y: Float
)

data class Size(
    val width: Float,
    val height: Float
)