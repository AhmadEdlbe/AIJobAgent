package com.example.aijobagent.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.core.security.BiometricHelper
import com.example.aijobagent.core.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockState(
    val pin: String = "",
    val error: String? = null,
    val isPinSet: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isLoading: Boolean = true
)

sealed class LockEvent {
    data class PinChanged(val pin: String) : LockEvent()
    object SubmitPin : LockEvent()
    object BiometricSuccess : LockEvent()
    object CheckPinState : LockEvent()
}

@HiltViewModel
class LockViewModel @Inject constructor(
    private val pinManager: PinManager,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private val _navigateToSetup = MutableStateFlow(false)
    val navigateToSetup: StateFlow<Boolean> = _navigateToSetup.asStateFlow()

    init {
        checkState()
    }

    fun onEvent(event: LockEvent) {
        when (event) {
            is LockEvent.PinChanged -> _state.value = _state.value.copy(pin = event.pin, error = null)
            is LockEvent.SubmitPin -> verify()
            is LockEvent.BiometricSuccess -> _unlocked.value = true
            is LockEvent.CheckPinState -> checkState()
        }
    }

    private fun checkState() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val isSet = pinManager.isPinSet.first()
            val isEnabled = pinManager.isPinEnabled.first()
            val bioAvail = biometricHelper.isBiometricAvailable()
            if (!isSet || !isEnabled) {
                // No PIN required -> unlock directly
                _unlocked.value = true
            } else {
                _state.value = _state.value.copy(
                    isPinSet = isSet,
                    isBiometricAvailable = bioAvail,
                    isLoading = false
                )
            }
            if (!isSet) {
                _navigateToSetup.value = false
            }
        }
    }

    private fun verify() {
        viewModelScope.launch {
            val pin = _state.value.pin
            if (pin.length < 4) {
                _state.value = _state.value.copy(error = "PIN must be 4 digits")
                return@launch
            }
            val ok = pinManager.verifyPin(pin)
            if (ok) {
                _unlocked.value = true
                _state.value = _state.value.copy(error = null)
            } else {
                _state.value = _state.value.copy(error = "Incorrect PIN")
            }
        }
    }

    fun resetUnlock() { _unlocked.value = false }
}
