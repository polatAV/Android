package com.example.apiapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CharacterEntity::class, SearchHistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}