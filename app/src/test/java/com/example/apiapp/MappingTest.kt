package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import com.example.apiapp.data.model.CharacterDto
import com.example.apiapp.data.model.LocationDto
import com.example.apiapp.data.model.toDomain
import com.example.apiapp.data.db.FavoriteCharacterEntity
import com.example.apiapp.data.db.toDomain
import com.example.apiapp.data.db.toFavoriteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MappingTest {
    @Test
    fun testDtoToDomain() {
        val dto = CharacterDto(
            id = 1,
            name = "Rick",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            image = "url",
            origin = LocationDto("Earth", "url_earth"),
            location = LocationDto("Citadel", "url_citadel")
        )
        val domain = dto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Rick", domain.name)
        assertEquals("Alive", domain.status)
        assertEquals("Earth", domain.origin.name)
        assertEquals("url_earth", domain.origin.url)
    }

    @Test
    fun testEntityToDomainAndViceVersa() {
        val domain = Character(
            id = 2,
            name = "Morty",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            image = "url",
            origin = Location("Earth", ""),
            location = Location("Citadel", "")
        )
        val entity = domain.toFavoriteEntity(userId = 1)
        assertEquals(2, entity.id)
        assertEquals(1, entity.userId)
        assertEquals("Morty", entity.name)
        assertEquals("Earth", entity.originName)
        
        val restoredDomain = entity.toDomain()
        assertEquals(domain.id, restoredDomain.id)
        assertEquals(domain.name, restoredDomain.name)
        assertEquals(domain.origin.name, restoredDomain.origin.name)
    }
}

