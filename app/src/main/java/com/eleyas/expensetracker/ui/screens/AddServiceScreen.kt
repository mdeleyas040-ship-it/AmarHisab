package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.ServiceEntry
import com.eleyas.expensetracker.model.Vehicle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddServiceScreen(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (ServiceEntry) -> Unit = {}
) {
    val today = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    var date by remember { mutableStateOf(today) }
    var serviceType by remember { mutableStateOf("") }
    var odometer by remember {
        mutableStateOf(
            if (vehicle.currentOdometer > 0)
                vehicle.currentOdometer.toInt().toString()
            else ""
        )
    }
    var cost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var partsChanged by remember { mutableStateOf("") }
    var nextServiceDate by remember { mutableStateOf("") }
    var nextServiceOdometer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val serviceTypes = listOf(
        "Regular Service",
        "Oil Change",
        "Engine",
        "Brake",
        "Tire",
        "Electrical",
        "Other"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = vehicle.name.ifBlank {
                            vehicle.type
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Service Entry",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF16A05D).copy(alpha = 0.10f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF16A05D)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Service Type
            Text(
                text = "Service Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                serviceTypes.chunked(3).forEach { rowItems ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        rowItems.forEach { type ->

                            ServiceTypeChip(
                                title = type,
                                selected = serviceType == type,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    serviceType = type
                                }
                            )
                        }

                        repeat(3 - rowItems.size) {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Service Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        text = "Service Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            date = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Service Date")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = odometer,
                        onValueChange = {
                            if (
                                it.isEmpty() ||
                                it.all { char -> char.isDigit() }
                            ) {
                                odometer = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Odometer")
                        },
                        placeholder = {
                            Text("যেমন: 15000")
                        },
                        suffix = {
                            Text("KM")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = cost,
                        onValueChange = {
                            if (
                                it.isEmpty() ||
                                it.matches(
                                    Regex("^\\d*\\.?\\d*$")
                                )
                            ) {
                                cost = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Service Cost")
                        },
                        placeholder = {
                            Text("যেমন: 2500")
                        },
                        suffix = {
                            Text("৳")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = workshop,
                        onValueChange = {
                            workshop = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Workshop / Service Center")
                        },
                        placeholder = {
                            Text("যেমন: ABC Auto Service")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = partsChanged,
                        onValueChange = {
                            partsChanged = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Parts Changed")
                        },
                        placeholder = {
                            Text("যেমন: Engine Oil, Filter")
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Next Service
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        text = "Next Service",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "পরবর্তী servicing-এর সময় মনে রাখার জন্য তথ্য দিন।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nextServiceDate,
                        onValueChange = {
                            nextServiceDate = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Next Service Date")
                        },
                        placeholder = {
                            Text("যেমন: 30/09/2026")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = nextServiceOdometer,
                        onValueChange = {
                            if (
                                it.isEmpty() ||
                                it.all { char -> char.isDigit() }
                            ) {
                                nextServiceOdometer = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Next Service Odometer")
                        },
                        placeholder = {
                            Text("যেমন: 20000")
                        },
                        suffix = {
                            Text("KM")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                label = {
                    Text("Notes")
                },
                placeholder = {
                    Text("অতিরিক্ত কোনো তথ্য...")
                },
                maxLines = 4,
                shape = RoundedCornerShape(14.dp)
            )

            // Save
            Button(
                onClick = {

                    val entry = ServiceEntry(
                        vehicleId = vehicle.id,
                        date = date.trim(),
                        serviceType = serviceType.trim(),
                        odometer =
                            odometer.toDoubleOrNull() ?: 0.0,
                        cost =
                            cost.toDoubleOrNull() ?: 0.0,
                        workshop = workshop.trim(),
                        partsChanged = partsChanged.trim(),
                        nextServiceDate =
                            nextServiceDate.trim(),
                        nextServiceOdometer =
                            nextServiceOdometer
                                .toDoubleOrNull() ?: 0.0,
                        notes = notes.trim()
                    )

                    onSave(entry)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled =
                    serviceType.isNotBlank() &&
                            cost.toDoubleOrNull() != null,
                shape = RoundedCornerShape(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Save Service",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}

@Composable
private fun ServiceTypeChip(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(13.dp),
        color =
            if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface,
        tonalElevation =
            if (selected) 2.dp else 0.dp,
        onClick = onClick
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color =
                    if (selected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
            )
        }
    }
}