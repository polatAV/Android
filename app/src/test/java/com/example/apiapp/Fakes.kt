package com.example.apiapp

import com.example.apiapp.domain.model.Character
import com.example.apiapp.domain.model.User
import com.example.apiapp.domain.repository.RickAndMortyRepository
import com.example.apiapp.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeRickAndMortyRepository : RickAndMortyRepository, com.example.apiapp.domain.repository.FavoriteRepository, com.example.apiapp.domain.repository.SearchHistoryRepository, com.example.apiapp.domain.repository.NotesRepository, com.example.apiapp.domain.repository.TagRepository {
    var charactersResult: Result<List<Character>> = Result.success(emptyList())
    var characterResult: Result<Character> = Result.failure(Exception("Not set"))
    
    private val favouritesFlow = MutableStateFlow<Map<Int, List<Character>>>(emptyMap())
    private val recentQueriesFlow = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    private val notesFlow = MutableStateFlow<Map<Int, Map<Int, String>>>(emptyMap())
    private val tagsFlow = MutableStateFlow<Map<Int, Map<Int, List<String>>>>(emptyMap())
    private val cachedCharactersList = mutableListOf<Character>()
    
    var getCharactersCallCount = 0
    var getCharacterCallCount = 0
    
    override suspend fun getCharacters(name: String?): Result<List<Character>> {
        getCharactersCallCount++
        return charactersResult
    }
    
    override suspend fun getCharacter(id: Int, userId: Int?): Result<Character> {
        getCharacterCallCount++
        return characterResult
    }
    
    override fun getAllFavourites(userId: Int): Flow<List<Character>> {
        return favouritesFlow.map { map -> map[userId] ?: emptyList() }
    }
    
    override fun isFavoriteFlow(id: Int, userId: Int): Flow<Boolean> {
        return favouritesFlow.map { map -> (map[userId] ?: emptyList()).any { it.id == id } }
    }
    
    override suspend fun toggleFavorite(character: Character, userId: Int) {
        val currentMap = favouritesFlow.value.toMutableMap()
        val userFavs = (currentMap[userId] ?: emptyList()).toMutableList()
        if (userFavs.any { it.id == character.id }) {
            userFavs.removeAll { it.id == character.id }
        } else {
            userFavs.add(character)
        }
        currentMap[userId] = userFavs
        favouritesFlow.value = currentMap
    }
    
    override suspend fun updateFavoriteCharacter(character: Character, userId: Int) {
        val currentMap = favouritesFlow.value.toMutableMap()
        val userFavs = (currentMap[userId] ?: emptyList()).toMutableList()
        val index = userFavs.indexOfFirst { it.id == character.id }
        if (index != -1) {
            userFavs[index] = character
        } else {
            userFavs.add(character)
        }
        currentMap[userId] = userFavs
        favouritesFlow.value = currentMap
    }
    
    override fun getRecentQueries(userId: Int): Flow<List<String>> {
        return recentQueriesFlow.map { map -> map[userId] ?: emptyList() }
    }
    
    override suspend fun addSearchQuery(query: String, userId: Int) {
        val currentMap = recentQueriesFlow.value.toMutableMap()
        val userQueries = (currentMap[userId] ?: emptyList()).toMutableList()
        userQueries.removeAll { it == query }
        userQueries.add(0, query)
        currentMap[userId] = userQueries.take(5)
        recentQueriesFlow.value = currentMap
    }
    
    override suspend fun deleteSearchQuery(query: String, userId: Int) {
        val currentMap = recentQueriesFlow.value.toMutableMap()
        val userQueries = (currentMap[userId] ?: emptyList()).toMutableList()
        userQueries.removeAll { it == query }
        currentMap[userId] = userQueries
        recentQueriesFlow.value = currentMap
    }
    
    override suspend fun clearSearchHistory(userId: Int) {
        val currentMap = recentQueriesFlow.value.toMutableMap()
        currentMap.remove(userId)
        recentQueriesFlow.value = currentMap
    }

    override fun getAllNotes(userId: Int): Flow<Map<Int, String>> {
        return notesFlow.map { map -> map[userId] ?: emptyMap() }
    }

    override fun getNoteForCharacter(characterId: Int, userId: Int): Flow<String?> {
        return notesFlow.map { map -> map[userId]?.get(characterId) }
    }

    override suspend fun saveNoteForCharacter(characterId: Int, userId: Int, noteText: String) {
        val currentMap = notesFlow.value.toMutableMap()
        val userNotes = (currentMap[userId] ?: emptyMap()).toMutableMap()
        userNotes[characterId] = noteText
        currentMap[userId] = userNotes
        notesFlow.value = currentMap
    }

    override suspend fun deleteNoteForCharacter(characterId: Int, userId: Int) {
        val currentMap = notesFlow.value.toMutableMap()
        val userNotes = (currentMap[userId] ?: emptyMap()).toMutableMap()
        userNotes.remove(characterId)
        currentMap[userId] = userNotes
        notesFlow.value = currentMap
    }

    override fun getAllTags(userId: Int): Flow<Map<Int, List<String>>> {
        return tagsFlow.map { map -> map[userId] ?: emptyMap() }
    }

    override fun getTagsForCharacter(characterId: Int, userId: Int): Flow<List<String>> {
        return tagsFlow.map { map -> map[userId]?.get(characterId) ?: emptyList() }
    }

    override suspend fun addTagToCharacter(characterId: Int, userId: Int, tag: String) {
        val currentMap = tagsFlow.value.toMutableMap()
        val userTags = (currentMap[userId] ?: emptyMap()).toMutableMap()
        val characterTags = (userTags[characterId] ?: emptyList()).toMutableList()
        if (tag !in characterTags) {
            characterTags.add(tag)
        }
        userTags[characterId] = characterTags
        currentMap[userId] = userTags
        tagsFlow.value = currentMap
    }

    override suspend fun deleteTagFromCharacter(characterId: Int, userId: Int, tag: String) {
        val currentMap = tagsFlow.value.toMutableMap()
        val userTags = (currentMap[userId] ?: emptyMap()).toMutableMap()
        val characterTags = (userTags[characterId] ?: emptyList()).toMutableList()
        characterTags.remove(tag)
        userTags[characterId] = characterTags
        currentMap[userId] = userTags
        tagsFlow.value = currentMap
    }

    override suspend fun getCachedCharacters(): List<Character> = cachedCharactersList

    override fun getCachedCharactersFlow(): Flow<List<Character>> = flow { emit(cachedCharactersList) }

    override suspend fun saveCachedCharacters(characters: List<Character>) {
        cachedCharactersList.clear()
        cachedCharactersList.addAll(characters)
    }

    override suspend fun clearCachedCharacters() {
        cachedCharactersList.clear()
    }
}

class FakeUserRepository : UserRepository {
    val usersFlow = MutableStateFlow<List<User>>(emptyList())
    val activeUserIdFlow = MutableStateFlow<Int?>(null)

    override fun getAllUsers(): Flow<List<User>> = usersFlow

    override suspend fun createUser(name: String, avatarResName: String): Int {
        val id = (usersFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        usersFlow.value = usersFlow.value + User(id, name, avatarResName)
        return id
    }

    override suspend fun deleteUser(id: Int) {
        usersFlow.value = usersFlow.value.filterNot { it.id == id }
    }

    override fun getActiveUserId(): Flow<Int?> = activeUserIdFlow

    override suspend fun setActiveUserId(userId: Int?) {
        activeUserIdFlow.value = userId
    }
}

