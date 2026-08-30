package com.example.aijobagent.data.repository

import com.example.aijobagent.data.local.dao.CoverLetterDao
import com.example.aijobagent.data.local.dao.InterviewPrepDao
import com.example.aijobagent.data.local.entity.toEntity
import com.example.aijobagent.data.remote.mapper.toDomainModel
import com.example.aijobagent.domain.model.CoverLetter
import com.example.aijobagent.domain.model.InterviewPrep
import com.example.aijobagent.domain.model.InterviewQuestion
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.QuestionCategory
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.ProfileRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.math.min
import kotlin.random.Random

class AiRepositoryImpl @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val coverLetterDao: CoverLetterDao,
    private val interviewPrepDao: InterviewPrepDao,
    private val backendApi: com.example.aijobagent.data.remote.api.BackendApiService,
    private val openAiApi: com.example.aijobagent.data.remote.api.OpenAiApiService,
    private val backendConfig: com.example.aijobagent.core.config.BackendConfig,
    private val encryptedPrefs: com.example.aijobagent.core.security.EncryptedPrefs
) : AiRepository {

    override suspend fun analyzeJob(job: Job): Job {
        // 1. Try backend if enabled
        if (backendConfig.backendEnabled) {
            try {
                val remote = backendApi.analyze(job.id)
                return remote.toDomainModel()
            } catch (_: Exception) { /* fallback */ }
        }
        // 2. Try OpenAI direct if key available
        val openAiKey = backendConfig.openAiApiKey ?: encryptedPrefs.getString("openai_api_key", null)
        if (!openAiKey.isNullOrBlank()) {
            try {
                val profile = profileRepo.getProfileOnce()
                val analyzed = callOpenAiAnalyze(job, profile, openAiKey)
                if (analyzed != null) return analyzed
            } catch (_: Exception) { /* fallback */ }
        }
        // 3. Heuristic fallback
        val profile = profileRepo.getProfileOnce()
        val userSkills = profile?.skills?.map { it.lowercase() } ?: emptyList()

        val matching = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val allJobSkills = job.requirements.map { it.lowercase() }
        userSkills.forEach { skill ->
            if (allJobSkills.any { it.contains(skill) } || job.description.lowercase().contains(skill)) {
                matching.add(skill)
            }
        }
        job.requirements.forEach { req ->
            if (userSkills.none { it.contains(req.lowercase()) }) missing.add(req)
        }

        val matchPct = if (job.requirements.isEmpty()) 50 else {
            val base = (matching.size.toFloat() / (matching.size + missing.size).coerceAtLeast(1)) * 100
            val titleBonus = if (profile?.preferredJobTitles?.any { job.title.contains(it, ignoreCase = true) } == true) 10 else 0
            val countryBonus = if (profile?.preferredCountries?.contains(job.country) == true) 10 else 0
            min(95, (base + titleBonus + countryBonus).toInt() + Random.nextInt(-5, 6))
        }

        val experienceFit = when {
            matchPct >= 80 -> "Excellent fit - your experience aligns perfectly"
            matchPct >= 60 -> "Good fit - most requirements match your background"
            matchPct >= 40 -> "Partial fit - some gaps in required experience"
            else -> "Low fit - significant experience gap"
        }
        val salaryFit = when {
            job.salaryMax == null -> "Salary not specified"
            job.salaryMax!! > 10000 -> "Above market - great opportunity"
            job.salaryMax!! > 7000 -> "Market rate"
            else -> "Below expectations"
        }

        val whyMatches = if (matching.isEmpty()) "Your general experience may be relevant." else "Matches your skills: ${matching.joinToString(", ")}"
        val whyNot = if (missing.isEmpty()) "No major gaps identified." else "Missing: ${missing.joinToString(", ")}"

        return job.copy(
            matchPercentage = matchPct.coerceIn(0, 100),
            matchingSkills = matching.distinct(),
            missingSkills = missing.distinct(),
            experienceFit = experienceFit,
            salaryFit = salaryFit,
            whyMatches = whyMatches,
            whyNotMatches = whyNot,
            analyzedAt = System.currentTimeMillis()
        )
    }

    private suspend fun callOpenAiAnalyze(job: Job, profile: com.example.aijobagent.domain.model.UserProfile?, apiKey: String): Job? {
        return try {
            val prompt = """
                Analyze job fit. Candidate skills: ${profile?.skills} titles: ${profile?.preferredJobTitles} countries: ${profile?.preferredCountries}
                Job title: ${job.title} Company: ${job.company} Location: ${job.location} WorkMode: ${job.workMode} Seniority: ${job.seniority}
                Description: ${job.description}
                Requirements: ${job.requirements}
                TechStacks: ${job.techStacks} Salary: ${job.salaryMin}-${job.salaryMax}
                Return JSON: {"matchPercentage":0-100,"matchingSkills":[],"missingSkills":[],"experienceFit":"...","salaryFit":"...","whyMatches":"...","whyNotMatches":"..."}
                Only return JSON.
            """.trimIndent()
            val resp = openAiApi.chatCompletions(
                "Bearer $apiKey",
                com.example.aijobagent.data.remote.api.ChatRequest(
                    model = "gpt-4o-mini",
                    messages = listOf(com.example.aijobagent.data.remote.api.ChatRequest.Message("user", prompt))
                )
            )
            val content = resp.choices.firstOrNull()?.message?.content ?: return null
            parseOpenAiAnalyze(content, job)
        } catch (_: Exception) { null }
    }

    private fun parseOpenAiAnalyze(json: String, job: Job): Job? {
        return try {
            val clean = json.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
            val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(AnalyzeJson::class.java)
            val parsed = adapter.fromJson(clean) ?: return null
            job.copy(
                matchPercentage = parsed.matchPercentage.coerceIn(0,100),
                matchingSkills = parsed.matchingSkills,
                missingSkills = parsed.missingSkills,
                experienceFit = parsed.experienceFit,
                salaryFit = parsed.salaryFit,
                whyMatches = parsed.whyMatches,
                whyNotMatches = parsed.whyNotMatches,
                analyzedAt = System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    @com.squareup.moshi.JsonClass(generateAdapter = true)
    data class AnalyzeJson(
        val matchPercentage: Int,
        val matchingSkills: List<String> = emptyList(),
        val missingSkills: List<String> = emptyList(),
        val experienceFit: String = "",
        val salaryFit: String = "",
        val whyMatches: String = "",
        val whyNotMatches: String = ""
    )

    override suspend fun generateCoverLetter(job: Job): CoverLetter {
        // Try backend
        if (backendConfig.backendEnabled) {
            try {
                val remote = backendApi.coverLetter(job.id)
                val letter = CoverLetter(remote.id, remote.jobId, remote.content, System.currentTimeMillis(), remote.isEdited ?: false)
                coverLetterDao.upsert(letter.toEntity())
                return letter
            } catch (_: Exception) {}
        }
        // Try OpenAI direct
        val openAiKey = backendConfig.openAiApiKey ?: encryptedPrefs.getString("openai_api_key", null)
        if (!openAiKey.isNullOrBlank()) {
            try {
                val profile = profileRepo.getProfileOnce()
                val prompt = """
                    Write a tailored cover letter (300-400 words) for ${job.title} at ${job.company} (${job.location}) Description: ${job.description} Requirements: ${job.requirements} Candidate: ${profile?.fullName} Skills: ${profile?.skills} matching: ${job.matchingSkills}
                """.trimIndent()
                val resp = openAiApi.chatCompletions("Bearer $openAiKey", com.example.aijobagent.data.remote.api.ChatRequest(model="gpt-4o-mini", messages=listOf(com.example.aijobagent.data.remote.api.ChatRequest.Message("user", prompt))))
                val content = resp.choices.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    val letter = CoverLetter(UUID.randomUUID().toString(), job.id, content, System.currentTimeMillis(), false)
                    coverLetterDao.upsert(letter.toEntity())
                    return letter
                }
            } catch (_: Exception) {}
        }
        val profile = profileRepo.getProfileOnce()
        val name = profile?.fullName ?: "Applicant"
        val skills = profile?.skills?.joinToString(", ") ?: "relevant skills"

        val content = """
Dear Hiring Manager at ${job.company},

I am excited to apply for the ${job.title} position at ${job.company} ${if (job.location.isNotBlank()) "(${job.location})" else ""}. With experience in $skills, I am confident in my ability to contribute effectively to your team.

${job.whyMatches}

My background aligns with your requirements including ${job.requirements.take(3).joinToString(", ")}. I am particularly drawn to ${job.company} because of its innovation in the ${job.techStacks.firstOrNull()?.name ?: "technology"} space.

I would welcome the opportunity to discuss how my experience with ${matchingSkillsOrFallback(job)} can help ${job.company} achieve its goals. Thank you for considering my application.

Sincerely,
$name
${profile?.email ?: ""}
${profile?.phoneNumber ?: ""}
""".trimIndent()

        val letter = CoverLetter(
            id = UUID.randomUUID().toString(),
            jobId = job.id,
            content = content,
            generatedAt = System.currentTimeMillis(),
            isEdited = false
        )
        coverLetterDao.upsert(letter.toEntity())
        return letter
    }

    private fun matchingSkillsOrFallback(job: Job): String {
        return if (job.matchingSkills.isNotEmpty()) job.matchingSkills.take(3).joinToString(", ") else "my core technologies"
    }

    override suspend fun generateInterviewPrep(job: Job): InterviewPrep {
        if (backendConfig.backendEnabled) {
            try {
                val remote = backendApi.interviewPrep(job.id)
                val qs = remote.questions?.map {
                    InterviewQuestion(it.question, try{ QuestionCategory.valueOf(it.category)}catch(_:Exception){ QuestionCategory.TECHNICAL}, it.suggestedAnswer)
                } ?: emptyList()
                val prep = InterviewPrep(remote.id, remote.jobId, qs, System.currentTimeMillis())
                interviewPrepDao.upsert(prep.toEntity())
                return prep
            } catch (_: Exception) {}
        }
        val openAiKey = backendConfig.openAiApiKey ?: encryptedPrefs.getString("openai_api_key", null)
        if (!openAiKey.isNullOrBlank()) {
            try {
                val prompt = "Generate 7 interview Q&A for job: ${job.title} at ${job.company} Requirements: ${job.requirements} Missing: ${job.missingSkills} Categories: TECHNICAL,SYSTEM_DESIGN,HR,BEHAVIORAL Return JSON array: [{\"question\":\"...\",\"category\":\"TECHNICAL\",\"suggestedAnswer\":\"...\"}]"
                val resp = openAiApi.chatCompletions("Bearer $openAiKey", com.example.aijobagent.data.remote.api.ChatRequest(model="gpt-4o-mini", messages=listOf(com.example.aijobagent.data.remote.api.ChatRequest.Message("user", prompt))))
                val content = resp.choices.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    val json = content.substringAfter("[").substringBeforeLast("]").let { "[$it]" }
                    val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                    val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.aijobagent.data.remote.dto.InterviewPrepRemoteDto.QuestionDto::class.java)
                    val adapter = moshi.adapter<List<com.example.aijobagent.data.remote.dto.InterviewPrepRemoteDto.QuestionDto>>(type)
                    val parsed = adapter.fromJson(json)
                    if (parsed != null) {
                        val qs = parsed.map { InterviewQuestion(it.question, try{ QuestionCategory.valueOf(it.category)}catch(_:Exception){ QuestionCategory.TECHNICAL}, it.suggestedAnswer) }
                        val prep = InterviewPrep(UUID.randomUUID().toString(), job.id, qs, System.currentTimeMillis())
                        interviewPrepDao.upsert(prep.toEntity())
                        return prep
                    }
                }
            } catch (_: Exception) {}
        }
        val questions = mutableListOf<InterviewQuestion>()

        questions.add(InterviewQuestion("Explain your experience with ${job.requirements.firstOrNull() ?: "Kotlin"} and how you've used it in production.", QuestionCategory.TECHNICAL, "Discuss projects where you used this technology, challenges faced, and outcomes. Use STAR method."))
        questions.add(InterviewQuestion("How would you design a scalable system for ${job.company}'s ${job.title} role?", QuestionCategory.SYSTEM_DESIGN, "Mention microservices, caching, DB choice, scaling strategies. Draw on Spring Boot / Android experience."))
        questions.add(InterviewQuestion("What is the difference between Coroutines and Threads in Kotlin?", QuestionCategory.TECHNICAL, "Coroutines are lightweight threads managed by runtime, cooperative, less overhead. Threads are OS-level."))
        questions.add(InterviewQuestion("Why do you want to work at ${job.company}?", QuestionCategory.HR, "Research company values, products, mention alignment with your career goals."))
        questions.add(InterviewQuestion("Where do you see yourself in 5 years?", QuestionCategory.HR, "Show ambition but commitment to growing within company."))
        questions.add(InterviewQuestion("Tell me about a challenge you overcame in your previous project.", QuestionCategory.BEHAVIORAL, "Use STAR: Situation, Task, Action, Result, quantify impact."))
        if (job.missingSkills.isNotEmpty()) {
            questions.add(InterviewQuestion("You listed ${job.missingSkills.first()} as a missing skill - how would you ramp up quickly?", QuestionCategory.BEHAVIORAL, "Show learning plan: docs, courses, side projects, mentoring."))
        }

        val prep = InterviewPrep(
            id = UUID.randomUUID().toString(),
            jobId = job.id,
            questions = questions,
            generatedAt = System.currentTimeMillis()
        )
        interviewPrepDao.upsert(prep.toEntity())
        return prep
    }
}
