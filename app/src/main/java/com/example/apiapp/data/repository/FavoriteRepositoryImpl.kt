package com.example.apiapp.data.repository

import com.example.apiapp.data.db.FavoriteDao
import com.example.apiapp.data.db.toFavoriteEntity
import com.example.apiapp.data.db.toDomain
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getAllFavourites(userId: Int): Flow<List<Character>> {
        return favoriteDao.getAllFavourites(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavoriteFlow(id: Int, userId: Int): Flow<Boolean> {
        return favoriteDao.isFavouriteFlow(id, userId)
    }

    override suspend fun toggleFavorite(character: Character, userId: Int) {
        favoriteDao.toggleFavourite(character.toFavoriteEntity(userId))
    }

    override suspend fun updateFavoriteCharacter(character: Character, userId: Int) {
        favoriteDao.insertFavourite(character.toFavoriteEntity(userId))
    }
}
