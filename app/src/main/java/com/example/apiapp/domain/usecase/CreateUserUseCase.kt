package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, avatarResName: String): Int {
        return userRepository.createUser(name, avatarResName)
    }
}

