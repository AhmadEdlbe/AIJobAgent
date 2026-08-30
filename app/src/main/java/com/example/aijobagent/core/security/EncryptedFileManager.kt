package com.example.aijobagent.core.security

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    fun saveResumeFile(fileName: String, bytes: ByteArray): String {
        val dir = File(context.filesDir, "resumes").apply { mkdirs() }
        val file = File(dir, fileName)
        try {
            val encryptedFile = EncryptedFile.Builder(
                context, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileOutput().use { it.write(bytes) }
        } catch (e: Exception) {
            // fallback plain
            file.writeBytes(bytes)
        }
        return file.absolutePath
    }

    fun readResumeFile(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val encryptedFile = EncryptedFile.Builder(
                context, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileInput().use { it.readBytes() }
        } catch (e: Exception) {
            try { file.readBytes() } catch (_: Exception) { null }
        }
    }

    fun deleteResumeFile(path: String) { try { File(path).delete() } catch (_: Exception){} }

    fun getResumeDir(): File = File(context.filesDir, "resumes").apply { mkdirs() }
}
