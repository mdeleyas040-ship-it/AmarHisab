package com.eleyas.expensetracker.meal

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MealReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            MealReminderManager.scheduleAll(context)
            return
        }

        if (intent.action != MealReminderManager.ACTION_MEAL_REMINDER ||
            !MealReminderManager.isEnabled(context)
        ) return

        val mealType = intent.getStringExtra(MealReminderManager.EXTRA_MEAL_TYPE) ?: return
        MealReminderManager.createNotificationChannel(context)

        val detailIntent = Intent(context, MealDetailsActivity::class.java).apply {
            putExtra(MealReminderManager.EXTRA_MEAL_TYPE, mealType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(
            context,
            3200 + mealType.hashCode(),
            detailIntent,
            flags
        )

        val (title, message, notificationId) = when (mealType) {
            "breakfast" -> Triple(
                "🍳 সকালের খাবারের সময়",
                "আজকের সুষম নাশতার তালিকা দেখতে চাপুন।",
                3201
            )
            "lunch" -> Triple(
                "🍱 দুপুরের খাবারের সময়",
                "আজকের সুষম দুপুরের খাবারের তালিকা দেখতে চাপুন।",
                3202
            )
            else -> Triple(
                "🍽️ রাতের খাবারের সময়",
                "আজকের সুষম রাতের খাবারের তালিকা দেখতে চাপুন।",
                3203
            )
        }

        val notification = NotificationCompat.Builder(context, MealReminderManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(notificationId, notification)
        }

        MealReminderManager.reschedule(context, mealType)
    }
}
