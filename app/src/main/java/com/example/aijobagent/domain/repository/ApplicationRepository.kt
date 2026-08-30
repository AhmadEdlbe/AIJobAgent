package com.example.aijobagent.domain.repository

import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.model.DashboardStats
import kotlinx.coroutines.flow.Flow

interface ApplicationRepository {
    fun getAll(): Flow<List<ApplicationTrack>>
    fun getByStatus(status: ApplicationStatus): Flow<List<ApplicationTrack>>
    suspend fun getById(id: String): ApplicationTrack?
    suspend fun getByJobId(jobId: String): ApplicationTrack?
    suspend fun upsert(track: ApplicationTrack)
    suspend fun updateStatus(id: String, status: ApplicationStatus)
    suspend fun delete(id: String)
    fun getDashboardStats(): Flow<DashboardStats>
}
