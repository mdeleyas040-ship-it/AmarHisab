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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyRosterScreen(
    context: Context,
    onBack: () -> Unit
) {
    var savedRoster by remember {
        mutableStateOf(DutyRosterStorage.load(context))
    }

    var draftDays by remember {
        mutableStateOf<List<RosterDay>?>(null)
    }

    var draftName by remember {
        mutableStateOf("")
    }

    val notificationTime = remember {
        DutyRosterStorage.notificationTime(context)
    }

    var hourText by remember {
        mutableStateOf(
            notificationTime.first.toString().padStart(2, '0')
        )
    }

    var minuteText by remember {
        mutableStateOf(
            notificationTime.second.toString().padStart(2, '0')
        )
    }

    var scanning by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNameMenu by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(
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

    val names =
        activeDays
            .flatMap { day -> day.duties.map { it.person } }
            .distinct()
            .sorted()

    val todayIso =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Calendar.getInstance().time)

    val today =
        activeDays.firstOrNull { it.dateIso == todayIso }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Duty Roster",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Kitchen • Fish Fire",
                            style =
                                MaterialTheme.typography.labelSmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            launcher.launch("image/*")
                        },
                        enabled = !scanning
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "Upload roster"
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            item {
                Spacer(Modifier.padding(top = 2.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .primaryContainer
                        )
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color =
                                MaterialTheme.colorScheme
                                    .primary
                        ) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                modifier =
                                    Modifier.padding(12.dp),
                                tint =
                                    MaterialTheme.colorScheme
                                        .onPrimary
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Smart Duty Roster",
                                style =
                                    MaterialTheme.typography
                                        .titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                if (savedRoster != null) {
                                    "Roster is ready for daily reminders."
                                } else {
                                    "Upload your roster to get daily reminders."
                                },
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Roster Source",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Upload a clear roster image. The app will read the dates, staff and duty cells.",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                launcher.launch("image/*")
                            },
                            enabled = !scanning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (scanning) {
                                    "Reading roster..."
                                } else {
                                    "Upload Roster Image"
                                }
                            )
                        }

                        if (scanning) {
                            Text(
                                "Please wait while the roster is being analysed.",
                                style =
                                    MaterialTheme.typography.labelMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .primary
                            )
                        }

                        error?.let {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color =
                                    MaterialTheme.colorScheme
                                        .errorContainer
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint =
                                            MaterialTheme.colorScheme
                                                .onErrorContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        it,
                                        style =
                                            MaterialTheme.typography
                                                .bodySmall,
                                        color =
                                            MaterialTheme.colorScheme
                                                .onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (draftDays != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme
                                            .primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Review & Confirm",
                                    style =
                                        MaterialTheme.typography
                                            .titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                "Choose your name manually. The app will never guess your identity from OCR.",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = {
                                    showNameMenu = true
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    draftName.ifBlank {
                                        "Select your name"
                                    }
                                )
                            }

                            DropdownMenu(
                                expanded = showNameMenu,
                                onDismissRequest = {
                                    showNameMenu = false
                                }
                            ) {
                                names.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            draftName = name
                                            showNameMenu = false
                                        }
                                    )
                                }
                            }

                            HorizontalDivider()

                            Text(
                                "Daily notification",
                                style =
                                    MaterialTheme.typography
                                        .titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape =
                                        RoundedCornerShape(12.dp),
                                    color =
                                        MaterialTheme.colorScheme
                                            .secondaryContainer
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        modifier =
                                            Modifier.padding(10.dp),
                                        tint =
                                            MaterialTheme.colorScheme
                                                .onSecondaryContainer
                                    )
                                }

                                OutlinedTextField(
                                    value = hourText,
                                    onValueChange = {
                                        hourText =
                                            it.filter(
                                                Char::isDigit
                                            ).take(2)
                                    },
                                    label = { Text("Hour") },
                                    singleLine = true,
                                    modifier =
                                        Modifier.weight(1f)
                                )

                                Text(
                                    ":",
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = minuteText,
                                    onValueChange = {
                                        minuteText =
                                            it.filter(
                                                Char::isDigit
                                            ).take(2)
                                    },
                                    label = { Text("Minute") },
                                    singleLine = true,
                                    modifier =
                                        Modifier.weight(1f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color =
                                    MaterialTheme.colorScheme
                                        .surfaceVariant
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Example: 06:30 AM — daily duty reminder",
                                        style =
                                            MaterialTheme.typography
                                                .labelMedium
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val hour =
                                        hourText
                                            .toIntOrNull()
                                            ?.coerceIn(0, 23)
                                            ?: 6

                                    val minute =
                                        minuteText
                                            .toIntOrNull()
                                            ?.coerceIn(0, 59)
                                            ?: 30

                                    DutyRosterStorage.save(
                                        context,
                                        DutyRoster(
                                            uploadedAt =
                                                System.currentTimeMillis(),
                                            sourceName =
                                                "Roster image",
                                            myName =
                                                draftName.trim(),
                                            notificationHour =
                                                hour,
                                            notificationMinute =
                                                minute,
                                            days =
                                                draftDays.orEmpty()
                                        )
                                    )

                                    DutyRosterNotification
                                        .createChannel(context)

                                    DutyRosterNotification
                                        .schedule(context)

                                    savedRoster =
                                        DutyRosterStorage
                                            .load(context)

                                    draftDays = null
                                },
                                enabled = draftName.isNotBlank(),
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm & Save Roster")
                            }

                            if (draftName.isBlank()) {
                                Text(
                                    "Select your name before saving.",
                                    style =
                                        MaterialTheme.typography
                                            .labelSmall,
                                    color =
                                        MaterialTheme.colorScheme
                                            .error
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Today",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (today == null) {
                            Text(
                                "Today's date was not found in the saved roster.",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        } else {
                            val mine =
                                today.duties.firstOrNull {
                                    it.person.equals(
                                        savedRoster?.myName,
                                        ignoreCase = true
                                    )
                                }

                            val morning =
                                today.duties
                                    .filter {
                                        DutyRosterParser
                                            .isMorning(it.duty)
                                    }
                                    .map { it.person }
                                    .distinct()

                            val off =
                                today.duties
                                    .filter {
                                        DutyRosterParser
                                            .isOff(it.duty)
                                    }
                                    .map { it.person }
                                    .distinct()

                            StatusRow(
                                icon = Icons.Default.AccessTime,
                                label = "My duty",
                                value =
                                    mine?.duty ?: "Not found"
                            )

                            StatusRow(
                                icon = Icons.Default.EventNote,
                                label = "Morning staff",
                                value =
                                    morning.joinToString(", ")
                                        .ifBlank { "None" }
                            )

                            StatusRow(
                                icon = Icons.Default.CheckCircle,
                                label = "OFF today",
                                value =
                                    off.joinToString(", ")
                                        .ifBlank { "None" }
                            )
                        }
                    }
                }
            }

            if (today != null) {
                item {
                    Text(
                        "Today's full roster",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    today.duties,
                    key = {
                        it.person + "-" + it.duty
                    }
                ) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                entry.person,
                                modifier =
                                    Modifier.weight(0.40f),
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                entry.duty,
                                modifier =
                                    Modifier.weight(0.60f),
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    label,
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
                Text(
                    value,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
