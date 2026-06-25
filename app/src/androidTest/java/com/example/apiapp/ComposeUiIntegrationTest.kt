package com.example.apiapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.ui.CharacterListScreen
import com.example.apiapp.ui.ListUiState
import com.example.apiapp.ui.CharacterItemUiState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeUiIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCharacters = listOf(
        Character(
            id = 1,
            name = "Morty Smith",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            image = "",
            origin = Location("Earth", ""),
            location = Location("Earth", "")
        )
    )

    private val uiItems = testCharacters.map { CharacterItemUiState(it, isFavorite = false) }

    @Test
    fun testUiSuccessStateDisplaysCharacters() {
        composeTestRule.setContent {
            CharacterListScreen(
                state = ListUiState.Success(uiItems, queries = emptyList(), isFromCache = false, currentUser = null, allUsers = emptyList()),
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
                onFavoritesClick = {},
                onSettingsClick = {},
                onSelectUser = {},
                onCreateUser = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Morty Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Human").assertIsDisplayed()
    }

    @Test
    fun testUiErrorStateClickRetryTriggersCallback() {
        var retryClicked = false

        composeTestRule.setContent {
            CharacterListScreen(
                state = ListUiState.Error("Failed to fetch characters"),
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
                onRetry = { retryClicked = true },
                onFavoritesClick = {},
                onSettingsClick = {},
                onSelectUser = {},
                onCreateUser = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Failed to fetch characters").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Retry").performClick()
        
        assertTrue(retryClicked)
    }

    @Test
    fun testUiItemClickTriggersCallback() {
        var clickedCharacterId = -1

        composeTestRule.setContent {
            CharacterListScreen(
                state = ListUiState.Success(uiItems, queries = emptyList(), isFromCache = false, currentUser = null, allUsers = emptyList()),
                searchQuery = "",
                statusFilter = "All",
                errorEvents = emptyFlow(),
                onSearchChange = {},
                onSearchSubmit = {},
                onStatusFilterChange = {},
                onCharacterClick = { clickedCharacterId = it },
                onToggleFavorite = {},
                onDeleteHistoryQuery = {},
                onClearHistory = {},
                onRetry = {},
                onFavoritesClick = {},
                onSettingsClick = {},
                onSelectUser = {},
                onCreateUser = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Morty Smith").performClick()
        
        assertEquals(1, clickedCharacterId)
    }
}
