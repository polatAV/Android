package com.example.apiapp.data.repository

import com.example.apiapp.data.api.RickAndMortyApi
import com.example.apiapp.data.db.CachedCharacterDao
import com.example.apiapp.data.db.FavoriteDao
import com.example.apiapp.data.db.toCachedEntity
import com.example.apiapp.data.db.toDomain
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
    private val favoriteDao: FavoriteDao,
    private val cachedCharacterDao: CachedCharacterDao
) : RickAndMortyRepository {

    override suspend fun getCharacters(name: String?): Result<List<Character>> {
        return try {
            val response = api.getCharacters(name)
            Result.success(response.results.map { it.toDomain() })
        } catch (e: retrofit2.HttpException) {
            // api rick and morty возвращает 404 ошибку, если персонажи по фильтру поиска не найдены
            if (e.code() == 404) {
                Result.success(emptyList())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCharacter(id: Int, userId: Int?): Result<Character> {
        return try {
            val fresh = api.getCharacter(id).toDomain()
            try {
                // кэшируем свежие данные из сети, чтобы поддерживать оффлайн-режим
                cachedCharacterDao.insertCachedCharacters(listOf(fresh.toCachedEntity()))
            } catch (dbEx: Exception) {
                android.util.Log.e("RickAndMortyRepository", "failed to save character to cache", dbEx)
            }
            Result.success(fresh)
        } catch (e: Exception) {
            try {
                // ищем сначала в избранном, затем в глобальном кэше
                val localEntity = if (userId != null) {
                    favoriteDao.getFavoriteCharacterById(id, userId)
                } else {
                    null
                }
                if (localEntity != null) {
                    Result.success(localEntity.toDomain())
                } else {
                    val cachedEntity = cachedCharacterDao.getCachedCharacterById(id)
                    if (cachedEntity != null) {
                        Result.success(cachedEntity.toDomain())
                    } else {
                        Result.failure(e)
                    }
                }
            } catch (dbEx: Exception) {
                android.util.Log.e("RickAndMortyRepository", "failed to fetch from local database during fallback", dbEx)
                Result.failure(e)
            }
        }
    }

    override suspend fun getCachedCharacters(): List<Character> {
        return cachedCharacterDao.getCachedCharacters().map { it.toDomain() }
    }

    override fun getCachedCharactersFlow(): Flow<List<Character>> {
        return cachedCharacterDao.getCachedCharactersFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCachedCharacters(characters: List<Character>) {
        val entities = characters.map { it.toCachedEntity() }
        cachedCharacterDao.insertCachedCharacters(entities)
    }

    override suspend fun clearCachedCharacters() {
        cachedCharacterDao.clearCachedCharacters()
    }
}