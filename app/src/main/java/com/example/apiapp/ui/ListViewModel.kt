package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.GetSearchHistoryUseCase
import com.example.apiapp.domain.usecase.AddSearchHistoryUseCase
import com.example.apiapp.domain.usecase.DeleteSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ClearSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterItemUiState(
    val character: Character,
    val isFavorite: Boolean
)

sealed interface ListUiState {
    object Loading : ListUiState
    data class Success(
        val characters: List<CharacterItemUiState>,
        val queries: List<String>
    ) : ListUiState
    data class Error(val message: String) : ListUiState
    data class Empty(val query: String) : ListUiState
}

sealed interface ApiState {
    object Loading : ApiState
    data class Success(val query: String, val characters: List<Character>) : ApiState
    data class Error(val throwable: Throwable) : ApiState
}

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val addSearchHistoryUseCase: AddSearchHistoryUseCase,
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val apiResultFlow: Flow<ApiState> = combine(
        _searchQuery
            .map { it.trim() }
            .debounce { query -> if (query.isEmpty()) 0L else 500L }
            .distinctUntilChanged(),
        _retryTrigger.onStart { emit(Unit) }
    ) { query, _ -> query }
        .flatMapLatest { query ->
            flow {
                emit(ApiState.Loading)
                getCharactersUseCase(query.ifBlank { null })
                    .onSuccess { characters ->
                        emit(ApiState.Success(query, characters))
                    }
                    .onFailure { throwable ->
                        emit(ApiState.Error(throwable))
                    }
            }
        }

    val listUiState: StateFlow<ListUiState> = combine(
        apiResultFlow,
        _statusFilter,
        getFavoritesUseCase(),
        getSearchHistoryUseCase()
    ) { apiState, filter, favorites, history ->
        when (apiState) {
            is ApiState.Loading -> {
                ListUiState.Loading
            }
            is ApiState.Error -> {
                ListUiState.Error(apiState.throwable.message ?: "Unknown error")
            }
            is ApiState.Success -> {
                val filtered = apiState.characters.filter {
                    filter == "All" || it.status.equals(filter, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    ListUiState.Empty(apiState.query)
                } else {
                    val favoriteIds = favorites.map { it.id }.toSet()
                    val uiItems = filtered.map { character ->
                        CharacterItemUiState(character, isFavorite = character.id in favoriteIds)
                    }
                    ListUiState.Success(uiItems, history)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading
    )

    fun onSearchQueryChange(newQuery: String) {
        val cleaned = newQuery.replace("\n", "").replace("\r", "")
        _searchQuery.value = cleaned
    }

    fun onStatusFilterChange(newStatus: String) {
        _statusFilter.value = newStatus
    }

    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }

    fun toggleFavorite(character: Character) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(character)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to update favorites")
            }
        }
    }

    fun deleteHistoryQuery(query: String) {
        viewModelScope.launch {
            try {
                deleteSearchHistoryUseCase(query)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                clearSearchHistoryUseCase()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            try {
                val cleaned = query.trim().replace("\n", "").replace("\r", "")
                if (cleaned.isNotBlank()) {
                    addSearchHistoryUseCase(cleaned)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
