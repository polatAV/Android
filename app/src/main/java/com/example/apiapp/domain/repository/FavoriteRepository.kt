package com.example.apiapp.domain.repository

import com.example.apiapp.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavourites(userId: Int): Flow<List<Character>>
    fun isFavoriteFlow(id: Int, userId: Int): Flow<Boolean>
    suspend fun toggleFavorite(character: Character, userId: Int)
    suspend fun updateFavoriteCharacter(character: Character, userId: Int)
}
