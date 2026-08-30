package com.example.aijobagent.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileRemoteDto(
    val id: String?,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val linkedInUrl: String?,
    val gitHubUrl: String?,
    val resumeText: String?,
    val resumeFileName: String?,
    val preferredCountries: List<String>?,
    val preferredJobTitles: List<String>?,
    val skills: List<String>?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class JobRemoteDto(
    val id: String,
    val title: String,
    val company: String,
    val description: String?,
    val location: String?,
    val country: String?,
    @Json(name = "workMode") val workMode: String?,
    @Json(name = "seniority") val seniority: String?,
    val techStacks: List<String>?,
    val source: String?,
    val url: String?,
    val salaryMin: Int?,
    val salaryMax: Int?,
    val currency: String?,
    val postedAt: String?,
    val requirements: List<String>?,
    val isFavorite: Boolean?,
    val matchPercentage: Int?,
    val matchingSkills: List<String>?,
    val missingSkills: List<String>?,
    val experienceFit: String?,
    val salaryFit: String?,
    val whyMatches: String?,
    val whyNotMatches: String?,
    val analyzedAt: String?
)

@JsonClass(generateAdapter = true)
data class ApplicationRemoteDto(
    val id: String,
    val jobId: String,
    val job: JobRemoteDto?,
    val status: String,
    val appliedAt: String?,
    val interviewDate: String?,
    val notes: String?,
    val coverLetterId: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class CoverLetterRemoteDto(
    val id: String,
    val jobId: String,
    val content: String,
    val generatedAt: String?,
    val isEdited: Boolean?
)

@JsonClass(generateAdapter = true)
data class InterviewPrepRemoteDto(
    val id: String,
    val jobId: String,
    val questions: List<QuestionDto>?,
    val generatedAt: String?
) {
    @JsonClass(generateAdapter = true)
    data class QuestionDto(val question: String, val category: String, val suggestedAnswer: String)
}

@JsonClass(generateAdapter = true)
data class DashboardStatsRemoteDto(
    val totalJobsFound: Long,
    val highMatchJobs: Long,
    val applicationsSent: Long,
    val interviews: Long,
    val offers: Long,
    val pendingApprovals: Long
)

@JsonClass(generateAdapter = true)
data class ScanRequestDto(
    val query: String?,
    val preferredCountries: List<String>?,
    val preferredTitles: List<String>?,
    val skills: List<String>?
)

@JsonClass(generateAdapter = true)
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val deviceId: String,
    val expiresIn: Long
)
