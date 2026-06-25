package com.example.apiapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apiapp.data.db.AppDatabase
import com.example.apiapp.data.db.CachedCharacterEntity
import com.example.apiapp.data.db.CharacterDao
import com.example.apiapp.data.db.CharacterEntity
import com.example.apiapp.data.db.CharacterNoteEntity
import com.example.apiapp.data.db.CharacterTagEntity
import com.example.apiapp.data.db.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CharacterDao

    private val testUser1 = UserEntity(id = 1, name = "Rick", avatarResName = "avatar_rick")
    private val testUser2 = UserEntity(id = 2, name = "Morty", avatarResName = "avatar_morty")

    private val testEntityUser1 = CharacterEntity(
        id = 1,
        userId = 1,
        name = "Rick Sanchez",
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
        runBlocking {
            database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
            ).allowMainThreadQueries().build()
            dao = database.characterDao()
            dao.insertUser(testUser1)
            dao.insertUser(testUser2)
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndReadFavorite() = runBlocking {
        dao.insertFavourite(testEntityUser1)
        val favourites = dao.getAllFavourites(1).first()
        assertEquals(1, favourites.size)
        assertEquals("Rick Sanchez", favourites[0].name)

        // для пользователя 2 список должен быть пуст (изоляция)
        val favourites2 = dao.getAllFavourites(2).first()
        assertTrue(favourites2.isEmpty())
    }

    @Test
    fun testToggleFavoriteBehavior() = runBlocking {
        // toggle 1: not present -> should insert
        dao.toggleFavourite(testEntityUser1)
        var favourites = dao.getAllFavourites(1).first()
        assertEquals(1, favourites.size)
        
        // toggle 2: present -> should delete
        dao.toggleFavourite(testEntityUser1)
        favourites = dao.getAllFavourites(1).first()
        assertTrue(favourites.isEmpty())
    }

    @Test
    fun testDuplicatePrevention() = runBlocking {
        dao.insertFavourite(testEntityUser1)
        dao.insertFavourite(testEntityUser1)
        val favourites = dao.getAllFavourites(1).first()
        assertEquals(1, favourites.size)
    }

    @Test
    fun testNoteOperations() = runBlocking {
        val note = CharacterNoteEntity(
            characterId = 1,
            userId = 1,
            noteText = "Great scientist",
            updatedAt = System.currentTimeMillis()
        )

        dao.insertNote(note)
        val retrievedNote = dao.getNoteForCharacter(1, 1).first()
        assertNotNull(retrievedNote)
        assertEquals("Great scientist", retrievedNote?.noteText)

        // обновление заметки
        val updatedNote = note.copy(noteText = "Genius scientist")
        dao.insertNote(updatedNote)
        val retrievedNote2 = dao.getNoteForCharacter(1, 1).first()
        assertEquals("Genius scientist", retrievedNote2?.noteText)

        // удаление заметки
        dao.deleteNote(1, 1)
        val retrievedNote3 = dao.getNoteForCharacter(1, 1).first()
        assertNull(retrievedNote3)
    }

    @Test
    fun testTagOperations() = runBlocking {
        val tag1 = CharacterTagEntity(characterId = 1, userId = 1, tagName = "Hero")
        val tag2 = CharacterTagEntity(characterId = 1, userId = 1, tagName = "Sci-Fi")

        dao.insertTag(tag1)
        dao.insertTag(tag2)

        val tags = dao.getTagsForCharacter(1, 1).first()
        assertEquals(2, tags.size)
        assertTrue(tags.any { it.tagName == "Hero" })

        // удаление одного тега
        dao.deleteTagByName(1, 1, "Hero")
        val remainingTags = dao.getTagsForCharacter(1, 1).first()
        assertEquals(1, remainingTags.size)
        assertEquals("Sci-Fi", remainingTags[0].tagName)
    }

    @Test
    fun testCachedCharacters() = runBlocking {
        val cachedChar = CachedCharacterEntity(
            id = 10,
            name = "Summer Smith",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Female",
            image = "url",
            originName = "Earth",
            locationName = "Earth",
            cachedAt = System.currentTimeMillis()
        )

        dao.insertCachedCharacters(listOf(cachedChar))
        val cached = dao.getCachedCharacters()
        assertEquals(1, cached.size)
        assertEquals("Summer Smith", cached[0].name)

        dao.clearCachedCharacters()
        assertTrue(dao.getCachedCharacters().isEmpty())
    }
}
