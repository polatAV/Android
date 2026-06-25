package com.example.apiapp.domain.repository

import com.example.apiapp.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface RickAndMortyRepository {
    suspend fun getCharacters(name: String? = null): Result<List<Character>>
    suspend fun getCharacter(id: Int, userId: Int? = null): Result<Character>

    // --- Локальный кэш персонажей (offline-first) ---
    suspend fun getCachedCharacters(): List<Character>
    fun getCachedCharactersFlow(): Flow<List<Character>>
    suspend fun saveCachedCharacters(characters: List<Character>)
    suspend fun clearCachedCharacters()
}