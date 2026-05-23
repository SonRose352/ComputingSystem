package com.example.computingsystem.domain.usecase.mappin

import com.example.computingsystem.domain.model.MapPin
import com.example.computingsystem.domain.repository.IMapPinRepository
import javax.inject.Inject

class UpdateMapPinUseCase @Inject constructor(
    private val repository: IMapPinRepository
) {
    suspend operator fun invoke(pin: MapPin) = repository.updatePin(pin)
}