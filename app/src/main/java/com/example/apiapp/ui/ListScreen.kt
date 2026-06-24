package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    state: ListUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCharacterClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.favorites)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                placeholder = { Text(stringResource(R.string.search_characters)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            when (state) {
                is ListUiState.Loading -> LoadingView()
                is ListUiState.Error -> ErrorView(state.message, onRetry)
                is ListUiState.Empty -> EmptyView(stringResource(R.string.no_characters_found, searchQuery))
                is ListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.characters,
                            key = { it.id }
                        ) { character ->
                            CharacterItem(character, onCharacterClick)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterListScreenPreview() {
    ApiappTheme {
        CharacterListScreen(
            state = ListUiState.Success(
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
            searchQuery = "",
            onSearchChange = {},
            onCharacterClick = {},
            onRetry = {},
            onFavoritesClick = {}
        )
    }
}