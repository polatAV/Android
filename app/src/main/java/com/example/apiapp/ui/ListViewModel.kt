package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed interface ListUiState {
    object Loading : ListUiState
    data class Success(val characters: List<Character>) : ListUiState
    data class Error(val message: String) : ListUiState
    object Empty : ListUiState
}

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val listUiState: StateFlow<ListUiState> = combine(
        _searchQuery.debounce { query -> if (query.isEmpty()) 0L else 500L },
        _retryTrigger.onStart { emit(Unit) }
    ) { query, _ -> query }
        .flatMapLatest { query ->
            flow {
                emit(ListUiState.Loading)
                getCharactersUseCase(query.ifBlank { null })
                    .onSuccess { characters ->
                        if (characters.isEmpty()) {
                            emit(ListUiState.Empty)
                        } else {
                            emit(ListUiState.Success(characters))
                        }
                    }
                    .onFailure { throwable ->
                        emit(ListUiState.Error(throwable.message ?: "Unknown error"))
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListUiState.Loading
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }
}
