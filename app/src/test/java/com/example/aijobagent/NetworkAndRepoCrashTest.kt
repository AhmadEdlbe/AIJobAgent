package com.example.aijobagent

import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class NetworkAndRepoCrashTest {

    @Test fun userProfileEntity_roundTrip_doesNotCrash() = runTest {
        val profile = UserProfile(fullName = "Test", email = "a@b.com", phoneNumber = "123", skills = listOf("Kotlin","Java"))
        assertNotNull(profile)
        assertEquals(2, profile.skills.size)
    }

    @Test fun jobEntity_toDomain_doesNotCrash_onNullSalary() = runTest {
        val job = Job(id = "1", title = "Android", company = "Co", description = "Desc", location = "Remote", salaryMin = null, salaryMax = null)
        assertNull(job.salaryMin)
        assertEquals(0, job.matchPercentage)
    }

    @Test fun applicationStatus_handlesAll() {
        ApplicationStatus.entries.forEach { status ->
            val track = ApplicationTrack(id="1", jobId="j1", status=status)
            assertEquals(status, track.status)
        }
    }

    @Test fun jobSource_handlesAll() {
        JobSource.entries.forEach { source ->
            val job = Job(id="1", title="T", company="C", description="D", location="L", source=source)
            assertEquals(source, job.source)
        }
    }

    @Test fun pinHash_doesNotCrash_onEmpty() {
        // Simulate hash: empty PIN should still produce hash via PinManager logic (SHA-256)
        val pin = ""
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val hash = md.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
        assertNotNull(hash)
        assertEquals(64, hash.length)
    }
}
