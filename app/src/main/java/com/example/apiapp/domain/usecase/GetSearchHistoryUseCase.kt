package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    operator fun invoke(userId: Int): Flow<List<String>> {
        return searchHistoryRepository.getRecentQueries(userId)
    }
}



