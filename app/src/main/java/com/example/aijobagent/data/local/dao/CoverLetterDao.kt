package com.example.aijobagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aijobagent.data.local.entity.CoverLetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoverLetterDao {
    @Query("SELECT * FROM cover_letters WHERE jobId = :jobId LIMIT 1")
    suspend fun getForJob(jobId: String): CoverLetterEntity?

    @Query("SELECT * FROM cover_letters WHERE jobId = :jobId LIMIT 1")
    fun getForJobFlow(jobId: String): Flow<CoverLetterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CoverLetterEntity)

    @Query("DELETE FROM cover_letters WHERE id = :id")
    suspend fun deleteById(id: String)
}
