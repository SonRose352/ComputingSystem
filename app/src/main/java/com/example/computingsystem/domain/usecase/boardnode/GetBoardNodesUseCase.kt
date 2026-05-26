package com.example.computingsystem.domain.usecase.boardnode

import com.example.computingsystem.domain.repository.IBoardRepository
import javax.inject.Inject

class GetBoardNodesUseCase @Inject constructor(
    private val repository: IBoardRepository
) {
    operator fun invoke() = repository.getNodes()
}