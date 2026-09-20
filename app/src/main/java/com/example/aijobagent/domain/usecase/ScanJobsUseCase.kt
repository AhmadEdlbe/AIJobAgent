package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.JobRepository
import javax.inject.Inject

class ScanJobsUseCase @Inject constructor(private val repo: JobRepository) {
    suspend operator fun invoke(query: String? = null): List<Job> = repo.scanJobs(query)
}
