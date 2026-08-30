package com.example.aijobagent.di

import android.content.Context
import androidx.room.Room
import com.example.aijobagent.data.local.AppDatabase
import com.example.aijobagent.data.local.dao.ApplicationDao
import com.example.aijobagent.data.local.dao.CoverLetterDao
import com.example.aijobagent.data.local.dao.InterviewPrepDao
import com.example.aijobagent.data.local.dao.JobDao
import com.example.aijobagent.data.local.dao.ProfileDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideJobDao(db: AppDatabase): JobDao = db.jobDao()

    @Provides
    fun provideApplicationDao(db: AppDatabase): ApplicationDao = db.applicationDao()

    @Provides
    fun provideCoverLetterDao(db: AppDatabase): CoverLetterDao = db.coverLetterDao()

    @Provides
    fun provideInterviewPrepDao(db: AppDatabase): InterviewPrepDao = db.interviewPrepDao()
}
