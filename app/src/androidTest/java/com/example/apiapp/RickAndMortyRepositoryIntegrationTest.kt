package com.example.apiapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apiapp.data.api.RickAndMortyApi
import com.example.apiapp.data.db.AppDatabase
import com.example.apiapp.data.db.CachedCharacterDao
import com.example.apiapp.data.db.FavoriteDao
import com.example.apiapp.data.model.CharacterDto
import com.example.apiapp.data.model.CharacterResponseDto
import com.example.apiapp.data.model.LocationDto
import com.example.apiapp.data.repository.RickAndMortyRepositoryImpl
import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.Location
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class FakeRickAndMortyApi : RickAndMortyApi {
    var charactersResponse: CharacterResponseDto = CharacterResponseDto(emptyList())
    var characterResponse: CharacterDto? = null
    var shouldThrowException = false
    
    override suspend fun getCharacters(name: String?): CharacterResponseDto {
        if (shouldThrowException) throw Exception("Network failure")
        return charactersResponse
    }
    
    override suspend fun getCharacter(id: Int): CharacterDto {
        if (shouldThrowException) throw Exception("Network failure")
        return characterResponse ?: throw Exception("Not found")
    }
}

@RunWith(AndroidJUnit4::class)
class RickAndMortyRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var cachedCharacterDao: CachedCharacterDao
    private lateinit var api: FakeRickAndMortyApi
    private lateinit var repository: RickAndMortyRepositoryImpl

    private val testDto = CharacterDto(
        id = 1,
        name = "Rick",
        status = "Alive",
        species = "Human",
        type = "",
        gender = "Male",
        image = "url",
        origin = LocationDto("Earth", "url"),
        location = LocationDto("Citadel", "url")
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        favoriteDao = database.favoriteDao()
        cachedCharacterDao = database.cachedCharacterDao()
        api = FakeRickAndMortyApi()
        repository = RickAndMortyRepositoryImpl(api, favoriteDao, cachedCharacterDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testGetCharactersFromApiIntegration() = runBlocking {
        api.charactersResponse = CharacterResponseDto(listOf(testDto))
        val result = repository.getCharacters(null)
        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertNotNull(list)
        assertEquals(1, list!!.size)
        assertEquals("Rick", list[0].name)
    }

    @Test
    fun testCachedCharactersIntegration() = runBlocking {
        val character = Character(
            id = 10,
            name = "Summer",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Female",
            image = "url",
            origin = Location("Earth", ""),
            location = Location("Earth", "")
        )

        repository.saveCachedCharacters(listOf(character))
        
        val cachedFlow = repository.getCachedCharactersFlow().first()
        assertEquals(1, cachedFlow.size)
        assertEquals("Summer", cachedFlow[0].name)
        
        val cachedList = repository.getCachedCharacters()
        assertEquals(1, cachedList.size)
        
        repository.clearCachedCharacters()
        assertTrue(repository.getCachedCharacters().isEmpty())
    }
}
