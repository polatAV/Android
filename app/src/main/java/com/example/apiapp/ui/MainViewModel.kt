package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apiapp.data.model.Character
import com.example.apiapp.data.repository.RickAndMortyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListScreenUiState(
    val query: String = "",
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favourites: Set<Int> = emptySet(),
    val isSearchResultEmpty: Boolean = false
)

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val character: Character) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class MainViewModel(private val repository: RickAndMortyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ListScreenUiState())
    val uiState: StateFlow<ListScreenUiState> = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        getCharactersInternal(null)
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            getCharactersInternal(newQuery.ifBlank { null })
        }
    }

    fun retryListLoad() {
        getCharactersInternal(_uiState.value.query.ifBlank { null })
    }

    private fun getCharactersInternal(query: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSearchResultEmpty = false, error = null) }
            repository.getCharacters(query)
                .onSuccess { characters ->
                    _uiState.update {
                        it.copy(
                            characters = characters,
                            isLoading = false,
                            isSearchResultEmpty = characters.isEmpty() && !query.isNullOrBlank()
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    fun loadCharacterDetail(id: Int) {
        viewModelScope.launch {
            _detailUiState.value = DetailUiState.Loading
            repository.getCharacter(id)
                .onSuccess { character ->
                    _detailUiState.value = DetailUiState.Success(character)
                }
                .onFailure { throwable ->
                    _detailUiState.value = DetailUiState.Error(throwable.message ?: "Unknown error")
                }
        }
    }

    fun toggleFavorite(characterId: Int) {
        _uiState.update { currentState ->
            val newFavorites = if (currentState.favourites.contains(characterId)) {
                currentState.favourites - characterId
            } else {
                currentState.favourites + characterId
            }
            currentState.copy(favourites = newFavorites)
        }
    }

    companion object {
        fun provideFactory(
            repository: RickAndMortyRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
