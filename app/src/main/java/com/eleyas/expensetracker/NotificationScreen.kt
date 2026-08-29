package com.eleyas.expensetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    notifications: List<NotificationItem>,
    onNotificationClick: (NotificationItem) -> Unit = {}
) {

    val sortedNotifications =
        notifications.sortedByDescending { it.timestamp }

    val groupedNotifications =
        sortedNotifications.groupBy {

            val calendar = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }

            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ENGLISH
            ).format(Date(it.timestamp))
        }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${notifications.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (notifications.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "🔔",
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(
                    modifier = Modifier.size(12.dp)
                )

                Text(
                    text = "No notifications yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Your notifications will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                groupedNotifications.forEach { (_, dayNotifications) ->

                    item {

                        val firstNotification =
                            dayNotifications.first()

                        val dayText =
                            SimpleDateFormat(
                                "EEEE, dd MMMM yyyy",
                                Locale.ENGLISH
                            ).format(
                                Date(firstNotification.timestamp)
                            )

                        Text(
                            text = dayText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    top = 14.dp,
                                    bottom = 6.dp
                                )
                        )
                    }

                    items(
                        items = dayNotifications,
                        key = { it.id }
                    ) { notification ->

                        FacebookNotificationCard(
                            notification = notification,
                            onClick = {
                                onNotificationClick(notification)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FacebookNotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {

    val timeText =
        getNotificationTime(notification.timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 2.dp
            )
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (!notification.isRead) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Notification icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = getNotificationIcon(
                        notification.type
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight =
                        if (!notification.isRead) {
                            FontWeight.Bold
                        } else {
                            FontWeight.SemiBold
                        }
                )

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 3.dp)
                )

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }

            // Unread dot
            if (!notification.isRead) {

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

private fun getNotificationIcon(
    type: String
): String {

    return when (type.lowercase()) {

        "budget" -> "⚠️"
        "expense" -> "💸"
        "income" -> "💰"
        "loan" -> "💳"
        "debt" -> "💳"
        "reminder" -> "🔔"
        "success" -> "✅"
        "warning" -> "⚠️"

        else -> "🔔"
    }
}

private fun getNotificationTime(
    timestamp: Long
): String {

    val now = System.currentTimeMillis()

    val difference = now - timestamp

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {

        difference < minute ->
            "Just now"

        difference < hour ->
            "${difference / minute} min ago"

        difference < day ->
            "${difference / hour} hours ago"

        difference < 2 * day ->
            "Yesterday"

        difference < 7 * day ->
            "${difference / day} days ago"

        else ->
            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.ENGLISH
            ).format(
                Date(timestamp)
            )
    }
}