package com.example.aijobagent.core.config

import com.example.aijobagent.core.security.EncryptedPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendConfig @Inject constructor(private val prefs: EncryptedPrefs) {
    var backendUrl: String
        get() = prefs.getString("backend_url", "http://10.0.2.2:8080/api/v1/") ?: "http://10.0.2.2:8080/api/v1/"
        set(v) = prefs.saveString("backend_url", v)

    var openAiApiKey: String?
        get() = prefs.getString("openai_api_key", null)
        set(v) { if (v != null) prefs.saveString("openai_api_key", v) else prefs.prefs.edit().remove("openai_api_key").apply() }

    var backendEnabled: Boolean
        get() = prefs.getBoolean("backend_enabled", false)
        set(v) = prefs.saveBoolean("backend_enabled", v)
}
