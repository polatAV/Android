package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    state: DetailUiState,
    isFavorite: Boolean,
    noteText: String,
    tags: List<String>,
    snackbarEvents: Flow<String>,
    onToggleFavorite: (Character) -> Unit,
    onSaveNote: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarEvents) {
        snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (state) {
            is DetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is DetailUiState.Error -> ErrorView(state.message, onRetry, modifier = Modifier.padding(padding))
            is DetailUiState.Success -> {
                val character = state.character
                val layoutDirection = LocalLayoutDirection.current
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            start = padding.calculateStartPadding(layoutDirection),
                            end = padding.calculateEndPadding(layoutDirection)
                        )
                        .consumeWindowInsets(padding)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(bottom = padding.calculateBottomPadding())
                        .padding(dimensionResource(id = R.dimen.padding_medium)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = stringResource(R.string.character_image_desc, character.name),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.detail_image_size)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Text(text = character.name, style = MaterialTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.character_status, com.example.apiapp.ui.util.translateStatus(character.status)))
                    Text(text = stringResource(R.string.character_species, com.example.apiapp.ui.util.translateSpecies(character.species)))
                    Text(text = stringResource(R.string.character_gender, com.example.apiapp.ui.util.translateGender(character.gender)))
                    Text(text = stringResource(R.string.character_origin, com.example.apiapp.ui.util.translateOrigin(character.origin.name)))
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Button(onClick = { onToggleFavorite(character) }) {
                        val favText = if (isFavorite) {
                            stringResource(R.string.remove_from_favorites)
                        } else {
                            stringResource(R.string.add_to_favorites)
                        }
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = favText
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        Text(favText)
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    PersonalNotesSection(
                        noteText = noteText,
                        tags = tags,
                        onSaveNote = onSaveNote,
                        onToggleTag = onToggleTag
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailScreenPreview() {
    ApiappTheme {
        CharacterDetailScreen(
            state = DetailUiState.Success(
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
                )
            ),
            isFavorite = false,
            noteText = "Genius scientist",
            tags = listOf("Genius", "Sci-Fi"),
            snackbarEvents = emptyFlow(),
            onToggleFavorite = {},
            onSaveNote = {},
            onToggleTag = {},
            onBack = {},
            onRetry = {}
        )
    }
}