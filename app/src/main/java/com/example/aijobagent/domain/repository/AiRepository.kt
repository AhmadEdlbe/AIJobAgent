package com.example.aijobagent.domain.repository

import com.example.aijobagent.domain.model.CoverLetter
import com.example.aijobagent.domain.model.InterviewPrep
import com.example.aijobagent.domain.model.Job

interface AiRepository {
    suspend fun analyzeJob(job: Job): Job
    suspend fun generateCoverLetter(job: Job): CoverLetter
    suspend fun generateInterviewPrep(job: Job): InterviewPrep
}
