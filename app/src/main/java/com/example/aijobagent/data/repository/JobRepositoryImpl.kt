package com.example.aijobagent.data.repository

import com.example.aijobagent.data.local.dao.JobDao
import com.example.aijobagent.data.local.entity.toDomain as toLocalDomain
import com.example.aijobagent.data.local.entity.toEntity
import com.example.aijobagent.data.remote.mapper.toDomainModel
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobFilter
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.JobRepository
import com.example.aijobagent.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

class JobRepositoryImpl @Inject constructor(
    private val dao: JobDao,
    private val profileRepo: ProfileRepository,
    private val aiRepo: AiRepository,
    private val backendApi: com.example.aijobagent.data.remote.api.BackendApiService,
    private val backendConfig: com.example.aijobagent.core.config.BackendConfig
) : JobRepository {

    override fun getAllJobs(): Flow<List<Job>> = dao.getAllJobs().map { list -> list.map { it.toLocalDomain() } }

    override fun getHighMatchJobs(): Flow<List<Job>> = dao.getHighMatchJobs().map { list -> list.map { it.toLocalDomain() } }

    override suspend fun getJobById(id: String): Job? = dao.getJobById(id)?.toLocalDomain()

    override fun getJobFlowById(id: String): Flow<Job?> = dao.getJobFlowById(id).map { it?.toLocalDomain() }

    override fun getFilteredJobs(filter: JobFilter): Flow<List<Job>> {
        return dao.getAllJobs().map { entities ->
            var jobs = entities.map { it.toLocalDomain() }
            if (filter.searchQuery.isNotBlank()) {
                val q = filter.searchQuery.lowercase()
                jobs = jobs.filter { it.title.lowercase().contains(q) || it.company.lowercase().contains(q) || it.description.lowercase().contains(q) }
            }
            if (filter.techStacks.isNotEmpty()) jobs = jobs.filter { it.techStacks.any { ts -> ts in filter.techStacks } }
            if (filter.workModes.isNotEmpty()) jobs = jobs.filter { it.workMode in filter.workModes }
            if (filter.seniorities.isNotEmpty()) jobs = jobs.filter { it.seniority in filter.seniorities }
            if (filter.countries.isNotEmpty()) jobs = jobs.filter { it.country in filter.countries }
            if (filter.sources.isNotEmpty()) jobs = jobs.filter { it.source in filter.sources }
            if (filter.minMatchScore > 0) jobs = jobs.filter { it.matchPercentage >= filter.minMatchScore }
            jobs
        }
    }

    override suspend fun insertJobs(jobs: List<Job>) {
        dao.insertAll(jobs.map { it.toEntity() })
    }

    override suspend fun updateFavorite(id: String, isFav: Boolean) = dao.updateFavorite(id, isFav)

    override suspend fun deleteJob(id: String) = dao.deleteById(id)

    // Simulate scanning from multiple sources and AI analysis, with backend fallback
    override suspend fun scanJobs(query: String?): List<Job> {
        // Try backend first if enabled
        if (backendConfig.backendEnabled) {
            try {
                val profile = profileRepo.getProfileOnce()
                val req = com.example.aijobagent.data.remote.dto.ScanRequestDto(
                    query = query,
                    preferredCountries = profile?.preferredCountries,
                    preferredTitles = profile?.preferredJobTitles,
                    skills = profile?.skills
                )
                val remoteJobs = backendApi.scanJobs(req)
                val mapped = remoteJobs.map { it.toDomainModel() }
                dao.insertAll(mapped.map { it.toEntity() })
                if (mapped.isNotEmpty()) return mapped
            } catch (e: Exception) {
                // fallback to local mock
            }
        }
        val profile = profileRepo.getProfileOnce()
        val skills = profile?.skills ?: emptyList()

        // Mock job generation from sources
        val sources = listOf(
            com.example.aijobagent.domain.model.JobSource.LINKEDIN,
            com.example.aijobagent.domain.model.JobSource.INDEED,
            com.example.aijobagent.domain.model.JobSource.GLASSDOOR,
            com.example.aijobagent.domain.model.JobSource.REMOTE_OK,
            com.example.aijobagent.domain.model.JobSource.WELLFOUND
        )
        val companies = listOf("Google", "Meta", "Amazon", "Netflix", "Spotify", "Booking.com", "Careem", "Talabat", "Noon", "Microsoft")
        val titles = listOf("Senior Android Developer", "Backend Engineer (Spring Boot)", "Full Stack Developer", "Frontend React Developer", "Java Developer", "Node.js Engineer", "Android Kotlin Developer")
        val countries = listOf("UAE", "Saudi Arabia", "Germany", "USA", "UK", "Remote")
        val generated = (1..12).map { idx ->
            val title = titles.random()
            val company = companies.random()
            val country = countries.random()
            Job(
                id = "job_${System.currentTimeMillis()}_$idx",
                title = title,
                company = company,
                description = "We are looking for $title to join $company. Must have experience in ${skills.take(3).joinToString(", ").ifEmpty { "Java, Kotlin, Spring Boot" }}. Location: $country. This is a ${if (Random.nextBoolean()) "remote" else "onsite"} opportunity with competitive salary.",
                location = country,
                country = country,
                workMode = listOf(com.example.aijobagent.domain.model.WorkMode.REMOTE, com.example.aijobagent.domain.model.WorkMode.HYBRID, com.example.aijobagent.domain.model.WorkMode.ONSITE).random(),
                seniority = listOf(com.example.aijobagent.domain.model.Seniority.JUNIOR, com.example.aijobagent.domain.model.Seniority.MID_LEVEL, com.example.aijobagent.domain.model.Seniority.SENIOR).random(),
                techStacks = generateTech(title),
                source = sources.random(),
                url = "https://example.com/jobs/$idx",
                salaryMin = 3000 + Random.nextInt(5000),
                salaryMax = 8000 + Random.nextInt(10000),
                postedAt = System.currentTimeMillis() - Random.nextLong(1, 7) * 24 * 60 * 60 * 1000,
                requirements = listOf("Kotlin", "Java", "Spring Boot", "Android", "React").shuffled().take(4)
            )
        }
        // Analyze each via AI repo
        val analyzed = generated.map { job ->
            try { aiRepo.analyzeJob(job) } catch (_: Exception) { job }
        }
        dao.insertAll(analyzed.map { it.toEntity() })
        return analyzed
    }

    private fun generateTech(title: String): List<com.example.aijobagent.domain.model.TechStack> {
        return when {
            title.contains("Android") -> listOf(com.example.aijobagent.domain.model.TechStack.ANDROID, com.example.aijobagent.domain.model.TechStack.JAVA, com.example.aijobagent.domain.model.TechStack.SPRING_BOOT)
            title.contains("Backend") -> listOf(com.example.aijobagent.domain.model.TechStack.BACKEND, com.example.aijobagent.domain.model.TechStack.JAVA, com.example.aijobagent.domain.model.TechStack.SPRING_BOOT)
            title.contains("Frontend") -> listOf(com.example.aijobagent.domain.model.TechStack.FRONTEND, com.example.aijobagent.domain.model.TechStack.REACT, com.example.aijobagent.domain.model.TechStack.NODE_JS)
            title.contains("Full Stack") -> listOf(com.example.aijobagent.domain.model.TechStack.FULL_STACK, com.example.aijobagent.domain.model.TechStack.REACT, com.example.aijobagent.domain.model.TechStack.SPRING_BOOT)
            else -> listOf(com.example.aijobagent.domain.model.TechStack.JAVA, com.example.aijobagent.domain.model.TechStack.SPRING_BOOT)
        }
    }
}
