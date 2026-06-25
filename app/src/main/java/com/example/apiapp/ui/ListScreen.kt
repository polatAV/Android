package com.example.apiapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    state: ListUiState,
    searchQuery: String,
    statusFilter: String,
    errorEvents: Flow<String>,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onCharacterClick: (Int) -> Unit,
    onToggleFavorite: (Character) -> Unit,
    onDeleteHistoryQuery: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRetry: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(errorEvents) {
        errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    // prevent newline/carriage return insertion, e.g. from physical enter keys
                    val cleaned = it.replace("\n", "").replace("\r", "")
                    onSearchChange(cleaned)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchSubmit()
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                placeholder = { Text(stringResource(R.string.search_characters)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                val statuses = listOf("All", "Alive", "Dead", "Unknown")
                statuses.forEach { status ->
                    val selected = status == statusFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(status) },
                        label = { Text(status) }
                    )
                }
            }

            HorizontalDivider()

            when (state) {
                is ListUiState.Loading -> LoadingView()
                is ListUiState.Error -> ErrorView(state.message, onRetry)
                is ListUiState.Empty -> EmptyView(stringResource(R.string.no_characters_found, state.query))
                is ListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (searchQuery.isEmpty() && state.queries.isNotEmpty()) {
                            item {
                                SearchHistorySection(
                                    queries = state.queries,
                                    onSearchChange = onSearchChange,
                                    onDeleteHistoryQuery = onDeleteHistoryQuery,
                                    onClearHistory = onClearHistory
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                            }
                        }

                        items(
                            items = state.characters,
                            key = { it.character.id }
                        ) { item ->
                            CharacterItem(
                                character = item.character,
                                isFavorite = item.isFavorite,
                                onFavoriteClick = { onToggleFavorite(item.character) },
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
private fun CharacterListScreenPreview() {
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
                queries = emptyList()
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
            onFavoritesClick = {}
        )
    }
}