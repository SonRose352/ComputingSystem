package com.example.computingsystem.domain.usecase

import com.example.computingsystem.domain.repository.IBoardRepository
import javax.inject.Inject

class DeleteBoardNodeUseCase @Inject constructor(
    private val repository: IBoardRepository
) {
    suspend operator fun invoke(nodeId: String) = repository.deleteNode(nodeId)
}