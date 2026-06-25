package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "search_history",
    primaryKeys = ["query", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SearchHistoryEntity(
    val query: String,
    val userId: Int,
    val timestamp: Long
)
