package com.eleyas.expensetracker.dutyroster

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import com.eleyas.expensetracker.ui.components.AmarHisabFeatureHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyRosterScreen(
    context: Context,
    onBack: () -> Unit
) {
    var savedRoster by remember { mutableStateOf(DutyRosterStorage.load(context)) }
    var draftDays by remember { mutableStateOf<List<RosterDay>?>(null) }
    var draftName by remember { mutableStateOf("") }

    val notificationTime = remember { DutyRosterStorage.notificationTime(context) }
    var hourText by remember { mutableStateOf(notificationTime.first.toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(notificationTime.second.toString().padStart(2, '0')) }

    var scanning by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNameMenu by remember { mutableStateOf(false) }
    var preparationText by remember { mutableStateOf(DutyRosterStorage.preparationTasks(context).joinToString("\n")) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scanning = true
        error = null
        draftName = ""

        DutyRosterManager.scan(
            context = context,
            uri = uri,
            onSuccess = {
                draftDays = it
                scanning = false
            },
            onError = {
                error = it
                scanning = false
            }
        )
    }

    val activeDays = draftDays ?: savedRoster?.days.orEmpty()
    val names = activeDays.flatMap { it.duties.map { duty -> duty.person } }.distinct().sorted()

    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
    val today = activeDays.firstOrNull { it.dateIso == todayIso }
    val lastRosterDay = activeDays.maxByOrNull { it.dateIso }
    val isFinalRosterDay =
        savedRoster != null &&
                draftDays == null &&
                today != null &&
                lastRosterDay?.dateIso == today.dateIso

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AmarHisabFeatureHeader(
                title = "Duty Roster",
                subtitle = "Kitchen • Fish Fire",
                onBack = onBack,
                action = {
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        enabled = !scanning
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload roster")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(2.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.padding(13.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Smart Duty Roster",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (savedRoster != null)
                                        "Your roster is synced and ready."
                                    else
                                        "Upload a roster image to get started.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            if (savedRoster != null) {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        "ACTIVE",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        if (savedRoster != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Roster active • Daily reminder enabled",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MiniStat("Your name", savedRoster?.myName.orEmpty())
                                MiniStat("Days", activeDays.size.toString())
                                MiniStat(
                                    "Reminder",
                                    String.format(
                                        Locale.US,
                                        "%02d:%02d",
                                        savedRoster?.notificationHour ?: 6,
                                        savedRoster?.notificationMinute ?: 30
                                    )
                                )
                            }

                            if (savedRoster != null) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                                )
                                NextOffCountdownCard(
                                    roster = savedRoster!!,
                                    nowMillis = nowMillis
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                                )
                                PreparationTasksCard(
                                    text = preparationText,
                                    onTextChange = { preparationText = it },
                                    onSave = {
                                        DutyRosterStorage.setPreparationTasks(
                                            context,
                                            preparationText.lines()
                                                .map { it.trim() }
                                                .filter { it.isNotBlank() }
                                        )
                                        preparationText = DutyRosterStorage.preparationTasks(context).joinToString("\n")
                                        DutyRosterNotification.schedule(context)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isFinalRosterDay) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(17.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(13.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                ) {
                                    Icon(
                                        Icons.Default.EventNote,
                                        contentDescription = null,
                                        modifier = Modifier.padding(10.dp),
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }

                                Spacer(Modifier.width(11.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Roster শেষ দিন",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "আজকের রাতেই নতুন roster upload করুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                ) {
                                    Text(
                                        "ACTION",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }

                            Text(
                                "পুরোনো roster শেষ হওয়ার আগেই নতুন roster scan করে save করলে পরের duty cycle ready থাকবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Button(
                                onClick = { launcher.launch("image/*") },
                                enabled = !scanning,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Upload New Roster")
                            }
                        }
                    }
                }
            }

            if (today != null && draftDays == null) {
                item {
                    TodayDutyCard(
                        today = today,
                        myName = savedRoster?.myName.orEmpty()
                    )
                }

                            }

            item {
                SectionLabel(
                    icon = Icons.Default.CloudUpload,
                    title = if (draftDays != null) "New Roster" else "Roster Source",
                    subtitle = if (draftDays != null)
                        "Review the scanned roster before saving."
                    else
                        "Keep your roster image clear and readable."
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Upload a roster image",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "The app reads dates, staff names and duty times automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { launcher.launch("image/*") },
                            enabled = !scanning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                if (scanning) Icons.Default.Refresh else Icons.Default.CloudUpload,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (scanning) "Reading roster…" else "Upload Roster Image")
                        }

                        if (scanning) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "Analysing roster cells. Please wait…",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        error?.let {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (draftDays != null) {
                item {
                    ReviewRosterCard(
                        days = draftDays.orEmpty(),
                        names = names,
                        draftName = draftName,
                        showNameMenu = showNameMenu,
                        onShowNameMenu = { showNameMenu = true },
                        onDismissNameMenu = { showNameMenu = false },
                        onSelectName = {
                            draftName = it
                            showNameMenu = false
                        },
                        hourText = hourText,
                        minuteText = minuteText,
                        onHourChange = {
                            hourText = it.filter(Char::isDigit).take(2)
                        },
                        onMinuteChange = {
                            minuteText = it.filter(Char::isDigit).take(2)
                        },
                        onSave = {
                            val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 6
                            val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 30

                            DutyRosterStorage.save(
                                context,
                                DutyRoster(
                                    uploadedAt = System.currentTimeMillis(),
                                    sourceName = "Roster image",
                                    myName = draftName.trim(),
                                    notificationHour = hour,
                                    notificationMinute = minute,
                                    days = draftDays.orEmpty()
                                )
                            )

                            DutyRosterNotification.createChannel(context)
                            DutyRosterNotification.schedule(context)

                            savedRoster = DutyRosterStorage.load(context)
                            draftDays = null
                        }
                    )
                }
            }

            if (today != null && draftDays == null) {
                item {
                    SectionLabel(
                        icon = Icons.Default.EventNote,
                        title = "Today's Full Roster",
                        subtitle = "Everyone scheduled for today."
                    )
                }

                items(
                    today.duties,
                    key = { it.person + "-" + it.duty }
                ) { entry ->
                    RosterPersonRow(
                        entry = entry,
                        isMe = entry.person.equals(
                            savedRoster?.myName,
                            ignoreCase = true
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TodayDutyCard(
    today: RosterDay,
    myName: String
) {
    val mine = today.duties.firstOrNull {
        it.person.equals(myName, ignoreCase = true)
    }

    val morning = today.duties
        .filter { DutyRosterParser.isMorning(it.duty) }
        .map { it.person }
        .distinct()

    val off = today.duties
        .filter { DutyRosterParser.isOff(it.duty) }
        .map { it.person }
        .distinct()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Today's Duty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "LIVE",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        today.displayDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "${today.duties.size} staff",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            DutyHighlight(
                title = "My duty",
                value = mine?.duty ?: "Not found",
                icon = Icons.Default.Person,
                highlighted = true
            )

            DutyHighlight(
                title = "Morning staff",
                value = morning.joinToString(", ").ifBlank { "None" },
                icon = Icons.Default.EventNote
            )

            DutyHighlight(
                title = "OFF today",
                value = off.joinToString(", ").ifBlank { "None" },
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Composable
private fun ReviewRosterCard(
    days: List<RosterDay>,
    names: List<String>,
    draftName: String,
    showNameMenu: Boolean,
    onShowNameMenu: () -> Unit,
    onDismissNameMenu: () -> Unit,
    onSelectName: (String) -> Unit,
    hourText: String,
    minuteText: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(9.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Review & Confirm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${days.size} days detected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "Select your name manually. OCR will not guess your identity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onShowNameMenu,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    draftName.ifBlank { "Select your name" },
                    fontWeight = if (draftName.isBlank())
                        FontWeight.Normal else FontWeight.SemiBold
                )
            }

            DropdownMenu(
                expanded = showNameMenu,
                onDismissRequest = onDismissNameMenu
            ) {
                names.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { onSelectName(name) }
                    )
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Daily reminder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Choose when you want the roster notification.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = onHourChange,
                    label = { Text("Hour") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text(":", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = onMinuteChange,
                    label = { Text("Minute") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Example 06:30 — morning duty reminder",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Button(
                onClick = onSave,
                enabled = draftName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Roster")
            }

            if (draftName.isBlank()) {
                Text(
                    "Select your name to continue.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RosterPersonRow(
    entry: DutyEntry,
    isMe: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isMe)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isMe)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    if (isMe) Icons.Default.Person else Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (isMe)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.person,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isMe) {
                        Spacer(Modifier.width(7.dp))
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "YOU",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))
                DutyChips(entry.duty)
            }
        }
    }
}

@Composable
private fun DutyChips(duty: String) {
    val parts = duty
        .split(" • ")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) {
        Text(
            "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        parts.take(2).forEachIndexed { index, part ->
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (parts.size > 1)
                            if (index == 0) "AM  ${part}" else "PM  ${part}"
                        else
                            part,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when {
                        parts.size <= 1 -> MaterialTheme.colorScheme.surfaceVariant
                        index == 0 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    leadingIconContentColor = when {
                        parts.size <= 1 -> MaterialTheme.colorScheme.primary
                        index == 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            )
        }
    }
}

@Composable
private fun DutyHighlight(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlighted: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = if (highlighted)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = if (highlighted)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
        else
            null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (highlighted) "YOUR DUTY" else title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
