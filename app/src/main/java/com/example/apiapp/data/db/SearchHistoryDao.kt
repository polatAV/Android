package com.example.apiapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT 5")
    fun getRecentQueries(userId: Int): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :query AND userId = :userId")
    suspend fun deleteSearchQuery(query: String, userId: Int)

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearSearchHistory(userId: Int)
}
