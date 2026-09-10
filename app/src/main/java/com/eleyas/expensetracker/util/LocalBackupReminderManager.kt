package com.eleyas.expensetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.NotificationStorage
import com.eleyas.expensetracker.model.NotificationItem

object LocalBackupReminderManager {
    private const val CHANNEL_ID = "local_backup_reminder_channel"
    private const val NOTIFICATION_ID = 3001

    fun showAfterEntry(context: Context, userId: String, backupCreated: Boolean) {
        val title = "লোকাল ব্যাকআপ রিমাইন্ডার"
        val message = if (backupCreated) {
            "নতুন এন্ট্রির JSON ব্যাকআপ তৈরি হয়েছে। নিরাপদ রাখতে ফাইলটি অন্য কোথাও কপি করে রাখুন।"
        } else {
            "লোকাল JSON ব্যাকআপ তৈরি করা যায়নি। সেটিংস থেকে ম্যানুয়ালি ব্যাকআপ নিন।"
        }

        val existing = NotificationStorage.load(context, userId).toMutableList()
        existing.add(
            NotificationItem(
                title = title,
                message = message,
                type = "local_backup_reminder",
                timestamp = System.currentTimeMillis()
            )
        )
        NotificationStorage.save(context, existing.takeLast(100), userId)

        createNotificationChannel(context)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "লোকাল ব্যাকআপ রিমাইন্ডার",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "নতুন এন্ট্রির পর JSON ব্যাকআপ অন্যত্র সংরক্ষণের রিমাইন্ডার"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
