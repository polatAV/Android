package com.example.apiapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.apiapp.domain.repository.TagRepository
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(userId: Int): Flow<Map<Int, List<String>>> = tagRepository.getAllTags(userId)
}



