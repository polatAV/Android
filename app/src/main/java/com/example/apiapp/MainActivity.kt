package com.example.apiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.apiapp.ui.CharacterListScreen
import com.example.apiapp.ui.CharacterDetailScreen
import com.example.apiapp.ui.FavoritesScreen
import com.example.apiapp.ui.SettingsScreen
import com.example.apiapp.ui.ListViewModel
import com.example.apiapp.ui.DetailViewModel
import com.example.apiapp.ui.FavoritesViewModel
import com.example.apiapp.ui.SettingsViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.example.apiapp.ui.theme.ApiappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeState by settingsViewModel.appTheme.collectAsStateWithLifecycle()
            val languageState by settingsViewModel.appLanguage.collectAsStateWithLifecycle()
            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val locale = remember(languageState) {
                if (languageState == "system") {
                    java.util.Locale.getDefault()
                } else {
                    java.util.Locale(languageState)
                }
            }
            val context = LocalContext.current
            val localizedContext = remember(context, locale) {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(locale)
                val localized = context.createConfigurationContext(config)
                LocalizedContextWrapper(context, localized)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                ApiappTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
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
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                },
                                onSelectUser = { viewModel.selectUser(it) },
                                onCreateUser = { name, avatar -> viewModel.createAndSelectUser(name, avatar) }
                            )
                        }
                        composable(
                            route = "detail/{characterId}",
                            arguments = listOf(navArgument("characterId") { type = NavType.IntType }),
                            deepLinks = listOf(navDeepLink { uriPattern = "apiapp://detail/{characterId}" })
                        ) {
                            val viewModel: DetailViewModel = hiltViewModel()
                            val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
                            val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
                            val noteText by viewModel.note.collectAsStateWithLifecycle()
                            val tags by viewModel.tags.collectAsStateWithLifecycle()

                            CharacterDetailScreen(
                                state = detailState,
                                isFavorite = isFavorite,
                                noteText = noteText,
                                tags = tags,
                                snackbarEvents = viewModel.snackbarEvents,
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onSaveNote = { viewModel.saveNote(it) },
                                onToggleTag = { viewModel.toggleTag(it) },
                                onBack = {
                                    if (navController.currentDestination?.route?.startsWith("detail") == true) {
                                        navController.popBackStack()
                                    }
                                },
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
                                onSelectTag = { viewModel.selectTag(it) },
                                onBack = {
                                    if (navController.currentDestination?.route == "favorites") {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            val settingsViewModel: SettingsViewModel = hiltViewModel()
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = {
                                    if (navController.currentDestination?.route == "settings") {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

class LocalizedContextWrapper(
    base: android.content.Context,
    private val localizedContext: android.content.Context
) : android.content.ContextWrapper(base) {
    override fun getResources(): android.content.res.Resources = localizedContext.resources
    override fun getAssets(): android.content.res.AssetManager = localizedContext.assets
    override fun getTheme(): android.content.res.Resources.Theme = localizedContext.theme
}
