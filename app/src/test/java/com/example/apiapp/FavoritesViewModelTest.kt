package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.domain.usecase.GetActiveUserIdUseCase
import com.example.apiapp.domain.usecase.GetAllNotesUseCase
import com.example.apiapp.domain.usecase.GetAllTagsUseCase
import com.example.apiapp.domain.repository.RickAndMortyRepository
import com.example.apiapp.ui.FavoritesViewModel
import com.example.apiapp.ui.FavoritesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRickAndMortyRepository()
    private val userRepository = FakeUserRepository()
    private val getFavoritesUseCase = GetFavoritesUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    private val getActiveUserIdUseCase = GetActiveUserIdUseCase(userRepository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getAllTagsUseCase = GetAllTagsUseCase(repository)
    
    private val testCharacters = listOf(
        Character(1, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", ""))
    )

    @Test
    fun testFavoritesMapping() = runTest {
        userRepository.setActiveUserId(1)
        repository.toggleFavorite(testCharacters[0], 1)

        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = getFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getActiveUserIdUseCase = getActiveUserIdUseCase,
            getAllNotesUseCase = getAllNotesUseCase,
            getAllTagsUseCase = getAllTagsUseCase,
            rickAndMortyRepository = repository
        )
        
        viewModel.favoritesUiState.filter { it is FavoritesUiState.Success }.first()
        val state = viewModel.favoritesUiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(testCharacters, (state as FavoritesUiState.Success).favorites)
    }
}

