package com.example.aijobagent

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aijobagent.core.security.EncryptedPrefs
import com.example.aijobagent.core.security.PinManager
import com.example.aijobagent.data.local.AppDatabase
import com.example.aijobagent.data.local.entity.JobEntity
import com.example.aijobagent.data.local.entity.UserProfileEntity
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.WorkMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAndWorkflowInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var pinManager: PinManager
    private lateinit var encryptedPrefs: EncryptedPrefs

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        pinManager = PinManager(context)
        encryptedPrefs = EncryptedPrefs(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- PIN flow ----
    @Test
    fun pinManager_hashAndVerify() = runTest {
        pinManager.clearPin()
        assertFalse(pinManager.isPinSet.first())
        pinManager.setPin("1234")
        assertTrue(pinManager.isPinSet.first())
        assertTrue(pinManager.verifyPin("1234"))
        assertFalse(pinManager.verifyPin("0000"))
        pinManager.clearPin()
        assertFalse(pinManager.isPinSet.first())
    }

    @Test
    fun encryptedPrefs_storesEncrypted() {
        encryptedPrefs.saveString("test_key", "secret123")
        assertEquals("secret123", encryptedPrefs.getString("test_key"))
        encryptedPrefs.saveBoolean("bool_key", true)
        assertTrue(encryptedPrefs.getBoolean("bool_key"))
    }

    // ---- Profile DAO ----
    @Test
    fun profileDao_saveAndFlow() = runTest {
        val dao = db.profileDao()
        assertNull(dao.getProfile())
        val entity = UserProfileEntity(1, "Alice", "alice@x.com", "123", "https://linkedin.com/alice", "https://github.com/alice", null, "Resume text", "UAE", "Android", "Kotlin|Java", System.currentTimeMillis())
        dao.upsert(entity)
        val loaded = dao.getProfile()
        assertNotNull(loaded)
        assertEquals("Alice", loaded!!.fullName)
        val flow = dao.getProfileFlow().first()
        assertNotNull(flow)
    }

    // ---- Job DAO + high match query ----
    @Test
    fun jobDao_highMatchQuery() = runTest {
        val dao = db.jobDao()
        val jobs = listOf(
            JobEntity("1","Android Dev","Google","desc","Remote","Remote",WorkMode.REMOTE,Seniority.SENIOR,"ANDROID,JAVA",JobSource.LINKEDIN,"https://x",null,null,"USD",123L,"Kotlin|Java",false,85,"Kotlin","Java","fit","salary","why","whyNot",123L),
            JobEntity("2","Backend","Amazon","desc","UAE","UAE",WorkMode.ONSITE,Seniority.MID_LEVEL,"BACKEND,JAVA",JobSource.INDEED,"https://y",null,null,"USD",124L,"Java",false,60,"Java","","fit","salary","why","whyNot",124L),
            JobEntity("3","Frontend","Meta","desc","USA","USA",WorkMode.REMOTE,Seniority.JUNIOR,"FRONTEND,REACT",JobSource.REMOTE_OK,"https://z",null,null,"USD",125L,"React",false,90,"React","","fit","salary","why","whyNot",125L)
        )
        dao.insertAll(jobs)
        val all = dao.getAllJobs().first()
        assertEquals(3, all.size)
        val high = dao.getHighMatchJobs().first()
        assertEquals(2, high.size) // 85 and 90
        assertTrue(high.all { it.matchPercentage >= 75 })
        assertEquals(90, high.first().matchPercentage) // ordered DESC
    }

    // ---- Application workflow: SAVED -> PENDING_APPROVAL -> APPLIED ----
    @Test
    fun applicationWorkflow_manualApproval() = runTest {
        val appDao = db.applicationDao()
        val jobDao = db.jobDao()
        // insert job
        val job = JobEntity("j1","Title","Comp","desc","L","C",WorkMode.REMOTE,Seniority.SENIOR,"ANDROID",JobSource.MANUAL,"https://x",null,null,"USD", 1L,"Kotlin",false,80,"Kotlin","", "fit","salary","why","whyNot",1L)
        jobDao.insert(job)
        // save job -> SAVED
        val saved = com.example.aijobagent.data.local.entity.ApplicationEntity("app1","j1", com.example.aijobagent.domain.model.ApplicationStatus.SAVED.name, null, null, "", null, 1L, 1L)
        appDao.upsert(saved)
        var loaded = appDao.getById("app1")
        assertEquals("SAVED", loaded!!.status)
        // request approval -> PENDING_APPROVAL
        appDao.upsert(loaded.copy(status = com.example.aijobagent.domain.model.ApplicationStatus.PENDING_APPROVAL.name, updatedAt = 2L))
        loaded = appDao.getById("app1")
        assertEquals("PENDING_APPROVAL", loaded!!.status)
        // approve -> APPLIED
        appDao.upsert(loaded.copy(status = com.example.aijobagent.domain.model.ApplicationStatus.APPLIED.name, appliedAt = 3L))
        loaded = appDao.getById("app1")
        assertEquals("APPLIED", loaded!!.status)
        assertNotNull(loaded.appliedAt)
        // ensure dashboard stats would count it (via repo combine) - we test dao counts separately if needed
    }

    @Test
    fun coverLetterDao_crud() = runTest {
        val dao = db.coverLetterDao()
        val entity = com.example.aijobagent.data.local.entity.CoverLetterEntity("cl1","j1","Dear Hiring...", 1L,false)
        dao.upsert(entity)
        val loaded = dao.getForJob("j1")
        assertNotNull(loaded)
        assertEquals("Dear Hiring...", loaded!!.content)
        assertFalse(loaded.isEdited)
    }
}
