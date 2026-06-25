package com.example.apiapp.di

import com.example.apiapp.data.repository.*
import com.example.apiapp.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRickAndMortyRepository(
        repositoryImpl: RickAndMortyRepositoryImpl
    ): RickAndMortyRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        repositoryImpl: NotesRepositoryImpl
    ): NotesRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        repositoryImpl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        repositoryImpl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        repositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
}