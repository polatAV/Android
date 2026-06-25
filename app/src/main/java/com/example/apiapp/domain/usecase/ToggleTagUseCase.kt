package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.first
import com.example.apiapp.domain.repository.TagRepository
import javax.inject.Inject

class ToggleTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(characterId: Int, userId: Int, tagName: String) {
        val cleaned = tagName.trim()
        if (cleaned.isBlank()) return

        val existingTags = tagRepository.getTagsForCharacter(characterId, userId).first()
        if (cleaned in existingTags) {
            tagRepository.deleteTagFromCharacter(characterId, userId, cleaned)
        } else {
            tagRepository.addTagToCharacter(characterId, userId, cleaned)
        }
    }
}



