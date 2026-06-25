package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.NotesRepository
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    operator fun invoke(characterId: Int, userId: Int): Flow<String?> {
        return notesRepository.getNoteForCharacter(characterId, userId)
    }
}



