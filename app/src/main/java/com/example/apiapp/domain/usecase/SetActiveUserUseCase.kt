package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.UserRepository
import javax.inject.Inject

class SetActiveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int?) {
        userRepository.setActiveUserId(userId)
    }
}

