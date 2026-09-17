package com.eleyas.expensetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eleyas.expensetracker.MainActivity
import com.eleyas.expensetracker.R
import com.eleyas.expensetracker.model.WishlistItem

object WishlistNotificationManager {

    private const val CHANNEL_ID = "wishlist_affordability"

    fun notifyAffordable(context: Context, item: WishlistItem): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        createChannel(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_calendar)
            .setContentTitle("এখন কিনতে পারবেন! 🎉")
            .setContentText("${item.name} কেনার জন্য আপনার যথেষ্ট ব্যালেন্স আছে।")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${item.name} কিনতে ৳${formatMoney(item.price)} লাগবে। " +
                        "এখন আপনার যথেষ্ট ব্যালেন্স আছে।"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(item.id.hashCode(), notification)
        return true
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "উইশলিস্ট রিমাইন্ডার",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "উইশলিস্টের পণ্য কেনার সামর্থ্য হলে জানায়"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
