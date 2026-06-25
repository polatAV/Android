package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageSearchHistoryUseCase @Inject constructor(
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val addSearchHistoryUseCase: AddSearchHistoryUseCase,
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) {
    operator fun invoke(userId: Int): Flow<List<String>> = getSearchHistoryUseCase(userId)
    
    suspend fun add(query: String, userId: Int) {
        addSearchHistoryUseCase(query, userId)
    }
    
    suspend fun delete(query: String, userId: Int) {
        deleteSearchHistoryUseCase(query, userId)
    }
    
    suspend fun clear(userId: Int) {
        clearSearchHistoryUseCase(userId)
    }
}


