package com.example.aijobagent.domain.model

enum class JobSource {
    LINKEDIN, INDEED, GLASSDOOR, WELLFOUND, REMOTE_OK, WE_WORK_REMOTELY, COMPANY_PAGE, MANUAL
}

enum class WorkMode {
    REMOTE, HYBRID, ONSITE
}

enum class Seniority {
    JUNIOR, MID_LEVEL, SENIOR, LEAD, EXECUTIVE
}

enum class TechStack {
    FULL_STACK, BACKEND, FRONTEND, ANDROID, JAVA, SPRING_BOOT, REACT, NODE_JS
}

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val description: String,
    val location: String,
    val country: String = "",
    val workMode: WorkMode = WorkMode.REMOTE,
    val seniority: Seniority = Seniority.MID_LEVEL,
    val techStacks: List<TechStack> = emptyList(),
    val source: JobSource = JobSource.MANUAL,
    val url: String = "",
    val salaryMin: Int? = null,
    val salaryMax: Int? = null,
    val currency: String = "USD",
    val postedAt: Long = System.currentTimeMillis(),
    val requirements: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    // AI matching fields
    val matchPercentage: Int = 0,
    val matchingSkills: List<String> = emptyList(),
    val missingSkills: List<String> = emptyList(),
    val experienceFit: String = "",
    val salaryFit: String = "",
    val whyMatches: String = "",
    val whyNotMatches: String = "",
    val analyzedAt: Long? = null
)

data class JobFilter(
    val techStacks: Set<TechStack> = emptySet(),
    val workModes: Set<WorkMode> = emptySet(),
    val seniorities: Set<Seniority> = emptySet(),
    val minMatchScore: Int = 0,
    val countries: Set<String> = emptySet(),
    val sources: Set<JobSource> = emptySet(),
    val searchQuery: String = ""
)
