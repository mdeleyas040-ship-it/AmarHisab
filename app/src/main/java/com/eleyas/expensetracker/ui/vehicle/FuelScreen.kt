package com.eleyas.expensetracker.ui.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                        Text("Fuel")
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
                            contentDescription = "Back"
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
                            contentDescription = "Add Fuel"
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
                    text = "এখনও কোনো Fuel হিসাব নেই",
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
                    Text("Fuel যোগ করুন")
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fuelEntries) { entry ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        )
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
                                text = "${entry.liters} L × ${entry.pricePerLiter}"
                            )

                            Text(
                                text = "মোট: ${entry.totalCost}"
                            )

                            if (entry.odometer > 0) {
                                Text(
                                    text =
                                        "Odometer: ${entry.odometer} KM"
                                )
                            }

                            if (entry.fuelStation.isNotBlank()) {
                                Text(
                                    text =
                                        "Station: ${entry.fuelStation}"
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
                    Text("Fuel যোগ করুন")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
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
                        label = { Text("প্রতি লিটার") },
                        singleLine = true
                    )
                }
            }

            item {
                Text(
                    text = "মোট খরচ: %.2f".format(totalCost),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Odometer (KM)") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = fuelStation,
                    onValueChange = { fuelStation = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fuel Station") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
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
                    Text("Save Fuel")
                }
            }
        }
    }
}