package com.example.aijobagent.di

import com.example.aijobagent.data.repository.AiRepositoryImpl
import com.example.aijobagent.data.repository.ApplicationRepositoryImpl
import com.example.aijobagent.data.repository.JobRepositoryImpl
import com.example.aijobagent.data.repository.ProfileRepositoryImpl
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.ApplicationRepository
import com.example.aijobagent.domain.repository.JobRepository
import com.example.aijobagent.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository

    @Binds
    @Singleton
    abstract fun bindApplicationRepository(impl: ApplicationRepositoryImpl): ApplicationRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
}
