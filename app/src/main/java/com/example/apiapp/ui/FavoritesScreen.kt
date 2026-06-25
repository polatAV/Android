package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
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
    onSelectTag: (String?) -> Unit,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_favorites), stringResource(R.string.tab_notes_tags))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.personal_cabinet_title)) },
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
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    if (selectedTabIndex == 0) {
                        val favourites = state.favorites
                        if (favourites.isEmpty()) {
                            EmptyView(stringResource(R.string.no_favorites_yet))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
                            ) {
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
                    } else {
                        val notesAndTags = state.notesAndTags
                        val availableTags = (state as? FavoritesUiState.Success)?.availableTags ?: emptyList()
                        val selectedTag = (state as? FavoritesUiState.Success)?.selectedTag

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (availableTags.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = dimensionResource(id = R.dimen.padding_medium), vertical = dimensionResource(id = R.dimen.padding_small)),
                                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                                ) {
                                    item {
                                        FilterChip(
                                            selected = selectedTag == null,
                                            onClick = { onSelectTag(null) },
                                            label = { Text(stringResource(R.string.tag_filter_all)) }
                                        )
                                    }
                                    items(availableTags, key = { it }) { tag ->
                                        FilterChip(
                                            selected = selectedTag == tag,
                                            onClick = { onSelectTag(tag) },
                                            label = { Text(tag) }
                                        )
                                    }
                                }
                            }

                            if (notesAndTags.isEmpty()) {
                                EmptyView(
                                    if (selectedTag != null) stringResource(R.string.no_chars_with_tag, selectedTag)
                                    else stringResource(R.string.no_notes_or_tags)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
                                ) {
                                    items(
                                        items = notesAndTags,
                                        key = { it.character.id }
                                    ) { item ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            CharacterItem(
                                                character = item.character,
                                                isFavorite = item.isFavorite,
                                                onFavoriteClick = { onToggleFavorite(item.character) },
                                                onClick = onCharacterClick
                                            )
                                            if (!item.note.isNullOrBlank()) {
                                                Text(
                                                    text = stringResource(R.string.note_label, item.note.orEmpty()),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(
                                                        start = dimensionResource(id = R.dimen.padding_medium),
                                                        end = dimensionResource(id = R.dimen.padding_medium),
                                                        bottom = dimensionResource(id = R.dimen.padding_small)
                                                    )
                                                )
                                            }
                                            if (item.tags.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                                                    modifier = Modifier.padding(
                                                        start = dimensionResource(id = R.dimen.padding_medium),
                                                        bottom = dimensionResource(id = R.dimen.padding_medium)
                                                    )
                                                ) {
                                                    item.tags.forEach { tag ->
                                                        SuggestionChip(onClick = {}, label = { Text(tag) })
                                                    }
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}