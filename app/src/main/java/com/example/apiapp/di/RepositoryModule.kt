package com.example.apiapp.di

import com.example.apiapp.data.repository.RickAndMortyRepositoryImpl
import com.example.apiapp.domain.repository.RickAndMortyRepository
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
}