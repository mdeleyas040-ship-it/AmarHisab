package com.eleyas.expensetracker

import android.content.Context
import android.content.SharedPreferences

/**
 * Root-package compatibility facade for callers that resolve AccountStorage
 * from the app package. Delegates to the canonical util implementation.
 */
object AccountStorage {
    fun getPrefs(context: Context, userId: String): SharedPreferences =
        com.eleyas.expensetracker.util.AccountStorage.getPrefs(context, userId)
}
