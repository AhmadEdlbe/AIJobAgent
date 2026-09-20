package com.example.aijobagent

import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.model.DashboardStats
import com.example.aijobagent.domain.model.Job
import org.junit.Assert.*
import org.junit.Test

class DashboardStatsTest {

    private fun computeStats(jobs: List<Job>, apps: List<ApplicationTrack>): DashboardStats {
        val total = jobs.size
        val high = jobs.count { it.matchPercentage >= 75 }
        val applied = apps.count { it.status == ApplicationStatus.APPLIED }
        val interviews = apps.count { it.status == ApplicationStatus.INTERVIEW_SCHEDULED || it.status == ApplicationStatus.INTERVIEW_COMPLETED }
        val offers = apps.count { it.status == ApplicationStatus.OFFER_RECEIVED }
        val pending = apps.count { it.status == ApplicationStatus.PENDING_APPROVAL }
        return DashboardStats(total, high, applied, interviews, offers, pending)
    }

    @Test
    fun empty_givesZeros() {
        val stats = computeStats(emptyList(), emptyList())
        assertEquals(0, stats.totalJobsFound)
        assertEquals(0, stats.highMatchJobs)
        assertEquals(0, stats.applicationsSent)
    }

    @Test
    fun highMatchCount_correct() {
        val jobs = listOf(
            Job(id="1", title="A", company="C", description="d", location="L", matchPercentage=80),
            Job(id="2", title="B", company="C", description="d", location="L", matchPercentage=60),
            Job(id="3", title="C", company="C", description="d", location="L", matchPercentage=90)
        )
        val stats = computeStats(jobs, emptyList())
        assertEquals(3, stats.totalJobsFound)
        assertEquals(2, stats.highMatchJobs)
    }

    @Test
    fun applicationStats_counts() {
        val apps = listOf(
            ApplicationTrack(id="1", jobId="j1", status=ApplicationStatus.APPLIED),
            ApplicationTrack(id="2", jobId="j2", status=ApplicationStatus.PENDING_APPROVAL),
            ApplicationTrack(id="3", jobId="j3", status=ApplicationStatus.PENDING_APPROVAL),
            ApplicationTrack(id="4", jobId="j4", status=ApplicationStatus.INTERVIEW_SCHEDULED),
            ApplicationTrack(id="5", jobId="j5", status=ApplicationStatus.OFFER_RECEIVED),
            ApplicationTrack(id="6", jobId="j6", status=ApplicationStatus.REJECTED)
        )
        val stats = computeStats(emptyList(), apps)
        assertEquals(1, stats.applicationsSent)
        assertEquals(1, stats.interviews)
        assertEquals(1, stats.offers)
        assertEquals(2, stats.pendingApprovals)
    }

    @Test
    fun interviewCompleted_countsAsInterview() {
        val apps = listOf(ApplicationTrack(id="1", jobId="j1", status=ApplicationStatus.INTERVIEW_COMPLETED))
        val stats = computeStats(emptyList(), apps)
        assertEquals(1, stats.interviews)
    }
}
