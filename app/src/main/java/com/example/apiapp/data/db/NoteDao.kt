package com.example.apiapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM character_notes WHERE userId = :userId")
    fun getAllNotes(userId: Int): Flow<List<CharacterNoteEntity>>

    @Query("SELECT * FROM character_notes WHERE characterId = :characterId AND userId = :userId LIMIT 1")
    fun getNoteForCharacter(characterId: Int, userId: Int): Flow<CharacterNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CharacterNoteEntity)

    @Query("DELETE FROM character_notes WHERE characterId = :characterId AND userId = :userId")
    suspend fun deleteNote(characterId: Int, userId: Int)
}
