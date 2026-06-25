package com.example.apiapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(userId: Int): Flow<Map<Int, List<String>>>
    fun getTagsForCharacter(characterId: Int, userId: Int): Flow<List<String>>
    suspend fun addTagToCharacter(characterId: Int, userId: Int, tag: String)
    suspend fun deleteTagFromCharacter(characterId: Int, userId: Int, tag: String)
}
