package com.example.aijobagent.data.remote.api

import com.example.aijobagent.data.remote.dto.*
import retrofit2.http.*

interface BackendApiService {
    // Auth
    @POST("auth/register")
    suspend fun registerDevice(@Body body: Map<String, String>): TokenResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenResponseDto

    // Profile
    @GET("profile")
    suspend fun getProfile(): UserProfileRemoteDto

    @PUT("profile")
    suspend fun putProfile(@Body dto: UserProfileRemoteDto): UserProfileRemoteDto

    // Jobs
    @GET("jobs")
    suspend fun listJobs(
        @Query("q") q: String? = null,
        @Query("workMode") workMode: String? = null,
        @Query("seniority") seniority: String? = null,
        @Query("source") source: String? = null,
        @Query("minMatch") minMatch: Int = 0,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageResponse<JobRemoteDto>

    @GET("jobs/{id}")
    suspend fun getJob(@Path("id") id: String): JobRemoteDto

    @GET("jobs/high-match")
    suspend fun highMatch(): List<JobRemoteDto>

    @POST("jobs/scan")
    suspend fun scanJobs(@Body req: ScanRequestDto?): List<JobRemoteDto>

    @PATCH("jobs/{id}/favorite")
    suspend fun favorite(@Path("id") id: String, @Query("fav") fav: Boolean): JobRemoteDto

    // AI
    @POST("ai/analyze/{jobId}")
    suspend fun analyze(@Path("jobId") id: String): JobRemoteDto

    @POST("ai/cover-letter/{jobId}")
    suspend fun coverLetter(@Path("jobId") id: String): CoverLetterRemoteDto

    @POST("ai/interview-prep/{jobId}")
    suspend fun interviewPrep(@Path("jobId") id: String): InterviewPrepRemoteDto

    // Applications
    @GET("applications")
    suspend fun getApplications(@Query("status") status: String? = null): List<ApplicationRemoteDto>

    @POST("applications")
    suspend fun createApplication(@Body body: Map<String, String>): ApplicationRemoteDto

    @PATCH("applications/{id}/status")
    suspend fun updateStatus(@Path("id") id: String, @Body body: Map<String, String>): ApplicationRemoteDto

    @DELETE("applications/{id}")
    suspend fun deleteApplication(@Path("id") id: String)

    // Dashboard
    @GET("dashboard/stats")
    suspend fun dashboardStats(): DashboardStatsRemoteDto
}

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)
