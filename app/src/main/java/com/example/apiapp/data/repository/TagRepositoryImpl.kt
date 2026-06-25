package com.example.apiapp.data.repository

import com.example.apiapp.data.db.TagDao
import com.example.apiapp.data.db.CharacterTagEntity
import com.example.apiapp.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(userId: Int): Flow<Map<Int, List<String>>> {
        return tagDao.getAllTags(userId).map { entities ->
            entities.groupBy { it.characterId }
                .mapValues { entry -> entry.value.map { it.tagName } }
        }
    }

    override fun getTagsForCharacter(characterId: Int, userId: Int): Flow<List<String>> {
        return tagDao.getTagsForCharacter(characterId, userId).map { entities ->
            entities.map { it.tagName }
        }
    }

    override suspend fun addTagToCharacter(characterId: Int, userId: Int, tag: String) {
        tagDao.insertTag(CharacterTagEntity(characterId = characterId, userId = userId, tagName = tag))
    }

    override suspend fun deleteTagFromCharacter(characterId: Int, userId: Int, tag: String) {
        tagDao.deleteTagByName(characterId, userId, tag)
    }
}
