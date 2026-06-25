package com.example.apiapp.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.usecase.GetActiveUserIdUseCase
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.GetAllNotesUseCase
import com.example.apiapp.domain.usecase.GetAllTagsUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.domain.repository.RickAndMortyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class PersonalItemUiState(
    val character: Character,
    val note: String?,
    val tags: List<String>,
    val isFavorite: Boolean = false
)

sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    data class Success(
        val favorites: List<Character>,
        val notesAndTags: List<PersonalItemUiState>,
        val availableTags: List<String> = emptyList(),
        val selectedTag: String? = null
    ) : FavoritesUiState
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getActiveUserIdUseCase: GetActiveUserIdUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val rickAndMortyRepository: RickAndMortyRepository
) : ViewModel() {

    // отслеживаем активного пользователя для реактивного переключения его данных
    val activeUserId: StateFlow<Int?> = getActiveUserIdUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag

    init {
        viewModelScope.launch {
            activeUserId.collect {
                _selectedTag.value = null
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoritesUiState: StateFlow<FavoritesUiState> = activeUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                combine(
                    getFavoritesUseCase(userId),
                    getAllNotesUseCase(userId),
                    getAllTagsUseCase(userId),
                    rickAndMortyRepository.getCachedCharactersFlow(),
                    _selectedTag
                ) { favorites, notes, tags, cached, selectedTag ->
                    val personalIds = (notes.keys + tags.keys).toSet()
                    val favoriteIds = favorites.map { it.id }.toSet()
                    val allLocalChars = (favorites + cached).associateBy { it.id }
                    
                    // соберем все доступные теги для фильтрации
                    val allAvailableTags = tags.values.flatten().toSet().toList().sorted()

                    val personalItems = personalIds.mapNotNull { id ->
                        val char = allLocalChars[id]
                        if (char != null) {
                            val charTags = tags[id] ?: emptyList()
                            if (selectedTag == null || selectedTag in charTags) {
                                PersonalItemUiState(
                                    character = char,
                                    note = notes[id],
                                    tags = charTags,
                                    isFavorite = favoriteIds.contains(id)
                                )
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }

                    FavoritesUiState.Success(favorites, personalItems, allAvailableTags, selectedTag) as FavoritesUiState
                }
            } else {
                flow { emit(FavoritesUiState.Loading as FavoritesUiState) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState.Loading
        )

    fun toggleFavorite(character: Character) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(character, userId)
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "failed to toggle favorite", e)
            }
        }
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }
}