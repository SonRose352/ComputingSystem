package com.example.computingsystem.domain.usecase.board

import com.example.computingsystem.domain.model.Board
import com.example.computingsystem.domain.repository.IBoardMetaRepository
import javax.inject.Inject

class CreateBoardUseCase @Inject constructor(
    private val repository: IBoardMetaRepository
) {
    suspend operator fun invoke(board: Board) = repository.create(board)
}