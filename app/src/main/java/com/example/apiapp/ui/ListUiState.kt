package com.example.apiapp.ui

import androidx.compose.runtime.Immutable
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.User

@Immutable
data class CharacterItemUiState(
    val character: Character,
    val isFavorite: Boolean,
    val hasNotes: Boolean = false,
    val hasTags: Boolean = false
)

sealed interface ListUiState {
    object Loading : ListUiState
    data class Success(
        val characters: List<CharacterItemUiState>,
        val queries: List<String>,
        val isFromCache: Boolean,
        val currentUser: User?,
        val allUsers: List<User>
    ) : ListUiState
    data class Error(val message: String) : ListUiState
    data class Empty(val query: String) : ListUiState
}

sealed interface ApiState {
    object Loading : ApiState
    data class Success(
        val query: String,
        val characters: List<Character>,
        val isFromCache: Boolean
    ) : ApiState
    data class Error(val throwable: Throwable) : ApiState
}

fun mapToListUiState(
    apiState: ApiState,
    filter: String,
    favorites: List<Character>,
    history: List<String>,
    users: List<User>,
    userId: Int,
    notes: Map<Int, String>,
    tags: Map<Int, List<String>>
): ListUiState {
    val currentUser = users.find { it.id == userId }
    return when (apiState) {
        is ApiState.Loading -> ListUiState.Loading
        is ApiState.Error -> ListUiState.Error(apiState.throwable.message ?: "Unknown error")
        is ApiState.Success -> {
            val filtered = apiState.characters.filter {
                filter == "All" || it.status.equals(filter, ignoreCase = true)
            }
            if (filtered.isEmpty()) {
                ListUiState.Empty(apiState.query)
            } else {
                val favoriteIds = favorites.map { it.id }.toSet()
                val uiItems = filtered.map { character ->
                    CharacterItemUiState(
                        character = character,
                        isFavorite = character.id in favoriteIds,
                        hasNotes = !notes[character.id].isNullOrBlank(),
                        hasTags = !tags[character.id].isNullOrEmpty()
                    )
                }
                ListUiState.Success(
                    characters = uiItems,
                    queries = history,
                    isFromCache = apiState.isFromCache,
                    currentUser = currentUser,
                    allUsers = users
                )
            }
        }
    }
}
