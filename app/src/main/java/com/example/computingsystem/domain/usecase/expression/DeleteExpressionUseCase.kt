package com.example.computingsystem.domain.usecase.expression

import com.example.computingsystem.domain.model.Expression
import com.example.computingsystem.domain.repository.IExpressionRepository
import javax.inject.Inject

class DeleteExpressionUseCase @Inject constructor(
    private val repository: IExpressionRepository
) {
    suspend operator fun invoke(expression: Expression) =
        repository.delete(expression)
}