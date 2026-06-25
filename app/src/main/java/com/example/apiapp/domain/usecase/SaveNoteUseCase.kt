package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.NotesRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(characterId: Int, userId: Int, noteText: String) {
        if (noteText.isBlank()) {
            notesRepository.deleteNoteForCharacter(characterId, userId)
        } else {
            notesRepository.saveNoteForCharacter(characterId, userId, noteText.trim())
        }
    }
}



