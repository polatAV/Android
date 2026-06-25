package com.example.apiapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.model.User
import com.example.apiapp.ui.theme.ApiappTheme
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true)
@Composable
fun CharacterListScreenPreview() {
    ApiappTheme {
        CharacterListScreen(
            state = ListUiState.Success(
                characters = listOf(
                    CharacterItemUiState(
                        character = Character(
                            id = 1,
                            name = "Rick Sanchez",
                            status = "Alive",
                            species = "Human",
                            type = "",
                            gender = "Male",
                            image = "",
                            origin = Location("Earth", ""),
                            location = Location("Earth", "")
                        ),
                        isFavorite = true
                    )
                ),
                queries = emptyList(),
                isFromCache = false,
                currentUser = User(1, "Rick", "👴"),
                allUsers = listOf(User(1, "Rick", "👴"))
            ),
            searchQuery = "",
            statusFilter = "All",
            errorEvents = emptyFlow(),
            onSearchChange = {},
            onSearchSubmit = {},
            onStatusFilterChange = {},
            onCharacterClick = {},
            onToggleFavorite = {},
            onDeleteHistoryQuery = {},
            onClearHistory = {},
            onRetry = {},
            onFavoritesClick = {},
            onSettingsClick = {},
            onSelectUser = {},
            onCreateUser = { _, _ -> }
        )
    }
}
