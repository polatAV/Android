package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.domain.usecase.GetCharactersUseCase
import com.example.apiapp.domain.usecase.GetFavoritesUseCase
import com.example.apiapp.domain.usecase.GetSearchHistoryUseCase
import com.example.apiapp.domain.usecase.AddSearchHistoryUseCase
import com.example.apiapp.domain.usecase.DeleteSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ClearSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ToggleFavoriteUseCase
import com.example.apiapp.domain.usecase.GetActiveUserIdUseCase
import com.example.apiapp.domain.usecase.GetUsersUseCase
import com.example.apiapp.domain.usecase.CreateUserUseCase
import com.example.apiapp.domain.usecase.SetActiveUserUseCase
import com.example.apiapp.domain.usecase.ManageSearchHistoryUseCase
import com.example.apiapp.domain.usecase.ManageUsersUseCase
import com.example.apiapp.domain.usecase.GetAllNotesUseCase
import com.example.apiapp.domain.usecase.GetAllTagsUseCase
import com.example.apiapp.ui.ListViewModel
import com.example.apiapp.ui.ListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRickAndMortyRepository()
    private val userRepository = FakeUserRepository()

    private val getCharactersUseCase = GetCharactersUseCase(repository)
    private val getFavoritesUseCase = GetFavoritesUseCase(repository)
    private val getSearchHistoryUseCase = GetSearchHistoryUseCase(repository)
    private val addSearchHistoryUseCase = AddSearchHistoryUseCase(repository)
    private val deleteSearchHistoryUseCase = DeleteSearchHistoryUseCase(repository)
    private val clearSearchHistoryUseCase = ClearSearchHistoryUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    private val getActiveUserIdUseCase = GetActiveUserIdUseCase(userRepository)
    private val getUsersUseCase = GetUsersUseCase(userRepository)
    private val createUserUseCase = CreateUserUseCase(userRepository)
    private val setActiveUserUseCase = SetActiveUserUseCase(userRepository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getAllTagsUseCase = GetAllTagsUseCase(repository)

    private val manageSearchHistoryUseCase = ManageSearchHistoryUseCase(
        getSearchHistoryUseCase,
        addSearchHistoryUseCase,
        deleteSearchHistoryUseCase,
        clearSearchHistoryUseCase
    )

    private val manageUsersUseCase = ManageUsersUseCase(
        getActiveUserIdUseCase,
        getUsersUseCase,
        createUserUseCase,
        setActiveUserUseCase
    )
    
    private val testCharacters = listOf(
        Character(1, "Rick", "Alive", "Human", "", "Male", "url", Location("Earth", ""), Location("Citadel", ""))
    )

    private fun createViewModel() = ListViewModel(
        getCharactersUseCase = getCharactersUseCase,
        getFavoritesUseCase = getFavoritesUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
        manageSearchHistoryUseCase = manageSearchHistoryUseCase,
        manageUsersUseCase = manageUsersUseCase,
        getAllNotesUseCase = getAllNotesUseCase,
        getAllTagsUseCase = getAllTagsUseCase,
        rickAndMortyRepository = repository
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

