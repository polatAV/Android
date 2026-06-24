package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.repository.RickAndMortyRepository
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import com.example.apiapp.ui.ListViewModel
import com.example.apiapp.ui.ListUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeRickAndMortyRepository : RickAndMortyRepository {
    var charactersResult: Result<List<Character>> = Result.success(emptyList())
    var characterResult: Result<Character> = Result.failure(Exception("Not set"))
    
    private val favouritesFlow = MutableStateFlow<List<Character>>(emptyList())
    
    var getCharactersCallCount = 0
    var getCharacterCallCount = 0
    
    override suspend fun getCharacters(name: String?): Result<List<Character>> {
        getCharactersCallCount++
        return charactersResult
    }
    
    override suspend fun getCharacter(id: Int): Result<Character> {
        getCharacterCallCount++
        return characterResult
    }
    
    override fun getAllFavourites(): Flow<List<Character>> {
        return favouritesFlow
    }
    
    override fun isFavoriteFlow(id: Int): Flow<Boolean> {
        return favouritesFlow.map { list -> list.any { it.id == id } }
    }
    
    override suspend fun toggleFavorite(character: Character) {
        val current = favouritesFlow.value.toMutableList()
        if (current.any { it.id == character.id }) {
            current.removeAll { it.id == character.id }
        } else {
            current.add(character)
        }
        favouritesFlow.value = current
    }
    
    fun setFavourites(list: List<Character>) {
        favouritesFlow.value = list
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRickAndMortyRepository()
    private val getCharactersUseCase = GetCharactersUseCase(repository)
    
    private val testCharacters = listOf(
        Character(1, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", ""))
    )

    @Test
    fun testInitialStateIsLoading() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = ListViewModel(getCharactersUseCase)
        
        assertEquals(ListUiState.Loading, viewModel.listUiState.value)
    }

    @Test
    fun testSuccessfulDataLoad() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = ListViewModel(getCharactersUseCase)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        
        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(testCharacters, (state as ListUiState.Success).characters)
    }

    @Test
    fun testErrorLoadingAndRetryFlow() = runTest {
        repository.charactersResult = Result.failure(Exception("Network error"))
        val viewModel = ListViewModel(getCharactersUseCase)
        
        viewModel.listUiState.filter { it is ListUiState.Error }.first()
        assertEquals(ListUiState.Error("Network error"), viewModel.listUiState.value)
        assertEquals(1, repository.getCharactersCallCount)
        
        repository.charactersResult = Result.success(testCharacters)
        viewModel.retry()
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(testCharacters, (state as ListUiState.Success).characters)
        assertEquals(2, repository.getCharactersCallCount)
    }

    @Test
    fun testSearchQueryDebounceAndCancellation() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = ListViewModel(getCharactersUseCase)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        assertEquals(1, repository.getCharactersCallCount)
        
        viewModel.onSearchQueryChange("R")
        viewModel.onSearchQueryChange("Ri")
        viewModel.onSearchQueryChange("Rick")
        
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        assertEquals(2, repository.getCharactersCallCount)
        assertEquals("Rick", viewModel.searchQuery.value)
    }

    @Test
    fun testEmptySearchResultYieldsEmptyState() = runTest {
        repository.charactersResult = Result.success(emptyList())
        val viewModel = ListViewModel(getCharactersUseCase)
        
        viewModel.listUiState.filter { it is ListUiState.Empty }.first()
        assertEquals(ListUiState.Empty, viewModel.listUiState.value)
    }

    @Test
    fun testSequenceOfEmissionsFlow() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = ListViewModel(getCharactersUseCase)
        
        val emissions = mutableListOf<ListUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listUiState.collect { emissions.add(it) }
        }
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        
        assertTrue(emissions.first() is ListUiState.Loading)
        assertTrue(emissions.last() is ListUiState.Success)
        
        job.cancel()
    }
}
