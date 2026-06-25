package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "character_notes",
    primaryKeys = ["characterId", "userId"],
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
data class CharacterNoteEntity(
    val characterId: Int,
    val userId: Int,
    val noteText: String,
    val updatedAt: Long
)
