package com.example.aijobagent.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.domain.model.ApplicationStatus
import com.example.aijobagent.domain.model.ApplicationTrack
import com.example.aijobagent.domain.repository.ApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repo: ApplicationRepository
) : ViewModel() {

    val applications: StateFlow<List<ApplicationTrack>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateStatus(id: String, status: ApplicationStatus) {
        viewModelScope.launch { repo.updateStatus(id, status) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
