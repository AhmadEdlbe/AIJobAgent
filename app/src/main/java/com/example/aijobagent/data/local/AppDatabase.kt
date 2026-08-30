package com.example.aijobagent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aijobagent.data.local.dao.ApplicationDao
import com.example.aijobagent.data.local.dao.CoverLetterDao
import com.example.aijobagent.data.local.dao.InterviewPrepDao
import com.example.aijobagent.data.local.dao.JobDao
import com.example.aijobagent.data.local.dao.ProfileDao
import com.example.aijobagent.data.local.entity.ApplicationEntity
import com.example.aijobagent.data.local.entity.CoverLetterEntity
import com.example.aijobagent.data.local.entity.InterviewPrepEntity
import com.example.aijobagent.data.local.entity.JobEntity
import com.example.aijobagent.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        JobEntity::class,
        ApplicationEntity::class,
        CoverLetterEntity::class,
        InterviewPrepEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun jobDao(): JobDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun coverLetterDao(): CoverLetterDao
    abstract fun interviewPrepDao(): InterviewPrepDao

    companion object {
        const val DATABASE_NAME = "ai_job_agent_db"
    }
}