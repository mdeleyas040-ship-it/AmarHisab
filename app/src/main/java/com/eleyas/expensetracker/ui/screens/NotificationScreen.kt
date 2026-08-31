package com.eleyas.expensetracker.ui.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    notifications: List<NotificationItem>,
    onNotificationClick: (NotificationItem) -> Unit = {}
) {

    val sortedNotifications =
        notifications.sortedByDescending {
            it.timestamp
        }

    val groupedNotifications =
        sortedNotifications.groupBy {

            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ENGLISH
            ).format(
                Date(it.timestamp)
            )
        }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = "Notifications",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (notifications.isEmpty()) {
                            "No new activity"
                        } else {
                            "Your latest updates"
                        },
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                shape =
                    RoundedCornerShape(50.dp),
                color =
                    MaterialTheme.colorScheme
                        .surfaceVariant
            ) {

                Text(
                    text =
                        "${notifications.size}",
                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        // ---------------------------------------------------------
        // EMPTY STATE
        // ---------------------------------------------------------

        if (notifications.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Surface(
                    modifier =
                        Modifier.size(76.dp),
                    shape = CircleShape,
                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "🔔",
                            style =
                                MaterialTheme.typography
                                    .displaySmall
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.size(16.dp)
                )

                Text(
                    text =
                        "No notifications yet",
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        "Your notifications will appear here.",
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.padding(top = 5.dp)
                )
            }

        } else {

            // -----------------------------------------------------
            // NOTIFICATION LIST
            // -----------------------------------------------------

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(
                            top = 4.dp,
                            bottom = 24.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {

                groupedNotifications.forEach {
                        (_, dayNotifications) ->

                    item {

                        val firstNotification =
                            dayNotifications.first()

                        val dayText =
                            SimpleDateFormat(
                                "EEEE, dd MMMM yyyy",
                                Locale.ENGLISH
                            ).format(
                                Date(
                                    firstNotification.timestamp
                                )
                            )

                        Text(
                            text = dayText,
                            style =
                                MaterialTheme.typography
                                    .titleSmall,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurface,
                            modifier =
                                Modifier
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
                        key = {
                            it.id
                        }
                    ) { notification ->

                        NotificationCard(
                            notification =
                                notification,
                            onClick = {
                                onNotificationClick(
                                    notification
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// =================================================================
// NOTIFICATION CARD
// =================================================================

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {

    val isOnThisDay =
        notification.type.equals(
            "on_this_day",
            ignoreCase = true
        )

    if (isOnThisDay) {

        OnThisDayNotificationCard(
            notification = notification,
            onClick = onClick
        )

    } else {

        StandardNotificationCard(
            notification = notification,
            onClick = onClick
        )
    }
}

// =================================================================
// ON THIS DAY — PREMIUM CARD
// =================================================================

@Composable
private fun OnThisDayNotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
            .clickable {
                onClick()
            },
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (!notification.isRead) {
                        MaterialTheme.colorScheme
                            .primaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .surface
                    }
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // Premium icon
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "📅",
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )
                }

                Spacer(
                    modifier =
                        Modifier.size(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            notification.title
                                .ifBlank {
                                    "এই দিনে আপনার হিসাব"
                                },
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "পুরনো লেনদেনের স্মৃতি",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .primary,
                        fontWeight =
                            FontWeight.SemiBold,
                        modifier =
                            Modifier.padding(
                                top = 2.dp
                            )
                    )
                }

                if (!notification.isRead) {

                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.size(14.dp)
            )

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant
            )

            Spacer(
                modifier =
                    Modifier.size(12.dp)
            )

            Text(
                text =
                    notification.message,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Medium
            )

            Spacer(
                modifier =
                    Modifier.size(10.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        getNotificationTime(
                            notification.timestamp
                        ),
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text =
                        "বিস্তারিত দেখুন  ›",
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

// =================================================================
// STANDARD NOTIFICATION CARD
// =================================================================

@Composable
private fun StandardNotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {

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
        shape =
            RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (!notification.isRead) {
                        MaterialTheme.colorScheme
                            .primaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .surface
                    }
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme
                                .primary
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        getNotificationIcon(
                            notification.type
                        ),
                    style =
                        MaterialTheme.typography
                            .titleLarge
                )
            }

            Spacer(
                modifier =
                    Modifier.size(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        notification.title,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        if (!notification.isRead) {
                            FontWeight.Bold
                        } else {
                            FontWeight.SemiBold
                        }
                )

                Text(
                    text =
                        notification.message,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )

                Text(
                    text =
                        getNotificationTime(
                            notification.timestamp
                        ),
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            top = 5.dp
                        )
                )
            }

            if (!notification.isRead) {

                Spacer(
                    modifier =
                        Modifier.size(8.dp)
                )

                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme
                                    .primary
                            )
                )
            }
        }
    }
}

// =================================================================
// ICON
// =================================================================

private fun getNotificationIcon(
    type: String
): String {

    return when (
        type.lowercase(
            Locale.getDefault()
        )
    ) {

        "budget" ->
            "⚠️"

        "expense" ->
            "💸"

        "income" ->
            "💰"

        "loan" ->
            "💳"

        "debt" ->
            "💳"

        "reminder" ->
            "🔔"

        "on_this_day" ->
            "📅"

        "success" ->
            "✅"

        "warning" ->
            "⚠️"

        else ->
            "🔔"
    }
}

// =================================================================
// TIME
// =================================================================

private fun getNotificationTime(
    timestamp: Long
): String {

    val now =
        System.currentTimeMillis()

    val difference =
        now - timestamp

    val minute =
        60_000L

    val hour =
        60 * minute

    val day =
        24 * hour

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