package com.example.apiapp.domain.usecase

import com.example.apiapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageUsersUseCase @Inject constructor(
    private val getActiveUserIdUseCase: GetActiveUserIdUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val setActiveUserUseCase: SetActiveUserUseCase
) {
    fun getActiveUserId(): Flow<Int?> = getActiveUserIdUseCase()
    
    fun getUsers(): Flow<List<User>> = getUsersUseCase()
    
    suspend fun createUser(name: String, avatarResName: String): Int {
        return createUserUseCase(name, avatarResName)
    }
    
    suspend fun setActiveUser(userId: Int?) {
        setActiveUserUseCase(userId)
    }
}

