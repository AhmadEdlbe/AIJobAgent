package com.example.aijobagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val status: String, // ApplicationStatus.name
    val appliedAt: Long?,
    val interviewDate: Long?,
    val notes: String,
    val coverLetterId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun ApplicationEntity.toDomain(): ApplicationTrack {
    return ApplicationTrack(
        id = id,
        jobId = jobId,
        status = try { ApplicationStatus.valueOf(status) } catch (_: Exception) { ApplicationStatus.SAVED },
        appliedAt = appliedAt,
        interviewDate = interviewDate,
        notes = notes,
        coverLetterId = coverLetterId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ApplicationTrack.toEntity(): ApplicationEntity {
    return ApplicationEntity(
        id = id,
        jobId = jobId,
        status = status.name,
        appliedAt = appliedAt,
        interviewDate = interviewDate,
        notes = notes,
        coverLetterId = coverLetterId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
