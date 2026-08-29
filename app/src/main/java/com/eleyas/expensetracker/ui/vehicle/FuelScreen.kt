package com.eleyas.expensetracker.ui.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.FuelEntry
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.FuelRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(
    userId: String,
    vehicle: Vehicle,
    onBack: () -> Unit
) {
    val repository = remember { FuelRepository() }

    var fuelEntries by remember {
        mutableStateOf<List<FuelEntry>>(emptyList())
    }

    var showAddFuel by remember {
        mutableStateOf(false)
    }

    DisposableEffect(userId, vehicle.id) {
        val listener = repository.observeFuel(
            userId = userId,
            vehicleId = vehicle.id,
            onData = {
                fuelEntries = it
            }
        )

        onDispose {
            listener.remove()
        }
    }

    if (showAddFuel) {
        AddFuelScreen(
            vehicle = vehicle,
            onBack = {
                showAddFuel = false
            },
            onSave = { entry ->
                repository.addFuel(
                    userId = userId,
                    entry = entry,
                    onSuccess = {
                        showAddFuel = false
                    }
                )
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("জ্বালানি")
                        Text(
                            text = vehicle.name.ifBlank { vehicle.type },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "পেছনে"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showAddFuel = true
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "জ্বালানি যোগ করুন"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (fuelEntries.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "এখনও কোনো জ্বালানি হিসাব নেই",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        showAddFuel = true
                    }
                ) {
                    Text("জ্বালানি যোগ করুন")
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fuelEntries) { entry ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = entry.date,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "জ্বালানি: ${entry.liters} লিটার"
                            )

                            Text(
                                text = "প্রতি লিটার: ${entry.pricePerLiter}"
                            )

                            Text(
                                text = "মোট খরচ: ${entry.totalCost}"
                            )

                            if (entry.odometer > 0) {
                                Text(
                                    text =
                                        "ওডোমিটার: ${entry.odometer} কিমি"
                                )
                            }

                            if (entry.fuelStation.isNotBlank()) {
                                Text(
                                    text =
                                        "জ্বালানি স্টেশন: ${entry.fuelStation}"
                                )
                            }

                            if (entry.notes.isNotBlank()) {
                                Text(
                                    text =
                                        "মন্তব্য: ${entry.notes}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFuelScreen(
    vehicle: Vehicle,
    onBack: () -> Unit,
    onSave: (FuelEntry) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var fuelStation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val totalCost =
        (liters.toDoubleOrNull() ?: 0.0) *
                (pricePerLiter.toDoubleOrNull() ?: 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("জ্বালানি যোগ করুন")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "পেছনে"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    text = vehicle.name.ifBlank { vehicle.type },
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("তারিখ") },
                    placeholder = {
                        Text("যেমন: ৩০-০৮-২০২৬")
                    },
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = liters,
                        onValueChange = { liters = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("লিটার") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pricePerLiter,
                        onValueChange = {
                            pricePerLiter = it
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("প্রতি লিটার দাম") },
                        singleLine = true
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "মোট খরচ: %.2f".format(totalCost),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ওডোমিটার (কিমি)") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = fuelStation,
                    onValueChange = { fuelStation = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("জ্বালানি স্টেশন") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("মন্তব্য") },
                    minLines = 3
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled =
                        date.isNotBlank() &&
                                (liters.toDoubleOrNull() ?: 0.0) > 0 &&
                                (pricePerLiter.toDoubleOrNull() ?: 0.0) > 0,
                    onClick = {

                        val entry = FuelEntry(
                            vehicleId = vehicle.id,
                            date = date,
                            liters =
                                liters.toDoubleOrNull() ?: 0.0,
                            pricePerLiter =
                                pricePerLiter.toDoubleOrNull() ?: 0.0,
                            totalCost = totalCost,
                            odometer =
                                odometer.toDoubleOrNull() ?: 0.0,
                            fuelStation = fuelStation,
                            notes = notes
                        )

                        onSave(entry)
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            }
        }
    }
}