package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.RickAndMortyRepository
import javax.inject.Inject

class AddSearchHistoryUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend operator fun invoke(query: String) {
        repository.addSearchQuery(query)
    }
}
