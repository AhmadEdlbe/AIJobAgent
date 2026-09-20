package com.example.aijobagent.core.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object PdfTextExtractor {
    @Volatile private var initialized = false

    private fun ensureInit(context: Context) {
        if (!initialized) {
            try {
                PDFBoxResourceLoader.init(context)
                initialized = true
            } catch (_: Exception) {}
        }
    }

    suspend fun extractText(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            ensureInit(context)
            context.contentResolver.openInputStream(uri)?.use { input ->
                extractFromInputStream(input)
            } ?: Result.failure(Exception("Cannot open PDF"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractFromBytes(context: Context, bytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            ensureInit(context)
            bytes.inputStream().use { input ->
                extractFromInputStream(input)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractFromInputStream(input: InputStream): Result<String> {
        return try {
            PDDocument.load(input).use { doc ->
                if (doc.isEncrypted) {
                    return Result.failure(Exception("Encrypted PDF not supported"))
                }
                val stripper = PDFTextStripper()
                stripper.startPage = 1
                stripper.endPage = minOf(5, doc.numberOfPages) // first 5 pages
                val text = stripper.getText(doc).trim()
                if (text.isBlank()) Result.failure(Exception("No text found")) else Result.success(text.take(12000))
            }
        } catch (e: Exception) {
            // Fallback to raw string reading (may contain text for non-scanned PDFs)
            try {
                input.reset()
                val raw = input.readBytes().let { String(it) }
                if (raw.contains("Java") || raw.contains("Kotlin") || raw.length > 500) {
                    Result.success(raw.take(8000))
                } else Result.failure(e)
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }
}
