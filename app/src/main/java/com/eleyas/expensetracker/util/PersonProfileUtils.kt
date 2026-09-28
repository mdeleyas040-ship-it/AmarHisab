package com.eleyas.expensetracker.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/** Stable local storage for reusable person profile photos. */
fun persistPersonProfilePhoto(context: Context, source: Uri): String? {
    return try {
        val dir = File(context.filesDir, "person_profiles").apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.jpg")
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


/** Generates a permanent human-friendly Amar Hisab person ID. */
fun generatePersonUserId(existing: List<com.eleyas.expensetracker.model.PersonProfile>): String {
    val used = existing.mapNotNull {
        Regex("^AH-(\\d{4})$").matchEntire(it.id)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }.toSet()
    val seed = (System.currentTimeMillis() % 10000L).toInt()
    for (offset in 0..9999) {
        val n = (seed + offset) % 10000
        if (n !in used) return "AH-%04d".format(n)
    }
    return "AH-%04d".format((System.currentTimeMillis() % 10000L).toInt())
}
