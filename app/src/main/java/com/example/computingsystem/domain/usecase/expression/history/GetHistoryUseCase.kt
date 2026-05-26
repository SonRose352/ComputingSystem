package com.example.computingsystem.domain.usecase.expression.history

import com.example.computingsystem.domain.repository.IExpressionRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: IExpressionRepository
) {
    operator fun invoke() = repository.getHistory()
}