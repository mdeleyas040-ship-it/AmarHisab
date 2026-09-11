package com.eleyas.expensetracker

import android.content.Context
import com.eleyas.expensetracker.NotificationStorage
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Compatibility helper for the existing MainActivity notification-clear action.
 * The stable MainViewModel does not expose a clearAllNotifications member,
 * so keep the action available without changing the ViewModel's public state API.
 */
fun MainViewModel.clearAllNotifications(context: Context) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    NotificationStorage.save(context, emptyList(), userId)
}
