package com.example.aijobagent.domain.model

enum class ApplicationStatus {
    SAVED,
    PENDING_APPROVAL,
    APPLIED,
    INTERVIEW_SCHEDULED,
    INTERVIEW_COMPLETED,
    REJECTED,
    OFFER_RECEIVED,
    WITHDRAWN
}

data class ApplicationTrack(
    val id: String,
    val jobId: String,
    val job: Job? = null,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val appliedAt: Long? = null,
    val interviewDate: Long? = null,
    val notes: String = "",
    val coverLetterId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CoverLetter(
    val id: String,
    val jobId: String,
    val content: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false
)

data class InterviewPrep(
    val id: String,
    val jobId: String,
    val questions: List<InterviewQuestion> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

data class InterviewQuestion(
    val question: String,
    val category: QuestionCategory,
    val suggestedAnswer: String
)

enum class QuestionCategory {
    TECHNICAL, HR, BEHAVIORAL, SYSTEM_DESIGN
}

data class DashboardStats(
    val totalJobsFound: Int = 0,
    val highMatchJobs: Int = 0, // >= 75%
    val applicationsSent: Int = 0,
    val interviews: Int = 0,
    val offers: Int = 0,
    val pendingApprovals: Int = 0
)
