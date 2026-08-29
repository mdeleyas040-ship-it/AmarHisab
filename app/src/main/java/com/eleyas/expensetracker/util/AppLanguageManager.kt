package com.eleyas.expensetracker.util

import android.content.Context
import java.util.Locale

object AppLanguageManager {

    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "selected_language" // "bn" or "en"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
    }

    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getString(context: Context, bnText: String, enText: String): String {
        return if (getLanguage(context) == "bn") bnText else enText
    }
}
