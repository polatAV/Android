package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.RickAndMortyRepository
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend operator fun invoke(name: String? = null): Result<List<Character>> {
        return repository.getCharacters(name)
    }
}