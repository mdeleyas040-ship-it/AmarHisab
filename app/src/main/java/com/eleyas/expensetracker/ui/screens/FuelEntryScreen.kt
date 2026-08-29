package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.FuelEntry
import com.eleyas.expensetracker.model.Vehicle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FuelEntryScreen(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (FuelEntry) -> Unit = {}
) {
    val today = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date())
    }

    var date by remember { mutableStateOf(today) }
    var liters by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }
    var odometer by remember {
        mutableStateOf(
            if (vehicle.currentOdometer > 0)
                vehicle.currentOdometer.toInt().toString()
            else ""
        )
    }
    var fuelStation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val litersValue = liters.toDoubleOrNull() ?: 0.0
    val priceValue = pricePerLiter.toDoubleOrNull() ?: 0.0
    val totalCost = litersValue * priceValue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

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
                        text = "Fuel Entry",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1976D2).copy(alpha = 0.10f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color(0xFF1976D2)
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
                        text = "Fuel Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = liters,
                        onValueChange = {
                            if (
                                it.isEmpty() ||
                                it.matches(Regex("^\\d*\\.?\\d*$"))
                            ) {
                                liters = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fuel Amount") },
                        placeholder = { Text("যেমন: 25.5") },
                        suffix = { Text("L") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = pricePerLiter,
                        onValueChange = {
                            if (
                                it.isEmpty() ||
                                it.matches(Regex("^\\d*\\.?\\d*$"))
                            ) {
                                pricePerLiter = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Price per Liter") },
                        placeholder = { Text("যেমন: 120") },
                        suffix = { Text("৳/L") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Auto calculated total
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1976D2).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Total Fuel Cost",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "৳${"%.2f".format(totalCost)}",
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1976D2)
                                )
                            }

                            Text(
                                text = "${"%.2f".format(litersValue)} L",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

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
                        text = "Additional Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                        label = { Text("Odometer") },
                        placeholder = { Text("যেমন: 12500") },
                        suffix = { Text("KM") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = fuelStation,
                        onValueChange = {
                            fuelStation = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fuel Station") },
                        placeholder = { Text("যেমন: Shell / Local Station") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text("Notes") },
                        placeholder = { Text("কোনো অতিরিক্ত তথ্য...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val entry = FuelEntry(
                        vehicleId = vehicle.id,
                        date = date.trim(),
                        liters = litersValue,
                        pricePerLiter = priceValue,
                        totalCost = totalCost,
                        odometer =
                            odometer.toDoubleOrNull() ?: 0.0,
                        fuelStation = fuelStation.trim(),
                        notes = notes.trim()
                    )

                    onSave(entry)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = litersValue > 0 && priceValue > 0,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Save Fuel Entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}