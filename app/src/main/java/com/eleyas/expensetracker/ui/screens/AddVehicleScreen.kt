package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (Vehicle) -> Unit = {}
) {
    var vehicleName by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var registration by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }

    var selectedType by remember { mutableStateOf("Bike") }
    var selectedUsage by remember { mutableStateOf("Personal") }

    val vehicleTypes = listOf(
        "Bike",
        "Car",
        "Van",
        "Covered Van"
    )

    val usageTypes = listOf(
        "Personal",
        "Business"
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
                        text = "Vehicle",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "গাড়ি যোগ করুন",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
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

            // Vehicle Type
            Text(
                text = "Vehicle Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                vehicleTypes.forEach { type ->

                    VehicleTypeChip(
                        title = type,
                        selected = selectedType == type,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedType = type
                        }
                    )
                }
            }

            // Vehicle Information
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.10f
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Vehicle Information",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "আপনার গাড়ির basic information দিন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = vehicleName,
                        onValueChange = {
                            vehicleName = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Vehicle Name")
                        },
                        placeholder = {
                            Text("যেমন: My Bike")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = model,
                        onValueChange = {
                            model = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Model")
                        },
                        placeholder = {
                            Text("যেমন: Honda CB Shine")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = registration,
                        onValueChange = {
                            registration = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Registration Number")
                        },
                        placeholder = {
                            Text("যেমন: DHAKA METRO...")
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
                            Text("Current Odometer")
                        },
                        placeholder = {
                            Text("যেমন: 12500 KM")
                        },
                        suffix = {
                            Text("KM")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Usage Type
            Text(
                text = "Usage Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                usageTypes.forEach { usage ->

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedUsage = usage
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (selectedUsage == usage)
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.12f
                                    )
                                else
                                    MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    if (usage == "Personal")
                                        "👤"
                                    else
                                        "💼",
                                fontSize = 25.sp
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = usage,
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (selectedUsage == usage)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Save
            Button(
                onClick = {

                    val vehicle = Vehicle(
                        name = vehicleName.trim(),
                        model = model.trim(),
                        registrationNumber = registration.trim(),
                        type = selectedType,
                        usageType = selectedUsage,
                        currentOdometer =
                            odometer.toDoubleOrNull() ?: 0.0
                    )

                    onSave(vehicle)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = vehicleName.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Save Vehicle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VehicleTypeChip(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color =
            if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface,
        tonalElevation =
            if (selected) 2.dp else 0.dp
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = title,
                fontSize = 11.sp,
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