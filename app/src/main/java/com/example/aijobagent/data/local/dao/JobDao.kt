package com.example.aijobagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aijobagent.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY postedAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobEntity?

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    fun getJobFlowById(id: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE matchPercentage >= 75 ORDER BY matchPercentage DESC")
    fun getHighMatchJobs(): Flow<List<JobEntity>>

    @Query("SELECT COUNT(*) FROM jobs")
    fun getJobCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM jobs WHERE matchPercentage >= 75")
    fun getHighMatchCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Query("UPDATE jobs SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM jobs")
    suspend fun clearAll()
}
