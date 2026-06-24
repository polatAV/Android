package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
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
    private val getFavoritesUseCase = GetFavoritesUseCase(repository)
    
    private val testCharacters = listOf(
        Character(1, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", ""))
    )

    @Test
    fun testFavoritesMapping() = runTest {
        repository.setFavourites(testCharacters)
        val viewModel = FavoritesViewModel(getFavoritesUseCase)
        
        viewModel.favoritesUiState.filter { it is FavoritesUiState.Success }.first()
        val state = viewModel.favoritesUiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(testCharacters, (state as FavoritesUiState.Success).characters)
    }
}
