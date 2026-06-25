package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location

@Entity(
    tableName = "favourites",
    primaryKeys = ["id", "userId"],
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
data class FavoriteCharacterEntity(
    val id: Int,
    val userId: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val originName: String,
    val locationName: String
)

fun FavoriteCharacterEntity.toDomain(): Character {
    return Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        origin = Location(name = originName, url = ""),
        location = Location(name = locationName, url = "")
    )
}

fun Character.toFavoriteEntity(userId: Int): FavoriteCharacterEntity {
    return FavoriteCharacterEntity(
        id = id,
        userId = userId,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        originName = origin.name,
        locationName = location.name
    )
}
