package com.example.computingsystem.domain.usecase

import net.objecthunter.exp4j.ExpressionBuilder
import javax.inject.Inject

class EvaluateExpressionUseCase @Inject constructor() {

    /**
     * Возвращает результат вычисления или null при ошибке.
     * Заменяет «×» и «÷» на операторы, понятные exp4j,
     * а также «π» и «e» на их числовые значения.
     */
    operator fun invoke(input: String): Result<String> {
        return try {
            val sanitized = input
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())
                .let { processPercentage(it) }

            val value = ExpressionBuilder(sanitized)
                .build()
                .evaluate()

            // Если результат целый — убираем дробную часть
            val formatted = if (value % 1.0 == 0.0 && !value.isInfinite()) {
                value.toLong().toString()
            } else {
                value.toBigDecimal().stripTrailingZeros().toPlainString()
            }

            Result.success(formatted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun processPercentage(input: String): String {
        val regex = """(\d+(?:\.\d+)?)%""".toRegex()
        return regex.replace(input) { match ->
            val number = match.groupValues[1]
            "($number/100)"
        }
    }
}