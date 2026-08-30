package com.example.aijobagent.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.aijobagent.core.config.BackendConfig
import com.example.aijobagent.core.security.EncryptedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backendConfig: BackendConfig,
    private val prefs: EncryptedPrefs
): ViewModel() {
    private val _backendEnabled = MutableStateFlow(backendConfig.backendEnabled)
    val backendEnabled: StateFlow<Boolean> = _backendEnabled

    private val _backendUrl = MutableStateFlow(backendConfig.backendUrl)
    val backendUrl: StateFlow<String> = _backendUrl

    private val _openAiKey = MutableStateFlow(backendConfig.openAiApiKey ?: "")
    val openAiKey: StateFlow<String> = _openAiKey

    fun setBackendEnabled(enabled: Boolean){ backendConfig.backendEnabled = enabled; _backendEnabled.value = enabled }
    fun setBackendUrl(url: String){ backendConfig.backendUrl = url; _backendUrl.value = url }
    fun setOpenAiKey(key: String){ backendConfig.openAiApiKey = key; _openAiKey.value = key; prefs.saveString("openai_api_key", key) }
}
