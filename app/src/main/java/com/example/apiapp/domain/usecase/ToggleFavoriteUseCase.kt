package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(character: Character, userId: Int) {
        favoriteRepository.toggleFavorite(character, userId)
    }
}


