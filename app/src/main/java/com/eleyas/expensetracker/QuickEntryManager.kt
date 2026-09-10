package com.eleyas.expensetracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object QuickEntryManager {
    const val EXTRA_ENTRY_TYPE = "com.eleyas.expensetracker.EXTRA_ENTRY_TYPE"
    const val ENTRY_EXPENSE = "expense"
    const val ENTRY_INCOME = "income"

    private const val CHANNEL_ID = "quick_entry"
    private const val NOTIFICATION_ID = 4101
    private const val EXPENSE_SHORTCUT_ID = "quick_expense"
    private const val INCOME_SHORTCUT_ID = "quick_income"

    fun entryTypeFrom(intent: Intent?): String? =
        intent?.getStringExtra(EXTRA_ENTRY_TYPE)
            ?.takeIf { it == ENTRY_EXPENSE || it == ENTRY_INCOME }

    fun publish(context: Context) {
        publishShortcuts(context)
        publishNotification(context)
    }

    fun entryIntent(context: Context, type: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = "com.eleyas.expensetracker.action.QUICK_ENTRY_$type"
            data = Uri.parse("amarhisab://quick-entry/$type")
            putExtra(EXTRA_ENTRY_TYPE, type)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    private fun publishShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        shortcutManager.dynamicShortcuts = listOf(
            shortcut(context, EXPENSE_SHORTCUT_ID, "দ্রুত খরচ", ENTRY_EXPENSE),
            shortcut(context, INCOME_SHORTCUT_ID, "দ্রুত আয়", ENTRY_INCOME)
        )
    }

    private fun shortcut(
        context: Context,
        id: String,
        label: String,
        type: String
    ): ShortcutInfo =
        ShortcutInfo.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel("$label এন্ট্রি")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_input_add))
            .setIntent(entryIntent(context, type))
            .build()

    private fun publishNotification(context: Context) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "দ্রুত এন্ট্রি",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "নোটিফিকেশন প্যানেল থেকে দ্রুত আয় বা খরচ যোগ করুন"
                }
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_calendar)
            .setContentTitle("দ্রুত এন্ট্রি")
            .setContentText("এক ট্যাপে আয় বা খরচ যোগ করুন")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, "খরচ", pendingIntent(context, ENTRY_EXPENSE, 1))
            .addAction(0, "আয়", pendingIntent(context, ENTRY_INCOME, 2))
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(
        context: Context,
        type: String,
        requestCode: Int
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            entryIntent(context, type),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
