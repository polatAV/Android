package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    data class Success(val characters: List<Character>) : FavoritesUiState
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase
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
}