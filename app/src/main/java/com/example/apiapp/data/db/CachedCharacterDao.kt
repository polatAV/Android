package com.example.apiapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedCharacterDao {
    @Query("SELECT * FROM cached_characters ORDER BY cachedAt DESC")
    suspend fun getCachedCharacters(): List<CachedCharacterEntity>

    @Query("SELECT * FROM cached_characters ORDER BY cachedAt DESC")
    fun getCachedCharactersFlow(): Flow<List<CachedCharacterEntity>>

    @Query("SELECT * FROM cached_characters WHERE id = :id LIMIT 1")
    suspend fun getCachedCharacterById(id: Int): CachedCharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedCharacters(characters: List<CachedCharacterEntity>)

    @Query("DELETE FROM cached_characters")
    suspend fun clearCachedCharacters()
}
