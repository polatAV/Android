package com.example.apiapp.domain.repository

import com.example.apiapp.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface RickAndMortyRepository {
    suspend fun getCharacters(name: String? = null): Result<List<Character>>
    suspend fun getCharacter(id: Int): Result<Character>
    fun getAllFavourites(): Flow<List<Character>>
    fun isFavoriteFlow(id: Int): Flow<Boolean>
    suspend fun toggleFavorite(character: Character)
    fun getRecentQueries(): Flow<List<String>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()
}