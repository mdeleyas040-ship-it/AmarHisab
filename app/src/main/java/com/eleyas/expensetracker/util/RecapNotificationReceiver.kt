package com.eleyas.expensetracker.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.NotificationItem
import com.eleyas.expensetracker.NotificationStorage
import com.google.firebase.auth.FirebaseAuth

class RecapNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            RecapNotificationManager.ACTION_WEEKLY_RECAP -> {
                if (RecapNotificationManager.isWeeklyRecapEnabled(context)) {
                    showWeeklyRecapNotification(context)
                    saveInAppNotification(context, true)
                    RecapNotificationManager.scheduleWeeklyRecap(context)
                }
            }
            RecapNotificationManager.ACTION_MONTHLY_RECAP -> {
                if (RecapNotificationManager.isMonthlyRecapEnabled(context)) {
                    showMonthlyRecapNotification(context)
                    saveInAppNotification(context, false)
                    RecapNotificationManager.scheduleMonthlyRecap(context)
                }
            }
        }
    }

    private fun showWeeklyRecapNotification(context: Context) {
        RecapNotificationManager.createNotificationChannels(context)

        val prefs = context.getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val summary = RecapNotificationManager.getWeeklyExpenseSummary(prefs)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags)

        val notification = NotificationCompat.Builder(context, RecapNotificationManager.CHANNEL_ID_WEEKLY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("সাপ্তাহিক খরচের সারসংক্ষেপ 📊")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
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
                notificationManager.notify(RecapNotificationManager.NOTIFICATION_ID_WEEKLY, notification)
            }
        } else {
            notificationManager.notify(RecapNotificationManager.NOTIFICATION_ID_WEEKLY, notification)
        }
    }

    private fun showMonthlyRecapNotification(context: Context) {
        RecapNotificationManager.createNotificationChannels(context)

        val prefs = context.getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val summary = RecapNotificationManager.getMonthlyExpenseSummary(prefs)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags)

        val notification = NotificationCompat.Builder(context, RecapNotificationManager.CHANNEL_ID_MONTHLY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("মাসিক খরচের সারসংক্ষেপ 📈")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
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
                notificationManager.notify(RecapNotificationManager.NOTIFICATION_ID_MONTHLY, notification)
            }
        } else {
            notificationManager.notify(RecapNotificationManager.NOTIFICATION_ID_MONTHLY, notification)
        }
    }

    private fun saveInAppNotification(context: Context, isWeekly: Boolean) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val existing = NotificationStorage.load(context, currentUserId).toMutableList()

        val prefs = context.getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val summary = if (isWeekly) {
            RecapNotificationManager.getWeeklyExpenseSummary(prefs)
        } else {
            RecapNotificationManager.getMonthlyExpenseSummary(prefs)
        }

        val newItem = NotificationItem(
            title = if (isWeekly) "সাপ্তাহিক খরচের সারসংক্ষেপ 📊" else "মাসিক খরচের সারসংক্ষেপ 📈",
            message = summary,
            type = if (isWeekly) "weekly_recap" else "monthly_recap",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        existing.add(newItem)
        NotificationStorage.save(context, existing, currentUserId)
    }
}
