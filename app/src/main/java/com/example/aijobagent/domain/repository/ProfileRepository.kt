package com.example.aijobagent.domain.repository

import com.example.aijobagent.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<UserProfile?>
    suspend fun getProfileOnce(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateResume(path: String, text: String)
}
