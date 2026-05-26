package com.example.computingsystem.domain.model

import com.example.computingsystem.presentation.calculator.AngleMode

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage {
    RUSSIAN, ENGLISH
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.RUSSIAN,
    val defaultAngleMode: AngleMode = AngleMode.RAD
)