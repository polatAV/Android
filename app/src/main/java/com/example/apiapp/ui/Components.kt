package com.example.apiapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.theme.ApiappTheme

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
                modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size)),
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
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Button(onClick = onRetry) { 
            Text(stringResource(R.string.retry)) 
        }
    }
}

@Composable
fun EmptyView(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterItemPreview() {
    ApiappTheme {
        CharacterItem(
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
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingViewPreview() {
    ApiappTheme {
        LoadingView()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    ApiappTheme {
        ErrorView(message = "Network error", onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    ApiappTheme {
        EmptyView(message = "No results found")
    }
}