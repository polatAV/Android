package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(userId: Int) {
        searchHistoryRepository.clearSearchHistory(userId)
    }
}



