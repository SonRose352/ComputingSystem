package com.example.computingsystem.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.presentation.calculator.AngleMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (settings.language == AppLanguage.RUSSIAN) "Настройки" else "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Тема ──────────────────────────────────────────────
        SettingsSectionTitle(
            if (settings.language == AppLanguage.RUSSIAN) "Внешний вид" else "Appearance"
        )

        SettingsSegmentedRow(
            label = if (settings.language == AppLanguage.RUSSIAN) "Тема" else "Theme",
            options = listOf(
                AppTheme.SYSTEM to  if (settings.language == AppLanguage.RUSSIAN) "Авто"    else "Auto",
                AppTheme.LIGHT  to  if (settings.language == AppLanguage.RUSSIAN) "Светлая" else "Light",
                AppTheme.DARK   to  if (settings.language == AppLanguage.RUSSIAN) "Тёмная"  else "Dark"
            ),
            selected = settings.theme,
            onSelect = { viewModel.setTheme(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Язык ──────────────────────────────────────────────
        SettingsSectionTitle(
            if (settings.language == AppLanguage.RUSSIAN) "Язык" else "Language"
        )

        SettingsSegmentedRow(
            label = if (settings.language == AppLanguage.RUSSIAN) "Язык интерфейса" else "Interface language",
            options = listOf(
                AppLanguage.RUSSIAN to "Русский",
                AppLanguage.ENGLISH to "English"
            ),
            selected = settings.language,
            onSelect = { viewModel.setLanguage(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Калькулятор ───────────────────────────────────────
        SettingsSectionTitle(
            if (settings.language == AppLanguage.RUSSIAN) "Калькулятор" else "Calculator"
        )

        SettingsSegmentedRow(
            label = if (settings.language == AppLanguage.RUSSIAN)
                "Угол по умолчанию" else "Default angle mode",
            options = listOf(
                AngleMode.RAD to "RAD",
                AngleMode.DEG to "DEG"
            ),
            selected = settings.defaultAngleMode,
            onSelect = { viewModel.setAngleMode(it) }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun <T> SettingsSegmentedRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (value, title) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(title) }
                )
            }
        }
    }
}