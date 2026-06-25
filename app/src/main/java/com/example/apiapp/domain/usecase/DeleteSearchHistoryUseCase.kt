package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(query: String, userId: Int) {
        searchHistoryRepository.deleteSearchQuery(query, userId)
    }
}



