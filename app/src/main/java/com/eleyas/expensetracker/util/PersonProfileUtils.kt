package com.eleyas.expensetracker.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/** Stable local storage for reusable person profile photos. */
fun persistPersonProfilePhoto(context: Context, source: Uri): String? {
    return try {
        val dir = File(context.filesDir, "person_profiles").apply { mkdirs() }
        val target = File(dir, "\${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    } catch (_: Exception) {
        null
    }
}

fun deletePersonProfilePhoto(context: Context, photoPath: String?) {
    if (photoPath.isNullOrBlank()) return
    runCatching {
        val root = File(context.filesDir, "person_profiles").canonicalPath
        val file = File(photoPath).canonicalFile
        if (file.parentFile?.canonicalPath == root) file.delete()
    }
}
