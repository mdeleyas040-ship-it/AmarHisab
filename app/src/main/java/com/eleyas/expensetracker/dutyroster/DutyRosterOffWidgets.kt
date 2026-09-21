package com.eleyas.expensetracker.dutyroster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun NextOffCountdownCard(
    roster: DutyRoster,
    nowMillis: Long
) {
    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowMillis)
    // "Next OFF" means the next upcoming OFF after today.
    // If today is already OFF, skip today so the countdown does not show 00:00:00.
    val nextOff = roster.days
        .filter { it.dateIso > todayIso }
        .sortedBy { it.dateIso }
        .firstOrNull { day ->
            day.duties.firstOrNull {
                it.person.equals(roster.myName, ignoreCase = true)
            }?.duty?.let(DutyRosterParser::isOff) == true
        } ?: return

    val offDate = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(nextOff.dateIso)?.time
    }.getOrNull() ?: return

    val remaining = (offDate - nowMillis).coerceAtLeast(0L)
    val totalSeconds = remaining / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Next OFF",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Next OFF • " + nextOff.displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "LIVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountdownUnit(days.toString().padStart(2, '0'), "Days")
                CountdownUnit(hours.toString().padStart(2, '0'), "Hours")
                CountdownUnit(minutes.toString().padStart(2, '0'), "Minutes")
                CountdownUnit(seconds.toString().padStart(2, '0'), "Seconds")
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f))
    }
}

@Composable
internal fun PreparationTasksCard(
    text: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var showEditor by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val tasks = text.lines().map { it.trim() }.filter { it.isNotBlank() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Preparation Tasks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "OFF-এর আগের দিনের কাজ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
            IconButton(onClick = { showEditor = !showEditor }) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add task",
                        modifier = Modifier.padding(7.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (tasks.isEmpty()) {
            Text(
                "কোনো task যোগ করা হয়নি",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f)
            )
        } else {
            tasks.forEach { task ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "•",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            task,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (showEditor) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5,
                label = { Text("এক লাইনে একটি task") },
                placeholder = { Text("Juice বানানো\nBread preparation\nFace towel count") }
            )

            OutlinedButton(
                onClick = {
                    onSave()
                    showEditor = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Tasks")
            }
        }

        Text(
            "আগামীকাল OFF হলে 4:00 PM notification-এ taskগুলো দেখাবে। Empty থাকলেও notification আসবে।",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f)
        )
    }
}
