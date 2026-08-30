package com.example.aijobagent.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.domain.model.UserProfile
import com.example.aijobagent.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _saving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _saved = MutableStateFlow(false)
    private val _editProfile = MutableStateFlow(UserProfile())

    val uiState: StateFlow<ProfileUiState> = kotlinx.coroutines.flow.combine(
        repo.getProfile(), _saving, _error, _saved, _editProfile
    ) { flowProfile, saving, err, saved, edit ->
        val profile = flowProfile ?: UserProfile()
        // If edit is still default and profile loaded, sync
        ProfileUiState(
            profile = if (_editProfile.value.fullName.isEmpty() && profile.fullName.isNotEmpty() && !_saved.value) profile else edit.takeIf { it.fullName.isNotEmpty() || profile.fullName.isEmpty() } ?: profile,
            isLoading = false,
            isSaving = saving,
            error = err,
            saved = saved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    // Simpler: direct mutable
    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = repo.getProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Editable state holder
    private val _editable = MutableStateFlow<UserProfile>(UserProfile())
    val editable: StateFlow<UserProfile> = _editable

    init {
        viewModelScope.launch {
            repo.getProfile().collect { p ->
                if (p != null && _editable.value.fullName.isEmpty() && p.fullName.isNotEmpty()) {
                    _editable.value = p
                } else if (p == null) {
                    _editable.value = UserProfile()
                }
            }
        }
    }

    fun updateField(transform: (UserProfile) -> UserProfile) {
        _editable.value = transform(_editable.value)
        _error.value = null
        _saved.value = false
    }

    fun save() {
        viewModelScope.launch {
            val p = _editable.value
            if (p.fullName.isBlank()) { _error.value = "Full name required"; return@launch }
            if (p.email.isBlank() || !p.email.contains("@")) { _error.value = "Valid email required"; return@launch }
            _saving.value = true
            try {
                repo.saveProfile(p)
                _saved.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _saving.value = false
            }
        }
    }

    fun addSkill(skill: String) {
        if (skill.isBlank()) return
        val current = _editable.value.skills.toMutableList()
        if (!current.contains(skill.trim())) {
            current.add(skill.trim())
            _editable.value = _editable.value.copy(skills = current)
        }
    }

    fun removeSkill(skill: String) {
        _editable.value = _editable.value.copy(skills = _editable.value.skills.filter { it != skill })
    }
}
