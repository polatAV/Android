package com.example.apiapp.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetCharacterDetailUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: Int = checkNotNull(savedStateHandle["characterId"])

    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<DetailUiState> = _retryTrigger.onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                emit(DetailUiState.Loading)
                getCharacterDetailUseCase.getCharacter(characterId)
                    .onSuccess { emit(DetailUiState.Success(it)) }
                    .onFailure { emit(DetailUiState.Error(it.message ?: "Unknown error")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    val isFavorite: StateFlow<Boolean> = getCharacterDetailUseCase.isFavoriteFlow(characterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleFavorite(character: Character) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(character)
            } catch (e: Exception) {
                android.util.Log.e("DetailViewModel", "Failed to toggle favorite for ${character.name}", e)
            }
        }
    }

    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }
}