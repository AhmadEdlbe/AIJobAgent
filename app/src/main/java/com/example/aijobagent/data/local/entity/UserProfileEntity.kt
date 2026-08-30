package com.example.aijobagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aijobagent.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val linkedInUrl: String,
    val gitHubUrl: String,
    val resumePath: String?,
    val resumeText: String?,
    // Stored as "|"-delimited via Converters
    val preferredCountries: String,
    val preferredJobTitles: String,
    val skills: String,
    val updatedAt: Long
)

fun UserProfileEntity.toDomain(): UserProfile {
    fun split(v: String) = if (v.isEmpty()) emptyList() else v.split("|")
    return UserProfile(
        id = id,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        linkedInUrl = linkedInUrl,
        gitHubUrl = gitHubUrl,
        resumePath = resumePath,
        resumeText = resumeText,
        preferredCountries = split(preferredCountries),
        preferredJobTitles = split(preferredJobTitles),
        skills = split(skills),
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    fun join(list: List<String>) = list.joinToString("|")
    return UserProfileEntity(
        id = id,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        linkedInUrl = linkedInUrl,
        gitHubUrl = gitHubUrl,
        resumePath = resumePath,
        resumeText = resumeText,
        preferredCountries = join(preferredCountries),
        preferredJobTitles = join(preferredJobTitles),
        skills = join(skills),
        updatedAt = updatedAt
    )
}
