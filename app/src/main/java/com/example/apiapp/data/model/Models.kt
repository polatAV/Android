package com.example.apiapp.data.model

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    val results: List<Character>
)

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val origin: Location,
    val location: Location
)

data class Location(
    val name: String,
    val url: String
)
