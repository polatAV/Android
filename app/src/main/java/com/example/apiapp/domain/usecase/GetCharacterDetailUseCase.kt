package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.FavoriteRepository
import com.example.apiapp.domain.repository.RickAndMortyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharacterDetailUseCase @Inject constructor(
    private val rickAndMortyRepository: RickAndMortyRepository,
    private val favoriteRepository: FavoriteRepository
) {
    suspend fun getCharacter(id: Int, userId: Int? = null): Result<Character> {
        return rickAndMortyRepository.getCharacter(id, userId)
    }

    fun isFavoriteFlow(id: Int, userId: Int): Flow<Boolean> {
        return favoriteRepository.isFavoriteFlow(id, userId)
    }
}
