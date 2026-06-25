package com.example.apiapp.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiapp.R
import com.example.apiapp.ui.theme.ApiappTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val theme by viewModel.appTheme.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val syncInterval by viewModel.syncIntervalMinutes.collectAsStateWithLifecycle()
    SettingsContent(
        theme = theme,
        language = language,
        syncInterval = syncInterval,
        onThemeChange = viewModel::setTheme,
        onLanguageChange = viewModel::setLanguage,
        onSyncIntervalChange = viewModel::setSyncInterval,
        onClearCache = viewModel::clearCache,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    theme: String,
    language: String,
    syncInterval: Int,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSyncIntervalChange: (Int) -> Unit,
    onClearCache: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Text(text = stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
            val themes = listOf("system" to R.string.settings_theme_system, "light" to R.string.settings_theme_light, "dark" to R.string.settings_theme_dark)
            Column {
                themes.forEach { (key, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeChange(key) }
                            .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = theme == key, onClick = null)
                        Text(stringResource(labelRes), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)))
                    }
                }
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleMedium)
            val languages = listOf("system" to R.string.settings_language_system, "en" to R.string.settings_language_en, "ru" to R.string.settings_language_ru)
            Column {
                languages.forEach { (key, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageChange(key) }
                            .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = language == key, onClick = null)
                        Text(stringResource(labelRes), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)))
                    }
                }
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_sync_title), style = MaterialTheme.typography.titleMedium)
            val syncOptions = listOf(0 to R.string.settings_sync_off, 15 to R.string.settings_sync_15_min, 60 to R.string.settings_sync_1_hour, 720 to R.string.settings_sync_12_hours)
            Column {
                syncOptions.forEach { (minutes, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSyncIntervalChange(minutes) }
                            .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = syncInterval == minutes, onClick = null)
                        Text(stringResource(labelRes), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)))
                    }
                }
            }

            HorizontalDivider()

            Text(text = stringResource(R.string.settings_cache_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Button(
                onClick = {
                    onClearCache()
                    Toast.makeText(context, context.getString(R.string.settings_cache_cleared), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_clear_cache))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    ApiappTheme {
        SettingsContent(
            theme = "system", language = "system", syncInterval = 60,
            onThemeChange = {}, onLanguageChange = {}, onSyncIntervalChange = {},
            onClearCache = {}, onBack = {}
        )
    }
}
