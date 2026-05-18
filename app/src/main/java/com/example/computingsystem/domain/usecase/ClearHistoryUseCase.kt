package com.example.computingsystem.domain.usecase

import com.example.computingsystem.domain.repository.IExpressionRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: IExpressionRepository
) {
    suspend operator fun invoke() = repository.deleteAll()
}