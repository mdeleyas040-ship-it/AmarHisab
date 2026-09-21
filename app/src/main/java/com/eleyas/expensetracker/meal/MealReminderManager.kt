package com.eleyas.expensetracker.meal

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object MealReminderManager {
    const val ACTION_MEAL_REMINDER = "com.eleyas.expensetracker.ACTION_MEAL_REMINDER"
    const val EXTRA_MEAL_TYPE = "meal_type"
    const val CHANNEL_ID = "meal_reminder_channel"

    private const val REQUEST_BREAKFAST = 3101
    private const val REQUEST_LUNCH = 3102
    private const val REQUEST_DINNER = 3103

    private const val PREFS = "meal_reminder_prefs"
    private const val KEY_ENABLED = "enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) scheduleAll(context) else cancelAll(context)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "খাবারের রিমাইন্ডার",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "সুষম খাবারের দৈনিক রিমাইন্ডার"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleAll(context: Context) {
        if (!isEnabled(context)) return
        createNotificationChannel(context)
        schedule(context, 7, 0, "breakfast", REQUEST_BREAKFAST)
        schedule(context, 11, 0, "lunch", REQUEST_LUNCH)
        schedule(context, 18, 0, "dinner", REQUEST_DINNER)
    }

    private fun schedule(
        context: Context,
        hour: Int,
        minute: Int,
        mealType: String,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            action = ACTION_MEAL_REMINDER
            putExtra(EXTRA_MEAL_TYPE, mealType)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
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

    fun reschedule(context: Context, mealType: String) {
        if (!isEnabled(context)) return
        when (mealType) {
            "breakfast" -> schedule(context, 7, 0, "breakfast", REQUEST_BREAKFAST)
            "lunch" -> schedule(context, 11, 0, "lunch", REQUEST_LUNCH)
            "dinner" -> schedule(context, 18, 0, "dinner", REQUEST_DINNER)
        }
    }

    fun cancelAll(context: Context) {
        cancel(context, "breakfast", REQUEST_BREAKFAST)
        cancel(context, "lunch", REQUEST_LUNCH)
        cancel(context, "dinner", REQUEST_DINNER)
    }

    private fun cancel(context: Context, mealType: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            action = ACTION_MEAL_REMINDER
            putExtra(EXTRA_MEAL_TYPE, mealType)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        alarmManager.cancel(pendingIntent)
    }
}
