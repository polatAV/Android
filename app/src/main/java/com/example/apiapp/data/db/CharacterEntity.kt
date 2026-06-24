package com.example.apiapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location

@Entity(tableName = "favourites")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val originName: String,
    val locationName: String
)

fun CharacterEntity.toDomain(): Character {
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

fun Character.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
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