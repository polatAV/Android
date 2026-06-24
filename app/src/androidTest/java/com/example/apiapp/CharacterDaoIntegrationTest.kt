package com.example.apiapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apiapp.data.db.AppDatabase
import com.example.apiapp.data.db.CharacterDao
import com.example.apiapp.data.db.CharacterEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CharacterDao

    private val testEntity = CharacterEntity(
        id = 1,
        name = "Rick",
        status = "Alive",
        species = "Human",
        type = "",
        gender = "Male",
        image = "url",
        originName = "Earth",
        locationName = "Citadel"
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.characterDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndReadFavorite() = runBlocking {
        dao.insertFavourite(testEntity)
        val favourites = dao.getAllFavourites().first()
        assertEquals(1, favourites.size)
        assertEquals("Rick", favourites[0].name)
    }

    @Test
    fun testToggleFavoriteBehavior() = runBlocking {
        // Toggle 1: Not present -> Should insert
        dao.toggleFavourite(testEntity)
        var favourites = dao.getAllFavourites().first()
        assertEquals(1, favourites.size)
        
        // Toggle 2: Present -> Should delete
        dao.toggleFavourite(testEntity)
        favourites = dao.getAllFavourites().first()
        assertTrue(favourites.isEmpty())
    }

    @Test
    fun testDuplicatePrevention() = runBlocking {
        // Inserting same entity twice should replace, not create duplicates
        dao.insertFavourite(testEntity)
        dao.insertFavourite(testEntity)
        val favourites = dao.getAllFavourites().first()
        assertEquals(1, favourites.size)
    }
}
