package com.example.computingsystem.domain.usecase.board

import com.example.computingsystem.domain.repository.IBoardMetaRepository
import javax.inject.Inject

class GetBoardsUseCase @Inject constructor(
    private val repository: IBoardMetaRepository
) {
    operator fun invoke() = repository.getAll()
}