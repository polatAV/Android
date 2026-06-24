package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    data class Success(val characters: List<Character>) : FavoritesUiState
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val favoritesUiState: StateFlow<FavoritesUiState> = getFavoritesUseCase()
        .map { characters ->
            FavoritesUiState.Success(characters)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState.Loading
        )

    fun toggleFavorite(character: Character) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(character)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}