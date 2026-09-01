package com.eleyas.expensetracker.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object SmartReminderScheduler {

    const val ACTION_SMART_REMINDER =
        "com.eleyas.expensetracker.ACTION_SMART_REMINDER"

    const val ACTION_SMART_REMINDER_TEST =
        "com.eleyas.expensetracker.ACTION_SMART_REMINDER_TEST"

    const val CHANNEL_ID =
        "smart_reminder_channel_v2"

    const val EXTRA_TRANSACTION_ID =
        "smart_reminder_transaction_id"

    const val EXTRA_TRANSACTION_TYPE =
        "smart_reminder_transaction_type"

    private const val REQUEST_CODE = 3001
    private const val TEST_REQUEST_CODE = 3002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "স্মার্ট রিমাইন্ডার",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "লেনদেন ও হিসাবের স্মার্ট রিমাইন্ডার"
            }

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleNext(
        context: Context,
        hour: Int = 21,
        minute: Int = 0
    ) {
        createNotificationChannel(context)

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                SmartReminderReceiver::class.java
            ).apply {
                action = ACTION_SMART_REMINDER
            }

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                flags
            )

        val calendar =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Development test.
     * Schedules the same receiver with the dedicated test action.
     */
    fun scheduleTest(
        context: Context,
        delayMinutes: Int = 1
    ) {
        createNotificationChannel(context)

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                SmartReminderReceiver::class.java
            ).apply {
                action = ACTION_SMART_REMINDER_TEST
            }

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                TEST_REQUEST_CODE,
                intent,
                flags
            )

        val triggerAt =
            System.currentTimeMillis() +
                    delayMinutes * 60_000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancelTest(context: Context) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                SmartReminderReceiver::class.java
            ).apply {
                action = ACTION_SMART_REMINDER_TEST
            }

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                TEST_REQUEST_CODE,
                intent,
                flags
            )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
