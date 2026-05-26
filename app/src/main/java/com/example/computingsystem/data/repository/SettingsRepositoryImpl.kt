package com.example.computingsystem.data.repository

import com.example.computingsystem.data.local.datastore.SettingsDataStore
import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppSettings
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.domain.repository.ISettingsRepository
import com.example.computingsystem.presentation.calculator.AngleMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : ISettingsRepository {

    override fun getSettings(): Flow<AppSettings> = dataStore.settings

    override suspend fun setTheme(theme: AppTheme) = dataStore.setTheme(theme)

    override suspend fun setLanguage(language: AppLanguage) = dataStore.setLanguage(language)

    override suspend fun setAngleMode(mode: AngleMode) = dataStore.setAngleMode(mode)
}