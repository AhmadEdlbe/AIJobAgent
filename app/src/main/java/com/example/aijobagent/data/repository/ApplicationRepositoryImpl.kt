package com.example.aijobagent.data.repository

import com.example.aijobagent.data.local.dao.ApplicationDao
import com.example.aijobagent.data.local.dao.JobDao
import com.example.aijobagent.data.local.entity.toDomain
import com.example.aijobagent.data.local.entity.toEntity
import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.model.DashboardStats
import com.example.aijobagent.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ApplicationRepositoryImpl @Inject constructor(
    private val dao: ApplicationDao,
    private val jobDao: JobDao
) : ApplicationRepository {

    override fun getAll(): Flow<List<ApplicationTrack>> =
        combine(dao.getAll(), jobDao.getAllJobs()) { apps, jobs ->
            val jobMap = jobs.associateBy { it.id }
            apps.map { app ->
                val domain = app.toDomain()
                val job = jobMap[domain.jobId]?.toDomain()
                domain.copy(job = job)
            }
        }

    override fun getByStatus(status: ApplicationStatus): Flow<List<ApplicationTrack>> =
        dao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): ApplicationTrack? = dao.getById(id)?.toDomain()

    override suspend fun getByJobId(jobId: String): ApplicationTrack? = dao.getByJobId(jobId)?.toDomain()

    override suspend fun upsert(track: ApplicationTrack) {
        dao.upsert(track.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun updateStatus(id: String, status: ApplicationStatus) {
        val existing = dao.getById(id) ?: return
        val updated = existing.copy(status = status.name, updatedAt = System.currentTimeMillis(), appliedAt = if (status == ApplicationStatus.APPLIED) System.currentTimeMillis() else existing.appliedAt)
        dao.upsert(updated)
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override fun getDashboardStats(): Flow<DashboardStats> {
        // Combine first 5 flows then combine with pending list to avoid 6-arg combine limit
        val firstFive = combine(
            jobDao.getJobCountFlow(),
            jobDao.getHighMatchCountFlow(),
            dao.countFlow(),
            dao.countInterviewsFlow(),
            dao.countOffersFlow()
        ) { total, high, apps, interviews, offers ->
            arrayOf(total, high, apps, interviews, offers)
        }
        return combine(firstFive, dao.getByStatus(ApplicationStatus.PENDING_APPROVAL.name)) { arr, pendingList ->
            val total = arr[0] as Int
            val high = arr[1] as Int
            val apps = arr[2] as Int
            val interviews = arr[3] as Int
            val offers = arr[4] as Int
            DashboardStats(
                totalJobsFound = total,
                highMatchJobs = high,
                applicationsSent = apps,
                interviews = interviews,
                offers = offers,
                pendingApprovals = pendingList.size
            )
        }
    }

    suspend fun createForJob(jobId: String, status: ApplicationStatus = ApplicationStatus.SAVED): ApplicationTrack {
        val track = ApplicationTrack(
            id = UUID.randomUUID().toString(),
            jobId = jobId,
            status = status
        )
        upsert(track)
        return track
    }
}
