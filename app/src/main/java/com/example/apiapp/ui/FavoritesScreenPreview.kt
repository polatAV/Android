package com.example.apiapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    ApiappTheme {
        FavoritesScreen(
            state = FavoritesUiState.Success(
                favorites = listOf(
                    Character(
                        id = 1,
                        name = "Rick Sanchez",
                        status = "Alive",
                        species = "Human",
                        type = "",
                        gender = "Male",
                        image = "",
                        origin = Location("Earth", ""),
                        location = Location("Earth", "")
                    )
                ),
                notesAndTags = listOf(
                    PersonalItemUiState(
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
                        note = "Greatest scientist",
                        tags = listOf("Genius")
                    )
                )
            ),
            onCharacterClick = {},
            onToggleFavorite = {},
            onSelectTag = {},
            onBack = {}
        )
    }
}
