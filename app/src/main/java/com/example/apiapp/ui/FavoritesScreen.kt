package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onCharacterClick: (Int) -> Unit,
    onToggleFavorite: (Character) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is FavoritesUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is FavoritesUiState.Success -> {
                val favourites = state.characters
                if (favourites.isEmpty()) {
                    EmptyView(stringResource(R.string.no_favorites_yet), modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(
                            items = favourites,
                            key = { it.id }
                        ) { character ->
                            CharacterItem(
                                character = character,
                                isFavorite = true,
                                onFavoriteClick = { onToggleFavorite(character) },
                                onClick = onCharacterClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    ApiappTheme {
        FavoritesScreen(
            state = FavoritesUiState.Success(
                characters = listOf(
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
                )
            ),
            onCharacterClick = {},
            onToggleFavorite = {},
            onBack = {}
        )
    }
}