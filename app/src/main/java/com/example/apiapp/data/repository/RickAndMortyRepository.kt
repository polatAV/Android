package com.example.apiapp.data.repository

import com.example.apiapp.data.api.RickAndMortyApi
import com.example.apiapp.data.model.Character

class RickAndMortyRepository(private val api: RickAndMortyApi) {

    suspend fun getCharacters(name: String? = null): Result<List<Character>> {
        return try {
            val response = api.getCharacters(name)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCharacter(id: Int): Result<Character> {
        return try {
            Result.success(api.getCharacter(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
