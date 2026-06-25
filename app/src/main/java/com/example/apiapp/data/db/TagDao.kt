package com.example.apiapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM character_tags WHERE userId = :userId")
    fun getAllTags(userId: Int): Flow<List<CharacterTagEntity>>

    @Query("SELECT * FROM character_tags WHERE characterId = :characterId AND userId = :userId")
    fun getTagsForCharacter(characterId: Int, userId: Int): Flow<List<CharacterTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: CharacterTagEntity)

    @Query("DELETE FROM character_tags WHERE characterId = :characterId AND userId = :userId AND tagName = :tagName")
    suspend fun deleteTagByName(characterId: Int, userId: Int, tagName: String)
}
