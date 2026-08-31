package com.eleyas.expensetracker.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.model.NotificationItem
import com.eleyas.expensetracker.NotificationStorage
import com.google.firebase.auth.FirebaseAuth

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == DailyReminderManager.ACTION_DAILY_REMINDER) {
            if (!DailyReminderManager.isReminderEnabled(context)) return

            // Show Notification
            showNotification(context)

            // Save in-app notification
            saveInAppNotification(context)

            // Reschedule for next day
            DailyReminderManager.scheduleDailyReminder(context)
        } else if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            if (DailyReminderManager.isReminderEnabled(context)) {
                DailyReminderManager.scheduleDailyReminder(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        DailyReminderManager.createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags)

        val notification = NotificationCompat.Builder(context, DailyReminderManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("আমার হিসাব 📝")
            .setContentText("আজকের খরচগুলো কি এন্ট্রি করা হয়েছে?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun saveInAppNotification(context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val existing = NotificationStorage.load(context, currentUserId).toMutableList()
        val newItem = NotificationItem(
            title = "আমার হিসাব 📝",
            message = "আজকের খরচগুলো কি এন্ট্রি করা হয়েছে?",
            type = "reminder",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        existing.add(newItem)
        NotificationStorage.save(context, existing, currentUserId)
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
    }
}
