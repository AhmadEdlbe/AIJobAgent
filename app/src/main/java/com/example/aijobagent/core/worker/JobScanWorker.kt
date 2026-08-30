package com.example.aijobagent.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aijobagent.domain.repository.ApplicationRepository
import com.example.aijobagent.domain.repository.JobRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class JobScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val jobRepo: JobRepository,
    private val applicationRepo: ApplicationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val jobs = jobRepo.scanJobs()
            if (jobs.any { it.matchPercentage >= 75 }) {
                NotificationHelper.showHighMatchNotification(applicationContext, jobs.count { it.matchPercentage >= 75 })
            }
            // Check pending approvals
            val stats = applicationRepo.getDashboardStats().first()
            if (stats.pendingApprovals > 0) {
                NotificationHelper.showPendingApplicationsReminder(applicationContext, stats.pendingApprovals)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "daily_job_scan"
    }
}
