package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import javax.inject.Inject

class AnalyzeJobUseCase @Inject constructor(private val aiRepo: AiRepository) {
    suspend operator fun invoke(job: Job): Job = aiRepo.analyzeJob(job)
}
