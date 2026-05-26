package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppSettings
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.presentation.calculator.AngleMode
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setAngleMode(mode: AngleMode)
}