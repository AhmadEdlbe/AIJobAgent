package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.util.UUID

class GetApplicationsUseCase @Inject constructor(private val repo: ApplicationRepository) {
    operator fun invoke(): Flow<List<ApplicationTrack>> = repo.getAll()
    fun byStatus(status: ApplicationStatus): Flow<List<ApplicationTrack>> = repo.getByStatus(status)
}

class SaveApplicationUseCase @Inject constructor(private val repo: ApplicationRepository) {
    suspend operator fun invoke(jobId: String, status: ApplicationStatus = ApplicationStatus.SAVED): ApplicationTrack {
        val track = ApplicationTrack(id = UUID.randomUUID().toString(), jobId = jobId, status = status)
        repo.upsert(track)
        return track
    }
}

class UpdateApplicationStatusUseCase @Inject constructor(private val repo: ApplicationRepository) {
    suspend operator fun invoke(id: String, status: ApplicationStatus) = repo.updateStatus(id, status)
}
