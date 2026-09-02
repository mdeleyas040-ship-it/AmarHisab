package com.eleyas.expensetracker

import android.content.Context
import com.eleyas.expensetracker.viewmodel.MainViewModel

/**
 * Clears the persisted notification list used by Amar Hisab.
 * Kept in a small separate file so MainViewModel.kt does not need refactoring.
 */
fun MainViewModel.clearAllNotifications(context: Context) {
    NotificationStorage.save(context, emptyList(), "guest")
}
