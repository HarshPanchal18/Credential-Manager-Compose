package com.example.credential_manager.di

import com.example.credential_manager.data.CredManagerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BindingModule {
    @Provides
    fun providesRepository() = CredManagerRepository()
}
