package com.example.apiapp.domain.repository

import com.example.apiapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun createUser(name: String, avatarResName: String): Int
    suspend fun deleteUser(id: Int)
    fun getActiveUserId(): Flow<Int?>
    suspend fun setActiveUserId(userId: Int?)
}
