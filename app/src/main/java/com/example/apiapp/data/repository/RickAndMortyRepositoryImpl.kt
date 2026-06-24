package com.example.apiapp.data.repository

import com.example.apiapp.data.api.RickAndMortyApi
import com.example.apiapp.data.db.CharacterDao
import com.example.apiapp.data.db.toDomain
import com.example.apiapp.data.db.toEntity
import com.example.apiapp.data.model.toDomain
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.RickAndMortyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RickAndMortyRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val characterDao: CharacterDao
) : RickAndMortyRepository {

    override suspend fun getCharacters(name: String?): Result<List<Character>> {
        return try {
            val response = api.getCharacters(name)
            Result.success(response.results.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCharacter(id: Int): Result<Character> {
        return try {
            Result.success(api.getCharacter(id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllFavourites(): Flow<List<Character>> {
        return characterDao.getAllFavourites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavoriteFlow(id: Int): Flow<Boolean> {
        return characterDao.isFavouriteFlow(id)
    }

    override suspend fun toggleFavorite(character: Character) {
        characterDao.toggleFavourite(character.toEntity())
    }
}