package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.CoverLetter
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import javax.inject.Inject

class GenerateCoverLetterUseCase @Inject constructor(private val aiRepo: AiRepository) {
    suspend operator fun invoke(job: Job): CoverLetter = aiRepo.generateCoverLetter(job)
}
