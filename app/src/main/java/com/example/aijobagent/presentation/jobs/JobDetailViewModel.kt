package com.example.aijobagent.presentation.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.model.CoverLetter
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.ApplicationRepository
import com.example.aijobagent.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class JobDetailState(
    val job: Job? = null,
    val application: ApplicationTrack? = null,
    val coverLetter: CoverLetter? = null,
    val isLoading: Boolean = true,
    val isGeneratingLetter: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepo: JobRepository,
    private val appRepo: ApplicationRepository,
    private val aiRepo: AiRepository
) : ViewModel() {

    private var jobId: String = ""

    private val _state = MutableStateFlow(JobDetailState())
    val state: StateFlow<JobDetailState> = _state.asStateFlow()

    fun setJobId(id: String) {
        if (jobId == id) return
        jobId = id
        load()
    }

    fun load() {
        if (jobId.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val job = jobRepo.getJobById(jobId)
                val app = appRepo.getByJobId(jobId)
                _state.value = _state.value.copy(job = job, application = app, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun generateCoverLetter() {
        val job = _state.value.job ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isGeneratingLetter = true)
            try {
                val letter = aiRepo.generateCoverLetter(job)
                _state.value = _state.value.copy(coverLetter = letter, isGeneratingLetter = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isGeneratingLetter = false)
            }
        }
    }

    fun requestApproval() {
        val job = _state.value.job ?: return
        viewModelScope.launch {
            val track = ApplicationTrack(
                id = UUID.randomUUID().toString(),
                jobId = job.id,
                status = ApplicationStatus.PENDING_APPROVAL
            )
            appRepo.upsert(track)
            _state.value = _state.value.copy(application = track)
        }
    }

    fun approveAndApply() {
        val app = _state.value.application ?: return
        viewModelScope.launch {
            appRepo.updateStatus(app.id, ApplicationStatus.APPLIED)
            load()
        }
    }

    fun saveJob() {
        // Just mark as saved in tracker
        val job = _state.value.job ?: return
        viewModelScope.launch {
            val existing = appRepo.getByJobId(job.id)
            if (existing == null) {
                val track = ApplicationTrack(id = UUID.randomUUID().toString(), jobId = job.id, status = ApplicationStatus.SAVED)
                appRepo.upsert(track)
                _state.value = _state.value.copy(application = track)
            }
        }
    }
}
