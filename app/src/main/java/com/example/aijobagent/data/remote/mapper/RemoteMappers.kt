package com.example.aijobagent.data.remote.mapper

import com.example.aijobagent.data.remote.dto.JobRemoteDto
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.WorkMode
import java.time.Instant

fun JobRemoteDto.toDomain(): Job = toDomainModel()
fun JobRemoteDto.toDomainModel(): Job {
    fun parseInstant(s: String?): Long = try { Instant.parse(s).toEpochMilli() } catch (_: Exception){ System.currentTimeMillis() }
    return Job(
        id = id,
        title = title,
        company = company,
        description = description ?: "",
        location = location ?: "",
        country = country ?: location ?: "",
        workMode = try { WorkMode.valueOf(workMode ?: "REMOTE") } catch (_: Exception){ WorkMode.REMOTE },
        seniority = try { Seniority.valueOf(seniority ?: "MID_LEVEL") } catch (_: Exception){ Seniority.MID_LEVEL },
        techStacks = techStacks?.mapNotNull { try{ TechStack.valueOf(it)}catch(_:Exception){null} } ?: emptyList(),
        source = try { JobSource.valueOf(source ?: "MANUAL") } catch (_: Exception){ JobSource.MANUAL },
        url = url ?: "",
        salaryMin = salaryMin,
        salaryMax = salaryMax,
        currency = currency ?: "USD",
        postedAt = parseInstant(postedAt),
        requirements = requirements ?: emptyList(),
        isFavorite = isFavorite ?: false,
        matchPercentage = matchPercentage ?: 0,
        matchingSkills = matchingSkills ?: emptyList(),
        missingSkills = missingSkills ?: emptyList(),
        experienceFit = experienceFit ?: "",
        salaryFit = salaryFit ?: "",
        whyMatches = whyMatches ?: "",
        whyNotMatches = whyNotMatches ?: "",
        analyzedAt = analyzedAt?.let { parseInstant(it) }
    )
}

fun Job.toRemoteDto(): JobRemoteDto {
    fun instantStr(m: Long): String = Instant.ofEpochMilli(m).toString()
    return JobRemoteDto(
        id = id, title = title, company = company, description = description, location = location, country = country,
        workMode = workMode.name, seniority = seniority.name, techStacks = techStacks.map { it.name }, source = source.name, url = url,
        salaryMin = salaryMin, salaryMax = salaryMax, currency = currency, postedAt = instantStr(postedAt), requirements = requirements, isFavorite = isFavorite,
        matchPercentage = matchPercentage, matchingSkills = matchingSkills, missingSkills = missingSkills, experienceFit = experienceFit, salaryFit = salaryFit, whyMatches = whyMatches, whyNotMatches = whyNotMatches, analyzedAt = analyzedAt?.let { instantStr(it) }
    )
}
