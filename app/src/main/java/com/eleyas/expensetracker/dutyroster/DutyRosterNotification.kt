package com.eleyas.expensetracker.dutyroster

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DutyRosterNotification {
    const val ACTION = "com.eleyas.expensetracker.ACTION_DUTY_ROSTER"

    private const val CHANNEL = "duty_roster"
    private const val REQUEST_CODE = 74021

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL,
                "Duty Roster",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily duty roster reminders"
            }

            context
                .getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context) {
        val roster = DutyRosterStorage.load(context) ?: return

        val alarm =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent =
            Intent(context, DutyRosterReceiver::class.java)
                .setAction(ACTION)

        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, roster.notificationHour)
            set(Calendar.MINUTE, roster.notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    fun notifyNow(context: Context) {
        val roster = DutyRosterStorage.load(context) ?: return

        val todayIso =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).format(Calendar.getInstance().time)

        val today =
            roster.days.firstOrNull {
                it.dateIso == todayIso
            } ?: return

        val mine =
            today.duties.firstOrNull {
                it.person.equals(
                    roster.myName,
                    ignoreCase = true
                )
            }

        val morning =
            today.duties
                .filter { DutyRosterParser.isMorning(it.duty) }
                .map { it.person }
                .distinct()

        val off =
            today.duties
                .filter { DutyRosterParser.isOff(it.duty) }
                .map { it.person }
                .distinct()

        val myText =
            mine?.duty?.ifBlank {
                "Roster entry not found"
            } ?: "আজকের entry পাওয়া যায়নি"

        createChannel(context)

        val openIntent =
            Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val contentIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE + 1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val detail = buildString {
            append("👤 My duty: ")
            append(myText)
            append("\n")
            append("🌅 Morning duty: ")
            append(
                morning.joinToString(", ")
                    .ifBlank { "None" }
            )
            append("\n")
            append("🔴 OFF: ")
            append(
                off.joinToString(", ")
                    .ifBlank { "None" }
            )
        }

        val notification =
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_calendar)
                .setContentTitle("Today's Duty Roster")
                .setContentText(myText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(detail)
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

        context
            .getSystemService(NotificationManager::class.java)
            .notify(REQUEST_CODE, notification)
    }
}

class DutyRosterReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        DutyRosterNotification.createChannel(context)

        when (intent?.action) {
            DutyRosterNotification.ACTION -> {
                DutyRosterNotification.notifyNow(context)
                DutyRosterNotification.schedule(context)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                DutyRosterNotification.schedule(context)
            }
        }
    }
}
