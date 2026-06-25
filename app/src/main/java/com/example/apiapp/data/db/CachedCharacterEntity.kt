package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location

@Entity(tableName = "cached_characters")
data class CachedCharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val originName: String,
    val locationName: String,
    val cachedAt: Long
)

fun CachedCharacterEntity.toDomain(): Character {
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

fun Character.toCachedEntity(cachedAt: Long = System.currentTimeMillis()): CachedCharacterEntity {
    return CachedCharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        originName = origin.name,
        locationName = location.name,
        cachedAt = cachedAt
    )
}
