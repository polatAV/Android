package com.example.apiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apiapp.ui.CharacterListScreen
import com.example.apiapp.ui.CharacterDetailScreen
import com.example.apiapp.ui.FavoritesScreen
import com.example.apiapp.ui.ListViewModel
import com.example.apiapp.ui.DetailViewModel
import com.example.apiapp.ui.FavoritesViewModel
import com.example.apiapp.ui.theme.ApiappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ApiappTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        val viewModel: ListViewModel = hiltViewModel()
                        val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
                        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                        val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

                        CharacterListScreen(
                            state = uiState,
                            searchQuery = searchQuery,
                            statusFilter = statusFilter,
                            errorEvents = viewModel.errorEvents,
                            onSearchChange = { viewModel.onSearchQueryChange(it) },
                            onSearchSubmit = { viewModel.saveSearchQuery(searchQuery) },
                            onStatusFilterChange = { viewModel.onStatusFilterChange(it) },
                            onCharacterClick = { id ->
                                viewModel.saveSearchQuery(searchQuery)
                                navController.navigate("detail/$id")
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeleteHistoryQuery = { viewModel.deleteHistoryQuery(it) },
                            onClearHistory = { viewModel.clearHistory() },
                            onRetry = { viewModel.retry() },
                            onFavoritesClick = {
                                navController.navigate("favorites")
                            }
                        )
                    }
                    composable(
                        route = "detail/{characterId}",
                        arguments = listOf(navArgument("characterId") { type = NavType.IntType })
                    ) {
                        val viewModel: DetailViewModel = hiltViewModel()
                        val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
                        val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

                        CharacterDetailScreen(
                            state = detailState,
                            isFavorite = isFavorite,
                            errorEvents = viewModel.errorEvents,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onBack = { navController.popBackStack() },
                            onRetry = { viewModel.retry() }
                        )
                    }
                    composable("favorites") {
                        val viewModel: FavoritesViewModel = hiltViewModel()
                        val favoritesState by viewModel.favoritesUiState.collectAsStateWithLifecycle()

                        FavoritesScreen(
                            state = favoritesState,
                            onCharacterClick = { id ->
                                navController.navigate("detail/$id")
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
