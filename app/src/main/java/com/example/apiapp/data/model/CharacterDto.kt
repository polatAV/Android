package com.example.apiapp.data.model

import androidx.annotation.Keep
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.google.gson.annotations.SerializedName

@Keep
data class CharacterResponseDto(
    @SerializedName("results")
    val results: List<CharacterDto>
)

@Keep
data class CharacterDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("species")
    val species: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("origin")
    val origin: LocationDto,
    @SerializedName("location")
    val location: LocationDto
)

@Keep
data class LocationDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)

fun LocationDto.toDomain(): Location {
    return Location(
        name = name,
        url = url
    )
}

fun CharacterDto.toDomain(): Character {
    return Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        origin = origin.toDomain(),
        location = location.toDomain()
    )
}