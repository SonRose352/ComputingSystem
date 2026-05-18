package com.example.computingsystem.domain.usecase

import com.example.computingsystem.domain.model.BoardNode
import com.example.computingsystem.domain.repository.IBoardRepository
import javax.inject.Inject

class AddBoardNodeUseCase @Inject constructor(
    private val repository: IBoardRepository
) {
    suspend operator fun invoke(node: BoardNode) = repository.addNode(node)
}