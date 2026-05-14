package com.example.computingsystem.domain.usecase

import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.repository.IExpressionRepository
import javax.inject.Inject

class SaveExpressionUseCase @Inject constructor(
    private val repository: IExpressionRepository
) {
    suspend operator fun invoke(expression: Expression) =
        repository.save(expression)
}