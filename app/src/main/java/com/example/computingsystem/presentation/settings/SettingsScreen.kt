package com.example.computingsystem.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.presentation.calculator.AngleMode
import com.example.computingsystem.R

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
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Тема ──────────────────────────────────────────────
        SettingsSectionTitle(
            stringResource(R.string.settings_appearance)
        )

        SettingsSegmentedRow(
            label = stringResource(R.string.settings_theme),
            options = listOf(
                AppTheme.SYSTEM to  stringResource(R.string.settings_theme_auto),
                AppTheme.LIGHT  to  stringResource(R.string.settings_theme_light),
                AppTheme.DARK   to  stringResource(R.string.settings_theme_dark)
            ),
            selected = settings.theme,
            onSelect = { viewModel.setTheme(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Язык ──────────────────────────────────────────────
        SettingsSectionTitle(stringResource(R.string.settings_language_section))

        SettingsSegmentedRow(
            label = stringResource(R.string.settings_language),
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
            stringResource(R.string.settings_calculator)
        )

        SettingsSegmentedRow(
            label = stringResource(R.string.settings_angle_mode),
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