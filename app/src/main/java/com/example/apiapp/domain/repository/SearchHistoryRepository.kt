package com.example.apiapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentQueries(userId: Int): Flow<List<String>>
    suspend fun addSearchQuery(query: String, userId: Int)
    suspend fun deleteSearchQuery(query: String, userId: Int)
    suspend fun clearSearchHistory(userId: Int)
}
