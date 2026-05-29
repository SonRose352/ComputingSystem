package com.example.computingsystem.domain.usecase.mappin

import com.example.computingsystem.domain.repository.IMapPinRepository
import javax.inject.Inject

class GetMapPinsUseCase @Inject constructor(
    private val repository: IMapPinRepository
) {
    operator fun invoke(boardId: String) = repository.getPins(boardId)
}