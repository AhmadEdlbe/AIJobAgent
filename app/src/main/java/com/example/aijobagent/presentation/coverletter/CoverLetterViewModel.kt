package com.example.aijobagent.presentation.coverletter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.data.local.dao.CoverLetterDao
import com.example.aijobagent.data.local.entity.toDomain
import com.example.aijobagent.data.local.entity.toEntity
import com.example.aijobagent.domain.model.CoverLetter
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.repository.AiRepository
import com.example.aijobagent.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoverLetterState(
    val job: Job? = null,
    val letter: CoverLetter? = null,
    val editedContent: String = "",
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val jobRepo: JobRepository,
    private val aiRepo: AiRepository,
    private val dao: CoverLetterDao
) : ViewModel() {
    private var jobId: String = ""

    private val _state = MutableStateFlow(CoverLetterState())
    val state: StateFlow<CoverLetterState> = _state.asStateFlow()

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
            val letter = dao.getForJob(jobId)?.toDomain()
            _state.value = _state.value.copy(job = job, letter = letter, editedContent = letter?.content ?: "", isLoading = false)
        }
    }

    fun generate() {
        val job = _state.value.job ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            try {
                val l = aiRepo.generateCoverLetter(job)
                _state.value = _state.value.copy(letter = l, editedContent = l.content, isGenerating = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isGenerating = false)
            }
        }
    }

    fun onEdit(newContent: String) { _state.value = _state.value.copy(editedContent = newContent) }

    fun saveEdited() {
        val curr = _state.value.letter ?: return
        viewModelScope.launch {
            val updated = curr.copy(content = _state.value.editedContent, isEdited = true)
            dao.upsert(updated.toEntity())
            _state.value = _state.value.copy(letter = updated)
        }
    }
}
