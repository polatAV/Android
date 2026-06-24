package com.example.apiapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM favourites")
    fun getAllFavourites(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(character: CharacterEntity)

    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteFavourite(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE id = :id)")
    suspend fun isFavouriteSync(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE id = :id)")
    fun isFavouriteFlow(id: Int): Flow<Boolean>

    @Transaction
    suspend fun toggleFavourite(character: CharacterEntity) {
        if (isFavouriteSync(character.id)) {
            deleteFavourite(character.id)
        } else {
            insertFavourite(character)
        }
    }
}