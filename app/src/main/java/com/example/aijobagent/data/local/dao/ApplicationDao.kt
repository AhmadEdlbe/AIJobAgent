package com.example.aijobagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aijobagent.data.local.entity.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    suspend fun getByJobId(jobId: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY updatedAt DESC")
    fun getByStatus(status: String): Flow<List<ApplicationEntity>>

    @Query("SELECT COUNT(*) FROM applications")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM applications WHERE status IN ('INTERVIEW_SCHEDULED','INTERVIEW_COMPLETED')")
    fun countInterviewsFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM applications WHERE status = 'OFFER_RECEIVED'")
    fun countOffersFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteById(id: String)
}
