package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(userId: Int): Flow<List<Character>> {
        return favoriteRepository.getAllFavourites(userId)
    }
}


