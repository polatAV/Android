package com.example.apiapp.di

import android.content.Context
import androidx.room.Room
import com.example.apiapp.data.db.AppDatabase
import com.example.apiapp.data.db.FavoriteDao
import com.example.apiapp.data.db.SearchHistoryDao
import com.example.apiapp.data.db.UserDao
import com.example.apiapp.data.db.NoteDao
import com.example.apiapp.data.db.TagDao
import com.example.apiapp.data.db.CachedCharacterDao
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
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideCachedCharacterDao(database: AppDatabase): CachedCharacterDao = database.cachedCharacterDao()
}