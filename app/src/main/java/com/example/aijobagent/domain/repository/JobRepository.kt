package com.example.aijobagent.domain.repository

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobFilter
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getAllJobs(): Flow<List<Job>>
    fun getHighMatchJobs(): Flow<List<Job>>
    suspend fun getJobById(id: String): Job?
    fun getJobFlowById(id: String): Flow<Job?>
    fun getFilteredJobs(filter: JobFilter): Flow<List<Job>>
    suspend fun insertJobs(jobs: List<Job>)
    suspend fun updateFavorite(id: String, isFav: Boolean)
    suspend fun deleteJob(id: String)
    suspend fun scanJobs(query: String? = null): List<Job>
}
