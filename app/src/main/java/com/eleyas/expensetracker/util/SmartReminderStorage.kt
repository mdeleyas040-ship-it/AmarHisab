package com.eleyas.expensetracker.util

import android.content.Context

object SmartReminderStorage {

    private const val KEY_SENT_IDS = "sent_transaction_reminders"

    private fun getPrefs(
        context: Context,
        userId: String
    ) = context.getSharedPreferences(
        "smart_reminder_storage_$userId",
        Context.MODE_PRIVATE
    )

    fun hasBeenSent(
        context: Context,
        userId: String,
        transactionId: Long
    ): Boolean {
        return getPrefs(context, userId)
            .getStringSet(KEY_SENT_IDS, emptySet())
            ?.contains(transactionId.toString()) == true
    }

    fun markAsSent(
        context: Context,
        userId: String,
        transactionId: Long
    ) {
        val prefs = getPrefs(context, userId)

        val current = prefs
            .getStringSet(KEY_SENT_IDS, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()

        current.add(transactionId.toString())

        prefs.edit()
            .putStringSet(KEY_SENT_IDS, current)
            .apply()
    }
}