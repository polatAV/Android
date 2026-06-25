package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.data.preferences.SettingsDataStore
import com.example.apiapp.data.sync.SyncScheduler
import com.example.apiapp.domain.repository.RickAndMortyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val syncScheduler: SyncScheduler,
    private val rickAndMortyRepository: RickAndMortyRepository
) : ViewModel() {

    val appTheme: StateFlow<String> = settingsDataStore.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val appLanguage: StateFlow<String> = settingsDataStore.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val syncIntervalMinutes: StateFlow<Int> = settingsDataStore.syncIntervalMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.setAppTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setAppLanguage(language)
        }
    }

    fun setSyncInterval(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.setSyncIntervalMinutes(minutes)
            // планируем или отменяем фоновые задачи воркера на основе обновленных настроек
            syncScheduler.scheduleSync(minutes)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                rickAndMortyRepository.clearCachedCharacters()
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "failed to clear cache", e)
            }
        }
    }
}
