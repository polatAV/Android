package com.example.apiapp.data.repository

import com.example.apiapp.data.db.NoteDao
import com.example.apiapp.data.db.CharacterNoteEntity
import com.example.apiapp.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NotesRepository {

    override fun getAllNotes(userId: Int): Flow<Map<Int, String>> {
        return noteDao.getAllNotes(userId).map { entities ->
            entities.associate { it.characterId to it.noteText }
        }
    }

    override fun getNoteForCharacter(characterId: Int, userId: Int): Flow<String?> {
        return noteDao.getNoteForCharacter(characterId, userId).map { it?.noteText }
    }

    override suspend fun saveNoteForCharacter(characterId: Int, userId: Int, noteText: String) {
        noteDao.insertNote(CharacterNoteEntity(characterId, userId, noteText, System.currentTimeMillis()))
    }

    override suspend fun deleteNoteForCharacter(characterId: Int, userId: Int) {
        noteDao.deleteNote(characterId, userId)
    }
}
