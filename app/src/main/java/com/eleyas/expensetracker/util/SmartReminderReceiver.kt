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
import com.eleyas.expensetracker.R
import com.google.firebase.auth.FirebaseAuth

class SmartReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        when (intent.action) {

            SmartReminderScheduler.ACTION_SMART_REMINDER -> {

                showOnThisDayNotification(context)

                SmartReminderScheduler.scheduleNext(
                    context
                )
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                SmartReminderScheduler.scheduleNext(
                    context
                )
            }
        }
    }

    private fun showOnThisDayNotification(
        context: Context
    ) {

        val userId =
            FirebaseAuth
                .getInstance()
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
                .getTransactionReminders(
                    transactions
                )

        if (reminders.isEmpty()) {
            return
        }

        /*
         * একই দিনে পাওয়া সব historical
         * transaction এক notification-এ।
         */
        val firstReminder =
            reminders.first()

        val transactionCount =
            reminders.size

        val message =
            if (transactionCount == 1) {

                firstReminder.message

            } else {

                "আজকের দিনে আগের বছরগুলোতে " +
                        "$transactionCount টি " +
                        "লেনদেন করেছিলেন। " +
                        "বিস্তারিত দেখতে ট্যাপ করুন।"
            }

        showNotification(
            context = context,
            title = "📅 এই দিনে আপনার হিসাব",
            message = message,
            transactionId =
                firstReminder.transactionId
        )
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        transactionId: Long
    ) {

        SmartReminderScheduler
            .createNotificationChannel(
                context
            )

        /*
         * Notification-এ tap করলে MainActivity খুলবে।
         * তারপর MainActivity onThisDay screen দেখাবে।
         */
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
                    SmartReminderScheduler
                        .EXTRA_TRANSACTION_ID,
                    transactionId
                )

                putExtra(
                    SmartReminderScheduler
                        .EXTRA_TRANSACTION_TYPE,
                    "on_this_day"
                )
            }

        val pendingFlags =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE

            } else {

                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                9001,
                intent,
                pendingFlags
            )

        val notification =
            NotificationCompat.Builder(
                context,
                SmartReminderScheduler.CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable
                        .ic_dialog_info
                )
                .setContentTitle(
                    title
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
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
                    android.Manifest.permission
                        .POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        manager.notify(
            9001,
            notification
        )
    }
}