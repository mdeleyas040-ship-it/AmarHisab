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
    private const val LAST_DAY_REQUEST_CODE = 74022
    private const val LAST_DAY_NOTIFICATION_ID = 74022
    private const val LAST_DAY_HOUR = 21
    private const val LAST_DAY_MINUTE = 0
    const val EXTRA_OPEN_DUTY_ROSTER = "open_duty_roster"

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

        scheduleLastDayReminder(context, roster)
    }

    private fun scheduleLastDayReminder(
        context: Context,
        roster: DutyRoster
    ) {
        val alarm =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent =
            Intent(context, DutyRosterReceiver::class.java)
                .setAction(ACTION)
                .putExtra("last_day_reminder", true)

        val pending = PendingIntent.getBroadcast(
            context,
            LAST_DAY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        alarm.cancel(pending)

        val lastDay = roster.days.maxByOrNull { it.dateIso } ?: return

        val lastDate = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .parse(lastDay.dateIso)
        }.getOrNull() ?: return

        val trigger = Calendar.getInstance().apply {
            time = lastDate
            set(Calendar.HOUR_OF_DAY, LAST_DAY_HOUR)
            set(Calendar.MINUTE, LAST_DAY_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (trigger.timeInMillis <= System.currentTimeMillis()) return

        alarm.set(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            pending
        )
    }

    fun notifyLastDay(context: Context) {
        createChannel(context)

        val openIntent =
            Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_DUTY_ROSTER, true)
            }

        val contentIntent =
            PendingIntent.getActivity(
                context,
                LAST_DAY_REQUEST_CODE + 1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_calendar)
                .setContentTitle("Duty Roster • New Roster Needed")
                .setContentText("আজকের নতুন roster upload করুন")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "আজ আপনার বর্তমান roster-এর শেষ দিন।\n" +
                                    "নতুন roster image upload করে পরের সপ্তাহের duty ready করুন।"
                        )
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

        context
            .getSystemService(NotificationManager::class.java)
            .notify(LAST_DAY_NOTIFICATION_ID, notification)
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
                if (intent?.getBooleanExtra("last_day_reminder", false) == true) {
                    DutyRosterNotification.notifyLastDay(context)
                } else {
                    DutyRosterNotification.notifyNow(context)
                }
                DutyRosterNotification.schedule(context)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                DutyRosterNotification.schedule(context)
            }
        }
    }
}
