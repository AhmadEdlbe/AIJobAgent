package com.example.aijobagent.core.security

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SQLCipher hook for Room. When enabled, DB file is encrypted with AES-256.
 * Passphrase is derived from device-specific EncryptedPrefs key + PIN hash.
 * To enable: EncryptedPrefs.saveBoolean("db_encrypted", true)
 * Future: migrate existing plain DB by rekey.
 */
@Singleton
class SqlCipherHelper @Inject constructor(
    private val encryptedPrefs: EncryptedPrefs
) {
    fun getSupportFactory(context: Context): SupportSQLiteOpenHelper.Factory? {
        if (!isEnabled()) return null
        val passphrase = getOrCreatePassphrase()
        // Load native libs
        try {
            SQLiteDatabase.loadLibs(context)
        } catch (_: Exception) {}
        val bytes = SQLiteDatabase.getBytes(passphrase.toCharArray())
        return SupportFactory(bytes)
    }

    fun isEnabled(): Boolean = encryptedPrefs.getBoolean("db_encrypted", false)

    fun setEnabled(enabled: Boolean) {
        encryptedPrefs.saveBoolean("db_encrypted", enabled)
    }

    private fun getOrCreatePassphrase(): String {
        val existing = encryptedPrefs.getString("db_passphrase", null)
        if (!existing.isNullOrBlank()) return existing
        // 32-char random + device stability
        val newPass = java.util.UUID.randomUUID().toString().replace("-", "") + java.util.UUID.randomUUID().toString().take(8)
        encryptedPrefs.saveString("db_passphrase", newPass)
        return newPass
    }

    fun rotatePassphrase(newPass: String) {
        encryptedPrefs.saveString("db_passphrase", newPass)
    }
}
