package com.example.apiapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(userId: Int): Flow<Map<Int, String>>
    fun getNoteForCharacter(characterId: Int, userId: Int): Flow<String?>
    suspend fun saveNoteForCharacter(characterId: Int, userId: Int, noteText: String)
    suspend fun deleteNoteForCharacter(characterId: Int, userId: Int)
}
