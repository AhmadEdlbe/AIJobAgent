package com.example.aijobagent.presentation.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.model.JobFilter
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.WorkMode
import com.example.aijobagent.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val filter: JobFilter = JobFilter(),
    val isScanning: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val repo: JobRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(JobFilter())
    private val _isScanning = MutableStateFlow(false)

    val uiState: StateFlow<JobListUiState> = combine(
        repo.getFilteredJobs(_filter.value),
        _filter,
        _isScanning
    ) { jobs, filter, scanning ->
        // This combine is static; need dynamic filter. Workaround: use flatCombine via separate flow
        JobListUiState(jobs.filterBy(filter), filter, scanning, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobListUiState())

    // Better: expose filtered via repo.getFilteredJobs which reacts to filter changes via manual reload
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobsFiltered: StateFlow<List<Job>> = _jobs

    init {
        viewModelScope.launch {
            repo.getAllJobs().collect { all ->
                _jobs.value = applyFilter(all, _filter.value)
            }
        }
        viewModelScope.launch {
            _filter.collect { f ->
                val all = repo.getAllJobs()
                // We'll rely on periodic refresh
            }
        }
    }

    private fun List<Job>.filterBy(filter: JobFilter): List<Job> = applyFilter(this, filter)

    private fun applyFilter(jobs: List<Job>, filter: JobFilter): List<Job> {
        var list = jobs
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.company.lowercase().contains(q) }
        }
        if (filter.techStacks.isNotEmpty()) list = list.filter { it.techStacks.any { t -> t in filter.techStacks } }
        if (filter.workModes.isNotEmpty()) list = list.filter { it.workMode in filter.workModes }
        if (filter.seniorities.isNotEmpty()) list = list.filter { it.seniority in filter.seniorities }
        if (filter.minMatchScore > 0) list = list.filter { it.matchPercentage >= filter.minMatchScore }
        return list
    }

    fun onSearch(query: String) {
        _filter.value = _filter.value.copy(searchQuery = query)
        refreshFiltered()
    }

    fun toggleTech(stack: TechStack) {
        val set = _filter.value.techStacks.toMutableSet()
        if (set.contains(stack)) set.remove(stack) else set.add(stack)
        _filter.value = _filter.value.copy(techStacks = set)
        refreshFiltered()
    }

    fun toggleWorkMode(mode: WorkMode) {
        val set = _filter.value.workModes.toMutableSet()
        if (set.contains(mode)) set.remove(mode) else set.add(mode)
        _filter.value = _filter.value.copy(workModes = set)
        refreshFiltered()
    }

    fun toggleSeniority(s: Seniority) {
        val set = _filter.value.seniorities.toMutableSet()
        if (set.contains(s)) set.remove(s) else set.add(s)
        _filter.value = _filter.value.copy(seniorities = set)
        refreshFiltered()
    }

    fun setMinMatch(score: Int) {
        _filter.value = _filter.value.copy(minMatchScore = score)
        refreshFiltered()
    }

    private fun refreshFiltered() {
        viewModelScope.launch {
            // trigger re-evaluation by collecting latest jobs
            repo.getAllJobs().collect { all ->
                _jobs.value = applyFilter(all, _filter.value)
            }
        }
    }

    fun scan() {
        viewModelScope.launch {
            _isScanning.value = true
            try { repo.scanJobs() } finally { _isScanning.value = false }
        }
    }

    fun toggleFavorite(job: Job) {
        viewModelScope.launch { repo.updateFavorite(job.id, !job.isFavorite) }
    }

    fun getFilter(): StateFlow<JobFilter> = _filter
}
