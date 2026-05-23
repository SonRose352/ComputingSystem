package com.example.computingsystem.domain.usecase.mappin

import com.example.computingsystem.domain.repository.IMapPinRepository
import javax.inject.Inject

class DeleteMapPinUseCase @Inject constructor(
    private val repository: IMapPinRepository
) {
    suspend operator fun invoke(pinId: String) = repository.deletePin(pinId)
}