package com.example.apiapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteCharacterEntity::class,
        SearchHistoryEntity::class,
        UserEntity::class,
        CharacterNoteEntity::class,
        CharacterTagEntity::class,
        CachedCharacterEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
    abstract fun cachedCharacterDao(): CachedCharacterDao
}