package com.eleyas.expensetracker.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.NotificationItem
import com.eleyas.expensetracker.NotificationStorage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

object RecapNotificationManager {
    const val ACTION_WEEKLY_RECAP = "com.eleyas.expensetracker.ACTION_WEEKLY_RECAP"
    const val ACTION_MONTHLY_RECAP = "com.eleyas.expensetracker.ACTION_MONTHLY_RECAP"
    
    const val CHANNEL_ID_WEEKLY = "weekly_recap_channel"
    const val CHANNEL_ID_MONTHLY = "monthly_recap_channel"
    
    const val NOTIFICATION_ID_WEEKLY = 2002
    const val NOTIFICATION_ID_MONTHLY = 2003
    
    private const val PREFS_NAME = "recap_notification_prefs"
    private const val KEY_WEEKLY_ENABLED = "weekly_recap_enabled"
    private const val KEY_MONTHLY_ENABLED = "monthly_recap_enabled"
    private const val REQUEST_CODE_WEEKLY = 1002
    private const val REQUEST_CODE_MONTHLY = 1003

    fun isWeeklyRecapEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_WEEKLY_ENABLED, true)
    }

    fun setWeeklyRecapEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_WEEKLY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleWeeklyRecap(context)
        } else {
            cancelWeeklyRecap(context)
        }
    }

    fun isMonthlyRecapEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MONTHLY_ENABLED, true)
    }

    fun setMonthlyRecapEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MONTHLY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleMonthlyRecap(context)
        } else {
            cancelMonthlyRecap(context)
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Weekly Recap Channel
            val weeklyName = "সাপ্তাহিক সারসংক্ষেপ"
            val weeklyDescription = "প্রতি সপ্তাহের শুরুতে আপনার খরচের সারসংক্ষেপ"
            val weeklyChannel = NotificationChannel(
                CHANNEL_ID_WEEKLY,
                weeklyName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = weeklyDescription
            }
            notificationManager.createNotificationChannel(weeklyChannel)

            // Monthly Recap Channel
            val monthlyName = "মাসিক সারসংক্ষেপ"
            val monthlyDescription = "প্রতি মাসের শেষে আপনার খরচের সারসংক্ষেপ"
            val monthlyChannel = NotificationChannel(
                CHANNEL_ID_MONTHLY,
                monthlyName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = monthlyDescription
            }
            notificationManager.createNotificationChannel(monthlyChannel)
        }
    }

    fun scheduleWeeklyRecap(context: Context, dayOfWeek: Int = Calendar.MONDAY, hour: Int = 9, minute: Int = 0) {
        if (!isWeeklyRecapEnabled(context)) return

        createNotificationChannels(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_WEEKLY_RECAP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_WEEKLY, intent, flags)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
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
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleMonthlyRecap(context: Context, dayOfMonth: Int = 1, hour: Int = 9, minute: Int = 0) {
        if (!isMonthlyRecapEnabled(context)) return

        createNotificationChannels(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_MONTHLY_RECAP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_MONTHLY, intent, flags)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MONTH, 1)
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
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelWeeklyRecap(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_WEEKLY_RECAP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_WEEKLY, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun cancelMonthlyRecap(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecapNotificationReceiver::class.java).apply {
            action = ACTION_MONTHLY_RECAP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_MONTHLY, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun getWeeklyExpenseSummary(prefs: SharedPreferences): String {
        val transactions = loadTransactions(prefs)
        val currentDate = Calendar.getInstance()
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val weeklyExpenses = transactions.filter { transaction ->
            transaction.type == "expense" && try {
                val txDate = dateFormat.parse(transaction.date)
                txDate != null && txDate >= weekAgo.time && txDate <= currentDate.time
            } catch (e: Exception) {
                false
            }
        }

        val totalExpense = weeklyExpenses.sumOf { it.amount }

        return if (totalExpense > 0) {
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            val startDate = sdf.format(weekAgo.time)
            val endDate = sdf.format(currentDate.time)
            "এই সপ্তাহে (${startDate}-${endDate}) মোট খরচ: ৳ ${"%.2f".format(totalExpense)}"
        } else {
            "এই সপ্তাহে কোনো খরচ নেই"
        }
    }

    fun getMonthlyExpenseSummary(prefs: SharedPreferences): String {
        val transactions = loadTransactions(prefs)
        val currentDate = Calendar.getInstance()
        val monthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val monthlyExpenses = transactions.filter { transaction ->
            transaction.type == "expense" && try {
                val txDate = dateFormat.parse(transaction.date)
                txDate != null && txDate >= monthAgo.time && txDate <= currentDate.time
            } catch (e: Exception) {
                false
            }
        }

        val totalExpense = monthlyExpenses.sumOf { it.amount }
        val monthFormat = SimpleDateFormat("MMMM", Locale("bn", "BD"))
        val currentMonth = monthFormat.format(currentDate.time)

        return if (totalExpense > 0) {
            "$currentMonth মাসে মোট খরচ: ৳ ${"%.2f".format(totalExpense)}"
        } else {
            "$currentMonth মাসে কোনো খরচ নেই"
        }
    }
}
