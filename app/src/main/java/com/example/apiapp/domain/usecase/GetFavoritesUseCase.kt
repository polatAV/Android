package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.repository.RickAndMortyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    operator fun invoke(): Flow<List<Character>> {
        return repository.getAllFavourites()
    }
}