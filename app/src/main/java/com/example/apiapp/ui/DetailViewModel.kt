package com.example.apiapp.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetActiveUserIdUseCase
import com.example.apiapp.domain.usecase.GetCharacterDetailUseCase
import com.example.apiapp.domain.usecase.GetNoteUseCase
import com.example.apiapp.domain.usecase.GetTagsUseCase
import com.example.apiapp.domain.usecase.SaveNoteUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.domain.usecase.ToggleTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val character: Character) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getActiveUserIdUseCase: GetActiveUserIdUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val toggleTagUseCase: ToggleTagUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: Int = checkNotNull(savedStateHandle["characterId"])

    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // отслеживаем id активного пользователя, чтобы перестраивать зависимости экранов
    val activeUserId: StateFlow<Int?> = getActiveUserIdUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<DetailUiState> = kotlinx.coroutines.flow.combine(
        _retryTrigger.onStart { emit(Unit) },
        activeUserId.filterNotNull()
    ) { _, userId -> userId }
        .flatMapLatest { userId ->
            flow {
                emit(DetailUiState.Loading)
                getCharacterDetailUseCase.getCharacter(characterId, userId)
                    .onSuccess { emit(DetailUiState.Success(it)) }
                    .onFailure { emit(DetailUiState.Error(it.message ?: "Unknown error")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = activeUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getCharacterDetailUseCase.isFavoriteFlow(characterId, userId)
            } else {
                flow { emit(false) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val note: StateFlow<String> = activeUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getNoteUseCase(characterId, userId)
            } else {
                flow { emit(null) }
            }
        }
        .map { it ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tags: StateFlow<List<String>> = activeUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getTagsUseCase(characterId, userId)
            } else {
                flow { emit(emptyList()) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    fun toggleFavorite(character: Character) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(character, userId)
            } catch (e: Exception) {
                _snackbarEvents.emit("Failed to update favorites")
            }
        }
    }

    fun saveNote(noteText: String) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            try {
                saveNoteUseCase(characterId, userId, noteText)
                _snackbarEvents.emit("Note saved successfully!")
            } catch (e: Exception) {
                _snackbarEvents.emit("Failed to save note")
            }
        }
    }

    fun toggleTag(tagName: String) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            try {
                toggleTagUseCase(characterId, userId, tagName)
            } catch (e: Exception) {
                _snackbarEvents.emit("Failed to update tag")
            }
        }
    }

    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }
}