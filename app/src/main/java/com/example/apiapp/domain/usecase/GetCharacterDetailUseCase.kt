package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.RickAndMortyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharacterDetailUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend fun getCharacter(id: Int): Result<Character> {
        return repository.getCharacter(id)
    }

    fun isFavoriteFlow(id: Int): Flow<Boolean> {
        return repository.isFavoriteFlow(id)
    }
}