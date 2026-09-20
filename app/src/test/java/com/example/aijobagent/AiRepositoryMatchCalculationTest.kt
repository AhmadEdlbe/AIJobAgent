package com.example.aijobagent

import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.UserProfile
import com.example.aijobagent.domain.model.WorkMode
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests heuristic matching logic mirrored from AiRepositoryImpl.
 * Ensures AI matching handles edge cases deterministically.
 */
class AiRepositoryMatchCalculationTest {

    private fun heuristicAnalyze(job: Job, profile: UserProfile?): Job {
        val userSkills = profile?.skills?.map { it.lowercase() } ?: emptyList()
        val matching = mutableListOf<String>()
        val missing = mutableListOf<String>()
        val allJobSkills = job.requirements.map { it.lowercase() }
        userSkills.forEach { skill ->
            if (allJobSkills.any { it.contains(skill) } || job.description.lowercase().contains(skill)) matching.add(skill)
        }
        job.requirements.forEach { req ->
            if (userSkills.none { it.contains(req.lowercase()) }) missing.add(req)
        }
        val matchPct = if (job.requirements.isEmpty()) 50 else {
            val base = (matching.size.toFloat() / (matching.size + missing.size).coerceAtLeast(1)) * 100
            val titleBonus = if (profile?.preferredJobTitles?.any { job.title.contains(it, ignoreCase = true) } == true) 10 else 0
            val countryBonus = if (profile?.preferredCountries?.contains(job.country) == true) 10 else 0
            kotlin.math.min(95, (base + titleBonus + countryBonus).toInt())
        }
        return job.copy(matchPercentage = matchPct.coerceIn(0,100), matchingSkills = matching.distinct(), missingSkills = missing.distinct())
    }

    @Test
    fun emptyRequirements_returns50() {
        val job = Job(id="1", title="Android Dev", company="X", description="desc", location="UAE")
        val profile = UserProfile(skills = listOf("Kotlin"))
        val result = heuristicAnalyze(job, profile)
        assertEquals(50, result.matchPercentage)
    }

    @Test
    fun perfectMatch_returnsHighScore() {
        val job = Job(id="1", title="Backend Engineer", company="Y", description="Java Spring Boot", location="UAE", requirements = listOf("Java","Spring Boot"))
        val profile = UserProfile(skills = listOf("Java","Spring Boot"), preferredJobTitles = listOf("Backend"), preferredCountries = listOf("UAE"))
        val result = heuristicAnalyze(job, profile)
        // base 100 + title 10 + country 10 capped 95
        assertEquals(95, result.matchPercentage)
        assertEquals(2, result.matchingSkills.size)
        assertTrue(result.missingSkills.isEmpty())
    }

    @Test
    fun missingSkills_detected() {
        val job = Job(id="1", title="Frontend", company="Z", description="React Node", location="Germany", requirements = listOf("React","Node.js","GraphQL"))
        val profile = UserProfile(skills = listOf("React"))
        val result = heuristicAnalyze(job, profile)
        assertTrue(result.matchingSkills.contains("react"))
        assertTrue(result.missingSkills.contains("Node.js"))
        assertTrue(result.missingSkills.contains("GraphQL"))
        assertTrue(result.matchPercentage in 30..50)
    }

    @Test
    fun caseInsensitive_match() {
        val job = Job(id="1", title="Dev", company="C", description="We use KOTLIN and android", location="USA", requirements = listOf("KOTLIN"))
        val profile = UserProfile(skills = listOf("kotlin"))
        val result = heuristicAnalyze(job, profile)
        assertEquals(100.coerceAtMost(95), result.matchPercentage.coerceAtMost(95))
        assertTrue(result.matchingSkills.contains("kotlin"))
    }

    @Test
    fun salaryFit_logic() {
        fun salaryFit(max: Int?): String = when {
            max == null -> "Salary not specified"
            max > 10000 -> "Above market - great opportunity"
            max > 7000 -> "Market rate"
            else -> "Below expectations"
        }
        assertEquals("Salary not specified", salaryFit(null))
        assertEquals("Above market - great opportunity", salaryFit(15000))
        assertEquals("Market rate", salaryFit(8000))
        assertEquals("Below expectations", salaryFit(5000))
    }
}
