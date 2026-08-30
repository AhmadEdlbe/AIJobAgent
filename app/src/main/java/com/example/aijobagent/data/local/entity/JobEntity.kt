package com.example.aijobagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.WorkMode

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val description: String,
    val location: String,
    val country: String,
    val workMode: WorkMode,
    val seniority: Seniority,
    val techStacksCsv: String, // TechStack list as CSV
    val source: JobSource,
    val url: String,
    val salaryMin: Int?,
    val salaryMax: Int?,
    val currency: String,
    val postedAt: Long,
    val requirementsCsv: String, // "|"-delimited
    val isFavorite: Boolean,
    val matchPercentage: Int,
    val matchingSkillsCsv: String,
    val missingSkillsCsv: String,
    val experienceFit: String,
    val salaryFit: String,
    val whyMatches: String,
    val whyNotMatches: String,
    val analyzedAt: Long?
)

fun JobEntity.toDomain(): Job {
    fun splitPipe(v: String) = if (v.isEmpty()) emptyList() else v.split("|")
    fun splitTech(v: String) = if (v.isEmpty()) emptyList() else v.split(",").mapNotNull {
        try { TechStack.valueOf(it) } catch (_: Exception) { null }
    }
    return Job(
        id = id,
        title = title,
        company = company,
        description = description,
        location = location,
        country = country,
        workMode = workMode,
        seniority = seniority,
        techStacks = splitTech(techStacksCsv),
        source = source,
        url = url,
        salaryMin = salaryMin,
        salaryMax = salaryMax,
        currency = currency,
        postedAt = postedAt,
        requirements = splitPipe(requirementsCsv),
        isFavorite = isFavorite,
        matchPercentage = matchPercentage,
        matchingSkills = splitPipe(matchingSkillsCsv),
        missingSkills = splitPipe(missingSkillsCsv),
        experienceFit = experienceFit,
        salaryFit = salaryFit,
        whyMatches = whyMatches,
        whyNotMatches = whyNotMatches,
        analyzedAt = analyzedAt
    )
}

fun Job.toEntity(): JobEntity {
    return JobEntity(
        id = id,
        title = title,
        company = company,
        description = description,
        location = location,
        country = country,
        workMode = workMode,
        seniority = seniority,
        techStacksCsv = techStacks.joinToString(",") { it.name },
        source = source,
        url = url,
        salaryMin = salaryMin,
        salaryMax = salaryMax,
        currency = currency,
        postedAt = postedAt,
        requirementsCsv = requirements.joinToString("|"),
        isFavorite = isFavorite,
        matchPercentage = matchPercentage,
        matchingSkillsCsv = matchingSkills.joinToString("|"),
        missingSkillsCsv = missingSkills.joinToString("|"),
        experienceFit = experienceFit,
        salaryFit = salaryFit,
        whyMatches = whyMatches,
        whyNotMatches = whyNotMatches,
        analyzedAt = analyzedAt
    )
}
