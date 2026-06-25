package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.RickAndMortyRepository
import javax.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend operator fun invoke(query: String) {
        repository.deleteSearchQuery(query)
    }
}
