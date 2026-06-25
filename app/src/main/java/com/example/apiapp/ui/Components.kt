package com.example.apiapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.model.User
import com.example.apiapp.ui.theme.ApiappTheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search

@Composable
fun User.getDisplayName(): String {
    return if (name == "Guest" || name == "Гость") {
        stringResource(R.string.default_user_name)
    } else {
        name
    }
}

@Composable
fun CharacterItem(
    character: Character,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: (Int) -> Unit,
    hasNotes: Boolean = false,
    hasTags: Boolean = false
) {
    ListItem(
        modifier = Modifier.clickable { onClick(character.id) },
        headlineContent = { Text(character.name) },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(com.example.apiapp.ui.util.translateSpecies(character.species))
                if (hasNotes) {
                    Text("📝", style = MaterialTheme.typography.bodySmall)
                }
                if (hasTags) {
                    Text("🏷️", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        leadingContent = {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size)),
                contentScale = ContentScale.Crop
            )
        },
        trailingContent = {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.add_to_favorites)
                    }
                )
            }
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

@Composable
fun UserAvatar(
    avatar: String,
    modifier: Modifier = Modifier
) {
    if (avatar.startsWith("http")) {
        AsyncImage(
            model = avatar,
            contentDescription = null,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatar, style = MaterialTheme.typography.bodyMedium)
        }
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
            isFavorite = false,
            onFavoriteClick = {},
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