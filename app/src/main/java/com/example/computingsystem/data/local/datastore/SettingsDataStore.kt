package com.example.computingsystem.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppSettings
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.presentation.calculator.AngleMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_THEME    = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_ANGLE    = stringPreferencesKey("angle_mode")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme    = AppTheme.valueOf(prefs[KEY_THEME]    ?: AppTheme.SYSTEM.name),
            language = AppLanguage.valueOf(prefs[KEY_LANGUAGE] ?: AppLanguage.RUSSIAN.name),
            defaultAngleMode = AngleMode.valueOf(prefs[KEY_ANGLE] ?: AngleMode.RAD.name)
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }

    suspend fun setAngleMode(mode: AngleMode) {
        context.dataStore.edit { it[KEY_ANGLE] = mode.name }
    }
}