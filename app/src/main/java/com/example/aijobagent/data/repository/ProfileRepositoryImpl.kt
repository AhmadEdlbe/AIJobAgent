package com.example.aijobagent.data.repository

import com.example.aijobagent.data.local.dao.ProfileDao
import com.example.aijobagent.data.local.entity.toDomain
import com.example.aijobagent.data.local.entity.toEntity
import com.example.aijobagent.domain.model.UserProfile
import com.example.aijobagent.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val dao: ProfileDao
) : ProfileRepository {
    override fun getProfile(): Flow<UserProfile?> = dao.getProfileFlow().map { it?.toDomain() }

    override suspend fun getProfileOnce(): UserProfile? = dao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsert(profile.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun updateResume(path: String, text: String) {
        val current = dao.getProfile()?.toDomain() ?: UserProfile()
        dao.upsert(current.copy(resumePath = path, resumeText = text, updatedAt = System.currentTimeMillis()).toEntity())
    }
}
