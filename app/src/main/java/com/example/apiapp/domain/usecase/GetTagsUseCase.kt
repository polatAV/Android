package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.TagRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(characterId: Int, userId: Int): Flow<List<String>> {
        return tagRepository.getTagsForCharacter(characterId, userId)
    }
}



