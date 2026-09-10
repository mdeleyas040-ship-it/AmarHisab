package com.eleyas.expensetracker.util

import android.content.Context
import com.eleyas.expensetracker.model.ScratchNote
import org.json.JSONArray
import org.json.JSONObject

object ScratchpadStorage {
    private const val PREF_NAME = "scratchpad_storage"
    private const val KEY_NOTES = "notes"

    fun load(context: Context, userId: String): List<ScratchNote> {
        val raw = context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
            .getString(KEY_NOTES, null) ?: return emptyList()

        return runCatching {
            val notes = JSONArray(raw).let { array ->
                List(array.length()) { index ->
                    val note = array.getJSONObject(index)
                    ScratchNote(
                        id = note.getLong("id"),
                        title = note.optString("title"),
                        content = note.optString("content"),
                        updatedAt = note.optLong("updatedAt")
                    )
                }
            }
            notes.sortedByDescending { it.updatedAt }
        }.getOrElse { emptyList() }
    }

    fun save(context: Context, userId: String, notes: List<ScratchNote>) {
        val json = JSONArray().apply {
            notes.forEach { note ->
                put(
                    JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("updatedAt", note.updatedAt)
                    }
                )
            }
        }

        context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NOTES, json.toString())
            .apply()
    }
}
