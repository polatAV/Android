package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    state: DetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: (Character) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
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
                        .padding(dimensionResource(id = R.dimen.padding_medium)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.detail_image_size)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Text(text = character.name, style = MaterialTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.character_status, character.status))
                    Text(text = stringResource(R.string.character_species, character.species))
                    Text(text = stringResource(R.string.character_gender, character.gender))
                    Text(text = stringResource(R.string.character_origin, character.origin.name))
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
            onToggleFavorite = {},
            onBack = {},
            onRetry = {}
        )
    }
}