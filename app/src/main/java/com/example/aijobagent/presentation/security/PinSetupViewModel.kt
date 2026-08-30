package com.example.aijobagent.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijobagent.core.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinSetupState(
    val pin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false
)

sealed class PinSetupEvent {
    data class PinChanged(val pin: String) : PinSetupEvent()
    data class ConfirmChanged(val pin: String) : PinSetupEvent()
    object Save : PinSetupEvent()
    object Skip : PinSetupEvent()
}

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _state = MutableStateFlow(PinSetupState())
    val state: StateFlow<PinSetupState> = _state.asStateFlow()

    fun onEvent(event: PinSetupEvent) {
        when (event) {
            is PinSetupEvent.PinChanged -> _state.value = _state.value.copy(pin = event.pin, error = null)
            is PinSetupEvent.ConfirmChanged -> _state.value = _state.value.copy(confirmPin = event.pin, error = null)
            is PinSetupEvent.Save -> save()
            is PinSetupEvent.Skip -> _state.value = _state.value.copy(isSuccess = true)
        }
    }

    private fun save() {
        val pin = _state.value.pin
        val confirm = _state.value.confirmPin
        if (pin.length < 4) { _state.value = _state.value.copy(error = "PIN must be 4 digits"); return }
        if (pin != confirm) { _state.value = _state.value.copy(error = "PINs do not match"); return }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            pinManager.setPin(pin)
            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
        }
    }
}
