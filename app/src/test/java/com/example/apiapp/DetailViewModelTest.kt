package com.example.apiapp

import androidx.lifecycle.SavedStateHandle
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.usecase.GetCharacterDetailUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.ui.DetailViewModel
import com.example.apiapp.ui.DetailUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRickAndMortyRepository()
    private val getCharacterDetailUseCase = GetCharacterDetailUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    
    private val testCharacter = Character(
        42, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", "")
    )

    @Test
    fun testDetailViewModelReadsIdFromSavedStateHandle() = runTest {
        repository.characterResult = Result.success(testCharacter)
        
        val savedStateHandle = SavedStateHandle(mapOf("characterId" to 42))
        val viewModel = DetailViewModel(
            getCharacterDetailUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )
        
        viewModel.detailUiState.filter { it is DetailUiState.Success }.first()
        
        val state = viewModel.detailUiState.value
        assertTrue(state is DetailUiState.Success)
        assertEquals(testCharacter, (state as DetailUiState.Success).character)
        assertEquals(1, repository.getCharacterCallCount)
    }

    @Test
    fun testToggleFavoriteLogic() = runTest {
        repository.characterResult = Result.success(testCharacter)
        
        val savedStateHandle = SavedStateHandle(mapOf("characterId" to 42))
        val viewModel = DetailViewModel(
            getCharacterDetailUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )
        
        assertEquals(false, viewModel.isFavorite.value)
        
        viewModel.toggleFavorite(testCharacter)
        
        viewModel.isFavorite.filter { it }.first()
        assertEquals(true, viewModel.isFavorite.value)
        
        viewModel.toggleFavorite(testCharacter)
        
        viewModel.isFavorite.filter { !it }.first()
        assertEquals(false, viewModel.isFavorite.value)
    }
}
