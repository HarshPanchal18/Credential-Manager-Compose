package com.example.credential_manager.di

import com.example.credential_manager.data.CredManagerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BindingModule { // A class that has only one instance, and it can be accessed by any other class in the application
    @Provides
    fun providesRepository() = CredManagerRepository()
}
