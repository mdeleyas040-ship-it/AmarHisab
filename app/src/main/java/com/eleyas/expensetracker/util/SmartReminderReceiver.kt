package com.eleyas.expensetracker.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eleyas.expensetracker.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SmartReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        when (intent.action) {

            SmartReminderScheduler.ACTION_SMART_REMINDER -> {
                showSmartReminders(context)
                SmartReminderScheduler.scheduleNext(context)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SmartReminderScheduler.scheduleNext(context)
            }
        }
    }

    private fun showSmartReminders(
        context: Context
    ) {

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                ?: "guest"

        val prefs =
            AccountStorage.getPrefs(
                context,
                userId
            )

        val transactions =
            loadTransactions(prefs)

        val reminders =
            SmartReminderManager
                .getTransactionReminders(transactions)

        if (reminders.isEmpty()) return

        reminders.forEach { reminder ->

            if (
                SmartReminderStorage.hasBeenSent(
                    context,
                    userId,
                    reminder.transactionId
                )
            ) {
                return@forEach
            }

            showNotification(
                context,
                reminder
            )

            SmartReminderStorage.markAsSent(
                context,
                userId,
                reminder.transactionId
            )
        }
    }

    private fun showNotification(
        context: Context,
        reminder: SmartReminder
    ) {

        SmartReminderScheduler
            .createNotificationChannel(context)

        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                putExtra(
                    SmartReminderScheduler.EXTRA_TRANSACTION_ID,
                    reminder.transactionId
                )

                putExtra(
                    SmartReminderScheduler.EXTRA_TRANSACTION_TYPE,
                    reminder.type
                )
            }

        val pendingFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                reminder.transactionId.hashCode(),
                intent,
                pendingFlags
            )

        val notification =
            NotificationCompat.Builder(
                context,
                SmartReminderScheduler.CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    reminder.title
                )
                .setContentText(
                    reminder.message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(reminder.message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        manager.notify(
            reminder.transactionId.hashCode(),
            notification
        )
    }
}