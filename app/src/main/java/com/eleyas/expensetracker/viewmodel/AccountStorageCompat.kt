package com.eleyas.expensetracker.viewmodel

import android.content.Context
import android.content.SharedPreferences

/**
 * ViewModel-package compatibility bridge.
 * Keeps MainViewModel's legacy AccountStorage reference unambiguous while
 * delegating to the canonical util AccountStorage implementation.
 */
object AccountStorage {
    fun getPrefs(context: Context, userId: String): SharedPreferences =
        com.eleyas.expensetracker.util.AccountStorage.getPrefs(context, userId)
}
