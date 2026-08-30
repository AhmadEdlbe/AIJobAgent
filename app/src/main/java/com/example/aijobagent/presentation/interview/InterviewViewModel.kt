package com.example.aijobagent.presentation.interview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.data.local.dao.InterviewPrepDao
import com.example.aijobagent.data.local.entity.toDomain
import com.example.aijobagent.domain.model.InterviewPrep
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewState(
    val job: Job? = null,
    val prep: InterviewPrep? = null,
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val jobRepo: JobRepository,
    private val aiRepo: AiRepository,
    private val dao: InterviewPrepDao
) : ViewModel() {
    private var jobId: String = ""
    private val _state = MutableStateFlow(InterviewState())
    val state: StateFlow<InterviewState> = _state.asStateFlow()

    fun setJobId(id: String) {
        if (jobId == id) return
        jobId = id
        load()
    }

    private fun load() {
        if (jobId.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val job = jobRepo.getJobById(jobId)
            val prep = dao.getForJob(jobId)?.toDomain()
            _state.value = _state.value.copy(job = job, prep = prep, isLoading = false)
        }
    }

    fun generate() {
        val job = _state.value.job ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            try {
                val p = aiRepo.generateInterviewPrep(job)
                _state.value = _state.value.copy(prep = p, isGenerating = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isGenerating = false)
            }
        }
    }
}
