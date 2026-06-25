package com.example.apiapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.model.User
import com.example.apiapp.ui.theme.ApiappTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    state: ListUiState, searchQuery: String, statusFilter: String, errorEvents: Flow<String>,
    onSearchChange: (String) -> Unit, onSearchSubmit: () -> Unit, onStatusFilterChange: (String) -> Unit,
    onCharacterClick: (Int) -> Unit, onToggleFavorite: (Character) -> Unit, onDeleteHistoryQuery: (String) -> Unit,
    onClearHistory: () -> Unit, onRetry: () -> Unit, onFavoritesClick: () -> Unit, onSettingsClick: () -> Unit,
    onSelectUser: (Int) -> Unit, onCreateUser: (String, String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var showProfileDialog by remember { mutableStateOf(false) }
    LaunchedEffect(errorEvents) {
        errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    val currentUser = (state as? ListUiState.Success)?.currentUser
    val allUsers = (state as? ListUiState.Success)?.allUsers ?: emptyList()
    if (showProfileDialog) {
        ProfileDialog(
            currentUser = currentUser,
            allUsers = allUsers,
            onSelectUser = onSelectUser,
            onCreateUser = onCreateUser,
            onDismiss = { showProfileDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    if (currentUser != null) {
                        TextButton(onClick = { showProfileDialog = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(
                                    avatar = currentUser.avatarResName,
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size_small))
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                                Text(text = currentUser.getDisplayName())
                            }
                        }
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.bookmarks_content_description)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_content_description)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection)
                )
        ) {
            if (state is ListUiState.Success && state.isFromCache) {
                // отображаем плашку офлайн-режима при отображении локального кэша
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Text(
                        text = stringResource(R.string.offline_mode_banner),
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            CharacterSearchField(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onSearchSubmit = onSearchSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_medium))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                val statuses = listOf("All", "Alive", "Dead", "Unknown")
                val statusLabels = mapOf(
                    "All" to R.string.status_filter_all,
                    "Alive" to R.string.status_filter_alive,
                    "Dead" to R.string.status_filter_dead,
                    "Unknown" to R.string.status_filter_unknown
                )
                statuses.forEach { status ->
                    val selected = status == statusFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(status) },
                        label = { Text(stringResource(statusLabels[status] ?: R.string.status_filter_all)) }
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
                    ) {
                        if (searchQuery.isEmpty() && state.queries.isNotEmpty()) {
                            item {
                                SearchHistorySection(
                                    queries = state.queries, onSearchChange = onSearchChange,
                                    onDeleteHistoryQuery = onDeleteHistoryQuery, onClearHistory = onClearHistory
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                            }
                        }
                        items(
                            items = state.characters,
                            key = { it.character.id }
                        ) { item ->
                            CharacterItem(
                                character = item.character, isFavorite = item.isFavorite,
                                onFavoriteClick = { onToggleFavorite(item.character) }, onClick = onCharacterClick,
                                hasNotes = item.hasNotes, hasTags = item.hasTags
                            )
                        }
                    }
                }
            }
        }
    }
}
