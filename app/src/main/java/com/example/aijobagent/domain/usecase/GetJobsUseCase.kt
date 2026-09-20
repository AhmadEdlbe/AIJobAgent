package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobFilter
import com.example.aijobagent.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobsUseCase @Inject constructor(private val repo: JobRepository) {
    operator fun invoke(filter: JobFilter): Flow<List<Job>> = repo.getFilteredJobs(filter)
    fun all(): Flow<List<Job>> = repo.getAllJobs()
    fun highMatch(): Flow<List<Job>> = repo.getHighMatchJobs()
}
