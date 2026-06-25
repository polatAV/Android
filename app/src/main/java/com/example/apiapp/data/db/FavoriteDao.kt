package com.example.apiapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favourites WHERE userId = :userId")
    fun getAllFavourites(userId: Int): Flow<List<FavoriteCharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(character: FavoriteCharacterEntity)

    @Query("SELECT * FROM favourites WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getFavoriteCharacterById(id: Int, userId: Int): FavoriteCharacterEntity?

    @Query("DELETE FROM favourites WHERE id = :id AND userId = :userId")
    suspend fun deleteFavourite(id: Int, userId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE id = :id AND userId = :userId)")
    suspend fun isFavouriteSync(id: Int, userId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE id = :id AND userId = :userId)")
    fun isFavouriteFlow(id: Int, userId: Int): Flow<Boolean>

    @Transaction
    suspend fun toggleFavourite(character: FavoriteCharacterEntity) {
        if (isFavouriteSync(character.id, character.userId)) {
            deleteFavourite(character.id, character.userId)
        } else {
            insertFavourite(character)
        }
    }
}
