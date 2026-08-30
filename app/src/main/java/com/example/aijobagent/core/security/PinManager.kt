package com.example.aijobagent.core.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pinDataStore by preferencesDataStore(name = "pin_store")

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val pinEnabledKey = stringPreferencesKey("pin_enabled")

    val isPinSet: Flow<Boolean> = context.pinDataStore.data.map { it[pinHashKey] != null }

    val isPinEnabled: Flow<Boolean> = context.pinDataStore.data.map { it[pinEnabledKey] == "true" }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.pinDataStore.edit { prefs ->
            prefs[pinHashKey] = hash
            prefs[pinEnabledKey] = "true"
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.pinDataStore.data.first()[pinHashKey] ?: return false
        return stored == hashPin(pin)
    }

    suspend fun clearPin() {
        context.pinDataStore.edit { it.remove(pinHashKey); it.remove(pinEnabledKey) }
    }

    suspend fun disablePin() {
        context.pinDataStore.edit { it[pinEnabledKey] = "false" }
    }

    suspend fun enablePin() {
        if (context.pinDataStore.data.first()[pinHashKey] != null) {
            context.pinDataStore.edit { it[pinEnabledKey] = "true" }
        }
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
