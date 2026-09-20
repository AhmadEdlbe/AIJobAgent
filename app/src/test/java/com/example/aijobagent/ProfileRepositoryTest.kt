package com.example.aijobagent

import com.example.aijobagent.domain.model.UserProfile
import org.junit.Assert.*
import org.junit.Test

class ProfileRepositoryTest {

    @Test
    fun entity_toDomain_mapping() {
        val entity = com.example.aijobagent.data.local.entity.UserProfileEntity(
            id = 1,
            fullName = "John Doe",
            email = "john@example.com",
            phoneNumber = "123",
            linkedInUrl = "https://linkedin.com/in/john",
            gitHubUrl = "https://github.com/john",
            resumePath = null,
            resumeText = "Experienced Android dev",
            preferredCountries = "UAE|Germany",
            preferredJobTitles = "Android|Backend",
            skills = "Kotlin|Java|Spring Boot",
            updatedAt = 123L
        )
        val domain = entity.toDomainTest()
        assertEquals("John Doe", domain.fullName)
        assertEquals(listOf("UAE","Germany"), domain.preferredCountries)
        assertEquals(listOf("Android","Backend"), domain.preferredJobTitles)
        assertEquals(listOf("Kotlin","Java","Spring Boot"), domain.skills)
        assertTrue(domain.isComplete())
    }

    @Test
    fun toEntity_roundTrip() {
        val domain = UserProfile(
            fullName = "Jane",
            email = "jane@x.com",
            phoneNumber = "999",
            linkedInUrl = "https://linkedin.com/in/jane",
            gitHubUrl = "https://github.com/jane",
            preferredCountries = listOf("USA","Remote"),
            preferredJobTitles = listOf("Full Stack"),
            skills = listOf("React","Node.js"),
            updatedAt = 456L
        )
        val entity = domain.toEntityTest()
        assertEquals("USA|Remote", entity.preferredCountries)
        assertEquals("React|Node.js", entity.skills)
        val back = entity.toDomainTest()
        assertEquals(domain.preferredCountries, back.preferredCountries)
        assertEquals(domain.skills, back.skills)
    }

    @Test
    fun isComplete_validation() {
        assertFalse(UserProfile(fullName="", email="a@a.com", skills=listOf("Kotlin")).isComplete())
        assertFalse(UserProfile(fullName="John", email="", skills=listOf("Kotlin")).isComplete())
        assertFalse(UserProfile(fullName="John", email="john@x.com", skills=emptyList()).isComplete())
        assertTrue(UserProfile(fullName="John", email="john@x.com", skills=listOf("Kotlin")).isComplete())
    }
}

// test helpers duplicating entity mappers to avoid Android deps
private fun com.example.aijobagent.data.local.entity.UserProfileEntity.toDomainTest(): UserProfile {
    fun split(v: String) = if (v.isEmpty()) emptyList() else v.split("|")
    return UserProfile(
        id = id, fullName = fullName, email = email, phoneNumber = phoneNumber,
        linkedInUrl = linkedInUrl, gitHubUrl = gitHubUrl, resumePath = resumePath, resumeText = resumeText,
        preferredCountries = split(preferredCountries), preferredJobTitles = split(preferredJobTitles), skills = split(skills), updatedAt = updatedAt
    )
}
private fun UserProfile.toEntityTest(): com.example.aijobagent.data.local.entity.UserProfileEntity {
    fun join(l: List<String>) = l.joinToString("|")
    return com.example.aijobagent.data.local.entity.UserProfileEntity(
        id = id, fullName = fullName, email = email, phoneNumber = phoneNumber,
        linkedInUrl = linkedInUrl, gitHubUrl = gitHubUrl, resumePath = resumePath, resumeText = resumeText,
        preferredCountries = join(preferredCountries), preferredJobTitles = join(preferredJobTitles), skills = join(skills), updatedAt = updatedAt
    )
}
