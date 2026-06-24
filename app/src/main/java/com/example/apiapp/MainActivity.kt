package com.example.apiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apiapp.data.api.RickAndMortyApi
import com.example.apiapp.data.repository.RickAndMortyRepository
import com.example.apiapp.ui.*
import com.example.apiapp.ui.theme.ApiappTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .build()
            .create(RickAndMortyApi::class.java)

        val repository = RickAndMortyRepository(api)

        enableEdgeToEdge()
        setContent {
            ApiappTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModel.provideFactory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        CharacterListScreen(
                            state = uiState,
                            onSearchChange = { viewModel.onSearchQueryChange(it) },
                            onCharacterClick = { id ->
                                navController.navigate("detail/$id")
                            },
                            onRetry = { viewModel.retryListLoad() },
                            onFavoritesClick = {
                                navController.navigate("favorites")
                            }
                        )
                    }
                    composable(
                        route = "detail/{characterId}",
                        arguments = listOf(navArgument("characterId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("characterId") ?: 0

                        LaunchedEffect(id) {
                            viewModel.loadCharacterDetail(id)
                        }

                        val detailState by viewModel.detailUiState.collectAsState()

                        CharacterDetailScreen(
                            state = detailState,
                            isFavorite = uiState.favourites.contains(id),
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onBack = { navController.popBackStack() },
                            onRetry = { viewModel.loadCharacterDetail(id) }
                        )
                    }
                    composable("favorites") {
                        FavoritesScreen(
                            allCharacters = uiState.characters,
                            favoriteIds = uiState.favourites,
                            onCharacterClick = { id ->
                                navController.navigate("detail/$id")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
