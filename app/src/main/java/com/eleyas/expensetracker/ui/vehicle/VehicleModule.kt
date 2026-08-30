package com.eleyas.expensetracker.ui.vehicle

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.VehicleRepository
import com.eleyas.expensetracker.ui.screens.AddVehicleScreen

@Composable
fun VehicleModule(
    userId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val repository = remember {
        VehicleRepository()
    }

    var vehicles by remember {
        mutableStateOf<List<Vehicle>>(emptyList())
    }

    var selectedVehicle by remember {
        mutableStateOf<Vehicle?>(null)
    }

    var showAddVehicle by remember {
        mutableStateOf(false)
    }

    var showEditVehicle by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showFuelScreen by remember {
        mutableStateOf(false)
    }

    var showServiceScreen by remember {
        mutableStateOf(false)
    }

    var showPartsScreen by remember {
        mutableStateOf(false)
    }

    var showSummaryScreen by remember {
        mutableStateOf(false)
    }

    DisposableEffect(userId) {
        if (userId.isBlank()) {
            onDispose { }
        } else {
            val listener = repository.observeVehicles(
                userId = userId,
                onData = {
                    vehicles = it
                },
                onError = {
                    Toast.makeText(
                        context,
                        "গাড়ির তথ্য লোড করা যায়নি",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    // EDIT
    if (showEditVehicle && selectedVehicle != null) {
        EditVehicleScreen(
            vehicle = selectedVehicle!!,
            modifier = modifier.fillMaxSize(),

            onBack = {
                showEditVehicle = false
                selectedVehicle = null
            },

            onSave = { updatedVehicle ->
                repository.updateVehicle(
                    userId = userId,
                    vehicle = updatedVehicle,

                    onSuccess = {
                        showEditVehicle = false
                        selectedVehicle = null

                        Toast.makeText(
                            context,
                            "গাড়ির তথ্য পরিবর্তন হয়েছে",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onError = {
                        Toast.makeText(
                            context,
                            "গাড়ির তথ্য পরিবর্তন করা যায়নি",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )

        return
    }

    // DELETE
    if (showDeleteDialog && selectedVehicle != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedVehicle = null
            },

            title = {
                Text("গাড়ি ডিলিট করবেন?")
            },

            text = {
                Text("এই গাড়ির তথ্য ডিলিট হয়ে যাবে। আপনি কি নিশ্চিত?")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val vehicle = selectedVehicle!!

                        repository.deleteVehicle(
                            userId = userId,
                            vehicleId = vehicle.id,

                            onSuccess = {
                                showDeleteDialog = false
                                selectedVehicle = null

                                Toast.makeText(
                                    context,
                                    "গাড়ি ডিলিট হয়েছে",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onError = {
                                Toast.makeText(
                                    context,
                                    "গাড়ি ডিলিট করা যায়নি",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                ) {
                    Text("ডিলিট")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedVehicle = null
                    }
                ) {
                    Text("বাতিল")
                }
            }
        )

        return
    }

    // SUMMARY
    if (showSummaryScreen && selectedVehicle != null) {
        VehicleSummaryScreen(
            userId = userId,
            vehicle = selectedVehicle!!,

            onBack = {
                showSummaryScreen = false
                selectedVehicle = null
            }
        )

        return
    }

    // FUEL
    if (showFuelScreen && selectedVehicle != null) {
        FuelScreen(
            userId = userId,
            vehicle = selectedVehicle!!,

            onBack = {
                showFuelScreen = false
                selectedVehicle = null
            }
        )

        return
    }

    // SERVICE
    if (showServiceScreen && selectedVehicle != null) {
        ServiceScreen(
            userId = userId,
            vehicle = selectedVehicle!!,
            modifier = modifier.fillMaxSize(),

            onBack = {
                showServiceScreen = false
                selectedVehicle = null
            }
        )

        return
    }

    // PARTS
    if (showPartsScreen && selectedVehicle != null) {
        PartsScreen(
            userId = userId,
            vehicle = selectedVehicle!!,
            modifier = modifier.fillMaxSize(),

            onBack = {
                showPartsScreen = false
                selectedVehicle = null
            }
        )

        return
    }

    // ADD VEHICLE
    if (showAddVehicle) {
        AddVehicleScreen(
            modifier = modifier.fillMaxSize(),

            onBack = {
                showAddVehicle = false
            },

            onSave = { vehicle ->
                repository.addVehicle(
                    userId = userId,
                    vehicle = vehicle,

                    onSuccess = {
                        showAddVehicle = false

                        Toast.makeText(
                            context,
                            "গাড়ি সফলভাবে যোগ হয়েছে",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onError = {
                        Toast.makeText(
                            context,
                            "গাড়ি যোগ করা যায়নি",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )

        return
    }

    // VEHICLE LIST
    VehicleScreen(
        modifier = modifier.fillMaxSize(),
        vehicles = vehicles,

        onAddVehicle = {
            showAddVehicle = true
        },

        onVehicleClick = { vehicle ->
            selectedVehicle = vehicle
        },

        onFuelClick = { vehicle ->
            selectedVehicle = vehicle
            showFuelScreen = true
        },

        onServiceClick = { vehicle ->
            selectedVehicle = vehicle
            showServiceScreen = true
        },

        onPartsClick = { vehicle ->
            selectedVehicle = vehicle
            showPartsScreen = true
        },

        onSummaryClick = { vehicle ->
            selectedVehicle = vehicle
            showSummaryScreen = true
        },

        onEditVehicle = { vehicle ->
            selectedVehicle = vehicle
            showEditVehicle = true
        },

        onDeleteVehicle = { vehicle ->
            selectedVehicle = vehicle
            showDeleteDialog = true
        }
    )
}