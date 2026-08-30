package com.eleyas.expensetracker.ui.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Vehicle

@Composable
fun EditVehicleScreen(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (Vehicle) -> Unit = {}
) {
    var name by remember { mutableStateOf(vehicle.name) }
    var model by remember { mutableStateOf(vehicle.model) }
    var registration by remember {
        mutableStateOf(vehicle.registrationNumber)
    }

    var odometer by remember {
        mutableStateOf(
            if (vehicle.currentOdometer == 0.0)
                ""
            else
                vehicle.currentOdometer.toInt().toString()
        )
    }

    var type by remember { mutableStateOf(vehicle.type) }
    var usage by remember { mutableStateOf(vehicle.usageType) }

    var nameError by remember { mutableStateOf(false) }
    var odometerError by remember { mutableStateOf(false) }

    val types = listOf("Bike", "Car", "Van", "Covered Van")
    val usages = listOf("Personal", "Business")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পেছনে"
                    )
                }

                Text(
                    text = "গাড়ির তথ্য পরিবর্তন",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("গাড়ির নাম *") },
                singleLine = true,
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("গাড়ির নাম দিন")
                    }
                }
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("মডেল") },
                singleLine = true
            )

            OutlinedTextField(
                value = registration,
                onValueChange = { registration = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("রেজিস্ট্রেশন নম্বর") },
                singleLine = true
            )

            OutlinedTextField(
                value = odometer,
                onValueChange = {
                    if (
                        it.isEmpty() ||
                        it.all { char -> char.isDigit() }
                    ) {
                        odometer = it
                        odometerError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("বর্তমান মিটার") },
                suffix = { Text("KM") },
                singleLine = true,
                isError = odometerError,
                supportingText = {
                    if (odometerError) {
                        Text("মিটারের সঠিক সংখ্যা দিন")
                    }
                }
            )

            Text(
                text = "গাড়ির ধরন",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { item ->
                    FilterChip(
                        selected = type == item,
                        onClick = { type = item },
                        label = { Text(item) }
                    )
                }
            }

            Text(
                text = "ব্যবহারের ধরন",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                usages.forEach { item ->
                    FilterChip(
                        selected = usage == item,
                        onClick = { usage = item },
                        label = { Text(item) }
                    )
                }
            }

            Button(
                onClick = {

                    val validName = name.isNotBlank()

                    val validOdometer =
                        odometer.isBlank() ||
                                odometer.toDoubleOrNull() != null

                    nameError = !validName
                    odometerError = !validOdometer

                    if (!validName || !validOdometer) {
                        return@Button
                    }

                    onSave(
                        vehicle.copy(
                            name = name.trim(),
                            model = model.trim(),
                            registrationNumber = registration.trim(),
                            type = type,
                            usageType = usage,
                            currentOdometer =
                                odometer.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "পরিবর্তন সংরক্ষণ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}