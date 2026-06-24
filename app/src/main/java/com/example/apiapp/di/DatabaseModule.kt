package com.example.apiapp.di

import android.content.Context
import androidx.room.Room
import com.example.apiapp.data.db.AppDatabase
import com.example.apiapp.data.db.CharacterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "favourites_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }
}