package com.eleyas.expensetracker.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.NotificationStorage
import com.eleyas.expensetracker.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class RecapNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                RecapNotificationManager.createNotificationChannels(context)
                RecapNotificationManager.scheduleWeeklyRecap(context)
                RecapNotificationManager.scheduleMonthlyRecap(context)
            }

            RecapNotificationManager.ACTION_WEEKLY_RECAP -> {
                if (RecapNotificationManager.isWeeklyRecapEnabled(context)) {
                    showWeeklyRecapNotification(context)
                    saveInAppNotification(context, true)
                    RecapNotificationManager.scheduleWeeklyRecap(context)
                }
            }

            RecapNotificationManager.ACTION_MONTHLY_RECAP -> {
                if (RecapNotificationManager.isMonthlyRecapEnabled(context)) {
                    val (start, end) = MonthlySummaryUtils.currentCycle()
                    showMonthlyRecapNotification(context, start, end)
                    saveMonthlyInAppNotification(context, start, end)
                    RecapNotificationManager.scheduleMonthlyRecap(context)
                }
            }
        }
    }

    private fun showWeeklyRecapNotification(context: Context) {
        RecapNotificationManager.createNotificationChannels(context)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val prefs = AccountStorage.getPrefs(context, userId)
        val summary = RecapNotificationManager.getWeeklyExpenseSummary(prefs)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val notification = NotificationCompat.Builder(
            context,
            RecapNotificationManager.CHANNEL_ID_WEEKLY
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("সাপ্তাহিক খরচের সারসংক্ষেপ 📊")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(context, 2002, mainIntent, pendingIntentFlags())
            )
            .setAutoCancel(true)
            .build()

        notifyIfAllowed(context, RecapNotificationManager.NOTIFICATION_ID_WEEKLY, notification)
    }

    private fun showMonthlyRecapNotification(context: Context, start: Date, end: Date) {
        RecapNotificationManager.createNotificationChannels(context)
        val period = MonthlySummaryUtils.notificationPeriod(start, end)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val prefs = AccountStorage.getPrefs(context, userId)
        val report = MonthlySummaryUtils.forPeriod(loadTransactions(prefs), start, end)

        val message = if (report.transactions.isEmpty()) {
            "\$period • কোনো লেনদেন নেই"
        } else {
            "\$period • \${report.transactions.size}টি লেনদেনের রিপোর্ট প্রস্তুত"
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(RecapNotificationManager.EXTRA_OPEN_MONTHLY_SUMMARY, true)
            putExtra(RecapNotificationManager.EXTRA_MONTHLY_START, start.time)
            putExtra(RecapNotificationManager.EXTRA_MONTHLY_END, end.time)
        }

        val notification = NotificationCompat.Builder(
            context,
            RecapNotificationManager.CHANNEL_ID_MONTHLY
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("মাসিক হিসাব প্রস্তুত 📊")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "\$message\n\nনোটিফিকেশন চাপলে পুরো Summary ও PDF Download দেখতে পারবেন।"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(context, 2003, mainIntent, pendingIntentFlags())
            )
            .setAutoCancel(true)
            .build()

        notifyIfAllowed(context, RecapNotificationManager.NOTIFICATION_ID_MONTHLY, notification)
    }

    private fun saveInAppNotification(context: Context, isWeekly: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val existing = NotificationStorage.load(context, userId).toMutableList()
        val prefs = AccountStorage.getPrefs(context, userId)
        val summary = if (isWeekly) {
            RecapNotificationManager.getWeeklyExpenseSummary(prefs)
        } else {
            RecapNotificationManager.getMonthlyExpenseSummary(prefs)
        }

        existing.add(
            NotificationItem(
                title = if (isWeekly) "সাপ্তাহিক খরচের সারসংক্ষেপ 📊" else "মাসিক খরচের সারসংক্ষেপ 📈",
                message = summary,
                type = if (isWeekly) "weekly_recap" else "monthly_recap",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
        NotificationStorage.save(context, existing, userId)
    }

    private fun saveMonthlyInAppNotification(context: Context, start: Date, end: Date) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val existing = NotificationStorage.load(context, userId).toMutableList()
        val prefs = AccountStorage.getPrefs(context, userId)
        val report = MonthlySummaryUtils.forPeriod(loadTransactions(prefs), start, end)
        val period = MonthlySummaryUtils.notificationPeriod(start, end)

        val message = if (report.transactions.isEmpty()) {
            "\$period • কোনো লেনদেন নেই"
        } else {
            "\$period • \${report.transactions.size}টি লেনদেনের রিপোর্ট প্রস্তুত"
        }

        existing.add(
            NotificationItem(
                title = "মাসিক হিসাব প্রস্তুত 📊",
                message = message,
                type = "monthly_recap",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
        NotificationStorage.save(context, existing, userId)
    }

    private fun notifyIfAllowed(
        context: Context,
        id: Int,
        notification: android.app.Notification
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(id, notification)
        }
    }

    private fun pendingIntentFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
}
