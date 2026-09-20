package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.UserProfile
import com.example.aijobagent.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> = repo.getProfile()
    suspend fun once(): UserProfile? = repo.getProfileOnce()
}

class SaveProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile) = repo.saveProfile(profile)
}
