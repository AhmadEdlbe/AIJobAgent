package com.example.aijobagent.domain.model

data class UserProfile(
    val id: Int = 1, // single user
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val linkedInUrl: String = "",
    val gitHubUrl: String = "",
    val resumePath: String? = null, // encrypted local file path
    val resumeText: String? = null,
    val preferredCountries: List<String> = emptyList(),
    val preferredJobTitles: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isComplete(): Boolean = fullName.isNotBlank() && email.isNotBlank() && skills.isNotEmpty()
}
