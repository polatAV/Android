package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.domain.usecase.ManageSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ManageUsersUseCase
import com.example.apiapp.domain.usecase.GetAllNotesUseCase
import com.example.apiapp.domain.usecase.GetAllTagsUseCase
import com.example.apiapp.domain.repository.RickAndMortyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val manageSearchHistoryUseCase: ManageSearchHistoryUseCase,
    private val manageUsersUseCase: ManageUsersUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val rickAndMortyRepository: RickAndMortyRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    val activeUserId: StateFlow<Int?> = manageUsersUseCase.getActiveUserId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            // инициализируем пользователя по умолчанию, если БД пуста
            val currentActiveId = manageUsersUseCase.getActiveUserId().first()
            val users = manageUsersUseCase.getUsers().first()
            if (users.isEmpty()) {
                val guestId = manageUsersUseCase.createUser("Guest", "👴")
                manageUsersUseCase.setActiveUser(guestId)
            } else if (currentActiveId == null || users.none { it.id == currentActiveId }) {
                manageUsersUseCase.setActiveUser(users.first().id)
            }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val apiResultFlow: SharedFlow<ApiState> = combine(
        _searchQuery.map { it.trim() }.debounce { if (it.isEmpty()) 0L else 500L }.distinctUntilChanged(),
        _retryTrigger.onStart { emit(Unit) }
    ) { query, _ -> query }
        .flatMapLatest { query ->
            flow {
                emit(ApiState.Loading)
                getCharactersUseCase(query.ifBlank { null })
                    .onSuccess {
                        if (query.isBlank()) rickAndMortyRepository.saveCachedCharacters(it)
                        emit(ApiState.Success(query, it, isFromCache = false))
                    }
                    .onFailure {
                        val cached = rickAndMortyRepository.getCachedCharacters()
                        if (cached.isNotEmpty() && query.isBlank()) {
                            emit(ApiState.Success(query, cached, isFromCache = true))
                        } else {
                            emit(ApiState.Error(it))
                        }
                    }
            }
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val listUiState: StateFlow<ListUiState> = activeUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                val userContextFlow = combine(
                    manageSearchHistoryUseCase(userId),
                    manageUsersUseCase.getUsers(),
                    getAllNotesUseCase(userId),
                    getAllTagsUseCase(userId)
                ) { history, users, notes, tags ->
                    Pair(Pair(history, users), Pair(notes, tags))
                }

                combine(
                    apiResultFlow,
                    _statusFilter,
                    getFavoritesUseCase(userId),
                    userContextFlow
                ) { apiState, filter, favorites, context ->
                    val (historyUsers, notesTags) = context
                    val (history, users) = historyUsers
                    val (notes, tags) = notesTags
                    mapToListUiState(apiState, filter, favorites, history, users, userId, notes, tags)
                }
            } else {
                flow { emit(ListUiState.Loading) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListUiState.Loading
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery.replace("\n", "").replace("\r", "")
    }

    fun onStatusFilterChange(newStatus: String) {
        _statusFilter.value = newStatus
    }

    fun selectUser(userId: Int) = viewModelScope.launch {
        manageUsersUseCase.setActiveUser(userId)
    }

    fun createAndSelectUser(name: String, avatarResName: String) = viewModelScope.launch {
        val newId = manageUsersUseCase.createUser(name, avatarResName)
        manageUsersUseCase.setActiveUser(newId)
    }

    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }

    fun toggleFavorite(character: Character) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(character, userId) }
                .onFailure { _errorEvents.emit("Failed to update favorites") }
        }
    }

    fun deleteHistoryQuery(query: String) = viewModelScope.launch {
        activeUserId.value?.let { userId ->
            runCatching { manageSearchHistoryUseCase.delete(query, userId) }
                .onFailure { android.util.Log.e("ListViewModel", "failed to delete history query", it) }
        }
    }

    fun clearHistory() = viewModelScope.launch {
        activeUserId.value?.let { userId ->
            runCatching { manageSearchHistoryUseCase.clear(userId) }
                .onFailure { android.util.Log.e("ListViewModel", "failed to clear history", it) }
        }
    }

    fun saveSearchQuery(query: String) = viewModelScope.launch {
        activeUserId.value?.let { userId ->
            val cleaned = query.trim().replace("\n", "").replace("\r", "")
            if (cleaned.isNotBlank()) {
                runCatching { manageSearchHistoryUseCase.add(cleaned, userId) }
                    .onFailure { android.util.Log.e("ListViewModel", "failed to save search query", it) }
            }
        }
    }
}
