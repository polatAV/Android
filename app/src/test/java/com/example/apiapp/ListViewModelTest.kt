package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.repository.RickAndMortyRepository
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.GetSearchHistoryUseCase
import com.example.apiapp.domain.usecase.AddSearchHistoryUseCase
import com.example.apiapp.domain.usecase.DeleteSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ClearSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
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
    private val recentQueriesFlow = MutableStateFlow<List<String>>(emptyList())
    
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
    
    override fun getRecentQueries(): Flow<List<String>> {
        return recentQueriesFlow
    }
    
    override suspend fun addSearchQuery(query: String) {
        val current = recentQueriesFlow.value.toMutableList()
        current.removeAll { it == query }
        current.add(0, query)
        recentQueriesFlow.value = current.take(5)
    }
    
    override suspend fun deleteSearchQuery(query: String) {
        val current = recentQueriesFlow.value.toMutableList()
        current.removeAll { it == query }
        recentQueriesFlow.value = current
    }
    
    override suspend fun clearSearchHistory() {
        recentQueriesFlow.value = emptyList()
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
    private val getFavoritesUseCase = GetFavoritesUseCase(repository)
    private val getSearchHistoryUseCase = GetSearchHistoryUseCase(repository)
    private val addSearchHistoryUseCase = AddSearchHistoryUseCase(repository)
    private val deleteSearchHistoryUseCase = DeleteSearchHistoryUseCase(repository)
    private val clearSearchHistoryUseCase = ClearSearchHistoryUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    
    private val testCharacters = listOf(
        Character(1, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", ""))
    )

    private fun createViewModel() = ListViewModel(
        getCharactersUseCase = getCharactersUseCase,
        getFavoritesUseCase = getFavoritesUseCase,
        getSearchHistoryUseCase = getSearchHistoryUseCase,
        addSearchHistoryUseCase = addSearchHistoryUseCase,
        deleteSearchHistoryUseCase = deleteSearchHistoryUseCase,
        clearSearchHistoryUseCase = clearSearchHistoryUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase
    )

    @Test
    fun testInitialStateIsLoading() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = createViewModel()
        
        val firstState = viewModel.listUiState.value
        assertTrue(firstState is ListUiState.Loading || firstState is ListUiState.Success)
    }

    @Test
    fun testSuccessfulDataLoad() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = createViewModel()
        
        viewModel.onSearchQueryChange("Rick")
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        
        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(testCharacters, (state as ListUiState.Success).characters.map { it.character })
    }

    @Test
    fun testErrorLoadingAndRetryFlow() = runTest {
        repository.charactersResult = Result.failure(Exception("Network error"))
        val viewModel = createViewModel()
        
        viewModel.onSearchQueryChange("Rick")
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Error }.first()
        assertEquals(ListUiState.Error("Network error"), viewModel.listUiState.value)
        assertEquals(1, repository.getCharactersCallCount)
        
        repository.charactersResult = Result.success(testCharacters)
        viewModel.retry()
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(testCharacters, (state as ListUiState.Success).characters.map { it.character })
        assertEquals(2, repository.getCharactersCallCount)
    }

    @Test
    fun testSearchQueryDebounceAndCancellation() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = createViewModel()
        
        val firstState = viewModel.listUiState.value
        assertTrue(firstState is ListUiState.Success || firstState is ListUiState.Loading)
        assertEquals(0, repository.getCharactersCallCount)
        
        viewModel.onSearchQueryChange("R")
        viewModel.onSearchQueryChange("Ri")
        viewModel.onSearchQueryChange("Rick")
        
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        assertEquals(1, repository.getCharactersCallCount)
        assertEquals("Rick", viewModel.searchQuery.value)
    }

    @Test
    fun testEmptySearchResultYieldsEmptyState() = runTest {
        repository.charactersResult = Result.success(emptyList())
        val viewModel = createViewModel()
        
        viewModel.onSearchQueryChange("NonExistent")
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Empty }.first()
        assertEquals(ListUiState.Empty("NonExistent"), viewModel.listUiState.value)
    }

    @Test
    fun testSequenceOfEmissionsFlow() = runTest {
        repository.charactersResult = Result.success(testCharacters)
        val viewModel = createViewModel()
        
        val emissions = mutableListOf<ListUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listUiState.collect { emissions.add(it) }
        }
        
        viewModel.onSearchQueryChange("Rick")
        advanceTimeBy(600)
        
        viewModel.listUiState.filter { it is ListUiState.Success }.first()
        
        assertTrue(emissions.any { it is ListUiState.Loading })
        assertTrue(emissions.any { it is ListUiState.Success })
        
        job.cancel()
    }
}
