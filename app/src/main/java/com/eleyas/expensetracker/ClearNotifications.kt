package com.eleyas.expensetracker

import android.content.Context
import com.eleyas.expensetracker.viewmodel.MainViewModel

/** Clears the persisted notification list for the currently active account. */
fun MainViewModel.clearAllNotifications(context: Context) {
    val userId = try {
        val field = MainViewModel::class.java.getDeclaredField("currentUserId")
        field.isAccessible = true
        field.get(this) as? String ?: "guest"
    } catch (_: Exception) {
        "guest"
    }
    NotificationStorage.save(context, emptyList(), userId)
}
