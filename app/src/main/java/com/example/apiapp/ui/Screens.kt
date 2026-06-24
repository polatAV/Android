package com.example.apiapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.data.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    state: ListScreenUiState,
    onSearchChange: (String) -> Unit,
    onCharacterClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rick & Morty") },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search characters...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            when {
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(state.error, onRetry)
                state.isSearchResultEmpty -> EmptyView("No characters found for '''${state.query}'''")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.characters) { character ->
                            CharacterItem(character, onCharacterClick)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    state: DetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: (Int) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is DetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is DetailUiState.Error -> ErrorView(state.message, onRetry, modifier = Modifier.padding(padding))
            is DetailUiState.Success -> {
                val character = state.character
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = character.name, style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Status: ${character.status}")
                    Text(text = "Species: ${character.species}")
                    Text(text = "Gender: ${character.gender}")
                    Text(text = "Origin: ${character.origin.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onToggleFavorite(character.id) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    allCharacters: List<Character>,
    favoriteIds: Set<Int>,
    onCharacterClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val favoriteCharacters = allCharacters.filter { it.id in favoriteIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (favoriteCharacters.isEmpty()) {
            EmptyView("No favorites yet.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(favoriteCharacters) { character ->
                    CharacterItem(character, onCharacterClick)
                }
            }
        }
    }
}


@Composable
fun CharacterItem(character: Character, onClick: (Int) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick(character.id) },
        headlineContent = { Text(character.name) },
        supportingContent = { Text(character.species) },
        leadingContent = {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyView(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}