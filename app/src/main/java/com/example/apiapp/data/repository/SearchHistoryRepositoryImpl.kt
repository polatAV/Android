package com.example.apiapp.data.repository

import com.example.apiapp.data.db.SearchHistoryDao
import com.example.apiapp.data.db.SearchHistoryEntity
import com.example.apiapp.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getRecentQueries(userId: Int): Flow<List<String>> {
        return searchHistoryDao.getRecentQueries(userId).map { entities ->
            entities.map { it.query }
        }
    }

    override suspend fun addSearchQuery(query: String, userId: Int) {
        searchHistoryDao.insertSearchQuery(SearchHistoryEntity(query, userId, System.currentTimeMillis()))
    }

    override suspend fun deleteSearchQuery(query: String, userId: Int) {
        searchHistoryDao.deleteSearchQuery(query, userId)
    }

    override suspend fun clearSearchHistory(userId: Int) {
        searchHistoryDao.clearSearchHistory(userId)
    }
}
