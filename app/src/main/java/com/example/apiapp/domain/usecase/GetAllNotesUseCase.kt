package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.NotesRepository
import javax.inject.Inject

class GetAllNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    operator fun invoke(userId: Int): Flow<Map<Int, String>> = notesRepository.getAllNotes(userId)
}



