package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Int?> = userRepository.getActiveUserId()
}

