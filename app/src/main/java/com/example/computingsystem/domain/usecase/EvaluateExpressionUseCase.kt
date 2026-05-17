package com.example.computingsystem.domain.usecase

import com.example.computingsystem.presentation.calculator.AngleMode
import net.objecthunter.exp4j.ExpressionBuilder
import javax.inject.Inject
import kotlin.math.*
import net.objecthunter.exp4j.function.Function

class EvaluateExpressionUseCase @Inject constructor() {

    private val customFunctions = listOf(
        object : Function("asin", 1) {
            override fun apply(vararg args: Double): Double = asin(args[0])
        },
        object : Function("acos", 1) {
            override fun apply(vararg args: Double): Double = acos(args[0])
        },
        object : Function("atan", 1) {
            override fun apply(vararg args: Double): Double = atan(args[0])
        },
        object : Function("log10", 1) {
            override fun apply(vararg args: Double): Double = log10(args[0])
        },
        object : Function("fact", 1) {
            override fun apply(vararg args: Double): Double {
                val n = args[0].toInt()
                if (n < 0 || n > 20 || args[0] != n.toDouble()) {
                    throw ArithmeticException("Factorial only for integers 0-20")
                }
                return factorial(n).toDouble()
            }
        }
    )

    operator fun invoke(input: String, angleMode: AngleMode = AngleMode.RAD): Result<String> {
        return try {
            val sanitized = input
                .let { addImplicitMultiplication(it) }
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())
                .let { processPercentage(it) }
                .let { processFactorial(it) }
                .let { processReciprocal(it) }
                .let { processTrigFunctions(it, angleMode) }
                .let { processArcFunctions(it, angleMode) }
                .replace("ln(", "log(")
                .replace("lg(", "log10(")

            val value = ExpressionBuilder(sanitized)
                .functions(*customFunctions.toTypedArray())
                .build()
                .evaluate()

            val formatted = if (value % 1.0 == 0.0 && !value.isInfinite() && value.absoluteValue < 1e15) {
                value.toLong().toString()
            } else {
                value.toBigDecimal().stripTrailingZeros().toPlainString()
            }

            Result.success(formatted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addImplicitMultiplication(input: String): String {
        return input
            // Число + π или e
            .replace(Regex("""(\d)([πe])""")) { "${it.groupValues[1]}*${it.groupValues[2]}" }
            // π или e + число
            .replace(Regex("""([πe])(\d)""")) { "${it.groupValues[1]}*${it.groupValues[2]}" }
            // Число + открывающая скобка
            .replace(Regex("""(\d)\(""")) { "${it.groupValues[1]}*(" }
            // Закрывающая скобка + число
            .replace(Regex("""\)(\d)""")) { ")*${it.groupValues[1]}" }
            // Закрывающая скобка + открывающая скобка
            .replace(Regex("""\)\("""), ")*(")
            // π или e + открывающая скобка
            .replace(Regex("""([πe])\(""")) { "${it.groupValues[1]}*(" }
            // Закрывающая скобка + π или e
            .replace(Regex("""\)([πe])""")) { ")*${it.groupValues[1]}" }
    }

    private fun processPercentage(input: String): String {
        val regex = """(\d+(?:\.\d+)?|\([^)]+\))%""".toRegex()
        return regex.replace(input) { match ->
            val number = match.groupValues[1]
            "($number/100)"
        }
    }

    private fun processFactorial(input: String): String {
        // Число или выражение в скобках + !
        val regex = """(\d+|\([^)]+\))!""".toRegex()
        return regex.replace(input) { match ->
            val expression = match.groupValues[1]
            "fact($expression)"
        }
    }

    private fun factorial(n: Int): Long {
        return if (n <= 1) 1 else n * factorial(n - 1)
    }

    private fun processReciprocal(input: String): String {
        return input
    }

    private fun processTrigFunctions(input: String, angleMode: AngleMode): String {
        if (angleMode == AngleMode.RAD) return input

        return input
            .replace(Regex("""sin\(""")) { "sin((${Math.PI}/180)*" }
            .replace(Regex("""cos\(""")) { "cos((${Math.PI}/180)*" }
            .replace(Regex("""tan\(""")) { "tan((${Math.PI}/180)*" }
    }

    private fun processArcFunctions(input: String, angleMode: AngleMode): String {
        if (angleMode == AngleMode.DEG) {
            return input
                .replace(Regex("""asin\(""")) { "((180/${Math.PI})*asin(" }
                .replace(Regex("""acos\(""")) { "((180/${Math.PI})*acos(" }
                .replace(Regex("""atan\(""")) { "((180/${Math.PI})*atan(" }
        }
        return input
    }
}