package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_tags",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["characterId", "userId", "tagName"], unique = true)]
)
data class CharacterTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val characterId: Int,
    val userId: Int,
    val tagName: String
)
