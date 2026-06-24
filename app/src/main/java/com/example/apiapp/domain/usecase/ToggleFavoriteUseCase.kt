package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.RickAndMortyRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend operator fun invoke(character: Character) {
        repository.toggleFavorite(character)
    }
}