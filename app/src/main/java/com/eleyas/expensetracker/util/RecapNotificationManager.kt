package com.eleyas.expensetracker.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object RecapNotificationManager {
    const val ACTION_WEEKLY_RECAP = "com.eleyas.expensetracker.ACTION_WEEKLY_RECAP"
    const val ACTION_MONTHLY_RECAP = "com.eleyas.expensetracker.ACTION_MONTHLY_RECAP"
    const val EXTRA_OPEN_MONTHLY_SUMMARY = "open_monthly_summary"
    const val EXTRA_MONTHLY_START = "monthly_summary_start"
    const val EXTRA_MONTHLY_END = "monthly_summary_end"

    const val CHANNEL_ID_WEEKLY = "weekly_recap_channel"
    const val CHANNEL_ID_MONTHLY = "monthly_recap_channel"
    const val NOTIFICATION_ID_WEEKLY = 2002
    const val NOTIFICATION_ID_MONTHLY = 2003

    private const val PREFS_NAME = "recap_notification_prefs"
    private const val KEY_WEEKLY_ENABLED = "weekly_recap_enabled"
    private const val KEY_MONTHLY_ENABLED = "monthly_recap_enabled"
    private const val REQUEST_CODE_WEEKLY = 1002
    private const val REQUEST_CODE_MONTHLY = 1003

    fun isWeeklyRecapEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WEEKLY_ENABLED, true)

    fun setWeeklyRecapEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_WEEKLY_ENABLED, enabled).apply()
        if (enabled) scheduleWeeklyRecap(context) else cancelWeeklyRecap(context)
    }

    fun isMonthlyRecapEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_MONTHLY_ENABLED, true)

    fun setMonthlyRecapEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_MONTHLY_ENABLED, enabled).apply()
        if (enabled) scheduleMonthlyRecap(context) else cancelMonthlyRecap(context)
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_WEEKLY,
                    "সাপ্তাহিক সারসংক্ষেপ",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "সাপ্তাহিক আর্থিক সারসংক্ষেপ" }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_MONTHLY,
                    "১৫–১৫ মাসিক সারসংক্ষেপ",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "প্রতি মাসের ১৫ তারিখে আগের ১৫–১৫ হিসাবের সারসংক্ষেপ" }
            )
        }
    }

    fun scheduleWeeklyRecap(
        context: Context,
        dayOfWeek: Int = Calendar.MONDAY,
        hour: Int = 9,
        minute: Int = 0
    ) {
        if (!isWeeklyRecapEnabled(context)) return
        createNotificationChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_WEEKLY_RECAP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE_WEEKLY, intent, pendingIntentFlags()
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
        }
        scheduleAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    /**
     * Monthly cycle: previous 15th (inclusive) -> current 15th (exclusive).
     * Notification fires on the 15th at 09:00 local device time.
     */
    fun scheduleMonthlyRecap(
        context: Context,
        dayOfMonth: Int = 15,
        hour: Int = 9,
        minute: Int = 0
    ) {
        if (!isMonthlyRecapEnabled(context)) return
        createNotificationChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_MONTHLY_RECAP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE_MONTHLY, intent, pendingIntentFlags()
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.MONTH, 1)
        }
        scheduleAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    private fun pendingIntentFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelWeeklyRecap(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_WEEKLY_RECAP
        }
        alarmManager.cancel(
            PendingIntent.getBroadcast(context, REQUEST_CODE_WEEKLY, intent, pendingIntentFlags())
        )
    }

    fun cancelMonthlyRecap(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_MONTHLY_RECAP
        }
        alarmManager.cancel(
            PendingIntent.getBroadcast(context, REQUEST_CODE_MONTHLY, intent, pendingIntentFlags())
        )
    }

    fun getWeeklyExpenseSummary(prefs: SharedPreferences): String {
        val transactions = loadTransactions(prefs)
        val currentDate = Calendar.getInstance()
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expenses = transactions.filter { transaction ->
            transaction.type == "expense" && try {
                val txDate = dateFormat.parse(transaction.date)
                txDate != null && txDate >= weekAgo.time && txDate <= currentDate.time
            } catch (_: Exception) {
                false
            }
        }
        val totalExpense = expenses.sumOf { it.amount }
        return if (totalExpense > 0) {
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            "এই সপ্তাহে (\${sdf.format(weekAgo.time)}-\${sdf.format(currentDate.time)}) মোট খরচ: ৳ \${"%.2f".format(totalExpense)}"
        } else {
            "এই সপ্তাহে কোনো খরচ নেই"
        }
    }

    fun getMonthlyCycleSummary(prefs: SharedPreferences): String {
        val (start, end) = MonthlySummaryUtils.currentCycle()
        val summary = MonthlySummaryUtils.forPeriod(loadTransactions(prefs), start, end)
        val expense = summary.expenseTransactions.sumOf { it.amount }
        val period = MonthlySummaryUtils.notificationPeriod(start, end)
        return "হিসাব প্রস্তুত • \$period • খরচ: ৳ \${"%.2f".format(expense)}"
    }

    fun getMonthlyExpenseSummary(prefs: SharedPreferences): String = getMonthlyCycleSummary(prefs)
}
