package com.example.apiapp.data.repository

import com.example.apiapp.data.db.UserDao
import com.example.apiapp.data.db.UserEntity
import com.example.apiapp.data.preferences.SettingsDataStore
import com.example.apiapp.domain.model.User
import com.example.apiapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val settingsDataStore: SettingsDataStore
) : UserRepository {

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { User(it.id, it.name, it.avatarResName) }
        }
    }

    override suspend fun createUser(name: String, avatarResName: String): Int {
        // room возвращает long id сгенерированной строки, приводим к int для соответствия доменной модели
        val id = userDao.insertUser(UserEntity(name = name, avatarResName = avatarResName))
        return id.toInt()
    }

    override suspend fun deleteUser(id: Int) {
        userDao.deleteUser(id)
    }

    override fun getActiveUserId(): Flow<Int?> {
        return settingsDataStore.activeUserId
    }

    override suspend fun setActiveUserId(userId: Int?) {
        settingsDataStore.setActiveUserId(userId)
    }
}
