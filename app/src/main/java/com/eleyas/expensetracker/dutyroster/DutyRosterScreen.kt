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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        mutableStateOf(DutyRosterStorage.myName(context))
    }

    val notificationTime =
        remember {
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

            DutyRosterManager.scan(
                context,
                uri,
                onSuccess = {
                    draftDays = it
                    if (draftName.isBlank()) {
                        draftName =
                            it.firstOrNull()
                                ?.duties
                                ?.firstOrNull()
                                ?.person
                                .orEmpty()
                    }
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
        topBar = {
            TopAppBar(
                title = { Text("Duty Roster") },
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
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Upload new roster"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Roster",
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        Text(
                            if (savedRoster != null) {
                                "Saved roster available"
                            } else {
                                "কোনো roster save করা নেই।"
                            },
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                launcher.launch("image/*")
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (scanning) {
                                    "Scanning..."
                                } else {
                                    "Upload Roster Image"
                                }
                            )
                        }

                        error?.let {
                            Text(
                                it,
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (draftDays != null) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Review before saving",
                                style =
                                    MaterialTheme.typography
                                        .titleMedium
                            )

                            Text(
                                "OCR থেকে পাওয়া তথ্য save করার আগে নিজের নাম ও notification time ঠিক করুন।"
                            )

                            OutlinedButton(
                                onClick = {
                                    showNameMenu = true
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "My name: " +
                                            draftName.ifBlank {
                                                "Select name"
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

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = hourText,
                                    onValueChange = {
                                        hourText =
                                            it.filter(
                                                Char::isDigit
                                            ).take(2)
                                    },
                                    label = { Text("Hour") },
                                    modifier =
                                        Modifier.weight(1f)
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
                                    modifier =
                                        Modifier.weight(1f)
                                )
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
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm & Save Roster")
                            }
                        }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Today",
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        if (today == null) {
                            Text(
                                "আজকের date roster-এ পাওয়া যায়নি।"
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

                            Text(
                                "👤 My duty: " +
                                        (mine?.duty ?: "Not found")
                            )

                            Text(
                                "🌅 Morning: " +
                                        morning.joinToString(", ")
                                            .ifBlank { "None" }
                            )

                            Text(
                                "🔴 OFF: " +
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
                            MaterialTheme.typography.titleMedium
                    )
                }

                items(
                    today.duties,
                    key = {
                        it.person + "-" + it.duty
                    }
                ) { entry ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(
                                entry.person,
                                modifier =
                                    Modifier.weight(0.42f)
                            )

                            Text(
                                entry.duty,
                                modifier =
                                    Modifier.weight(0.58f)
                            )
                        }
                    }
                }
            }
        }
    }
}
