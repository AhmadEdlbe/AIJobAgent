package com.example.aijobagent.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "ai_job_agent_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular prefs if device doesn't support encryption (e.g. emulator without keystore)
            context.getSharedPreferences("ai_job_agent_fallback", Context.MODE_PRIVATE)
        }
    }

    fun saveString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    fun getString(key: String, def: String? = null): String? = prefs.getString(key, def)
    fun saveBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    fun getBoolean(key: String, def: Boolean = false) = prefs.getBoolean(key, def)
    fun clear() { prefs.edit().clear().apply() }
}
