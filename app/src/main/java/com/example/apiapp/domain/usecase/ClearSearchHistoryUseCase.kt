package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.RickAndMortyRepository
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor(
    private val repository: RickAndMortyRepository
) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}
