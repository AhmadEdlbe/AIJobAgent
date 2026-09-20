package com.example.aijobagent.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.domain.model.DashboardStats
import com.example.aijobagent.domain.model.Job
import com.example.aijobagent.domain.usecase.GetDashboardStatsUseCase
import com.example.aijobagent.domain.usecase.GetJobsUseCase
import com.example.aijobagent.domain.usecase.ScanJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val highMatchJobs: List<Job> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStats: GetDashboardStatsUseCase,
    private val getJobs: GetJobsUseCase,
    private val scanJobsUseCase: ScanJobsUseCase
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardStats(),
        getJobs.highMatch(),
        _isScanning,
        _error
    ) { stats, jobs, scanning, err ->
        DashboardUiState(stats, jobs, scanning, err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun scanJobs() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _error.value = null
            try {
                scanJobsUseCase()
            } catch (e: Exception) {
                _error.value = e.message ?: "Scan failed"
            } finally {
                _isScanning.value = false
            }
        }
    }
}
