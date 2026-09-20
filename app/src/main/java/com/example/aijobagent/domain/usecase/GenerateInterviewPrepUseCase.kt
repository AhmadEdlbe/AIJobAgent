package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.InterviewPrep
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import javax.inject.Inject

class GenerateInterviewPrepUseCase @Inject constructor(private val aiRepo: AiRepository) {
    suspend operator fun invoke(job: Job): InterviewPrep = aiRepo.generateInterviewPrep(job)
}
