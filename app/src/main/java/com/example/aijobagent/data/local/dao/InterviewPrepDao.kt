package com.example.aijobagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aijobagent.data.local.entity.InterviewPrepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewPrepDao {
    @Query("SELECT * FROM interview_preps WHERE jobId = :jobId LIMIT 1")
    suspend fun getForJob(jobId: String): InterviewPrepEntity?

    @Query("SELECT * FROM interview_preps WHERE jobId = :jobId LIMIT 1")
    fun getForJobFlow(jobId: String): Flow<InterviewPrepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InterviewPrepEntity)
}
