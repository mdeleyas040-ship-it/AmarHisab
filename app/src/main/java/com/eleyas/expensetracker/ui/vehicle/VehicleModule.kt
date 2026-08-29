package com.eleyas.expensetracker.ui.vehicle

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.VehicleRepository
import com.eleyas.expensetracker.ui.screens.AddVehicleScreen
import com.eleyas.expensetracker.ui.screens.VehicleScreen

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

    var showAddVehicle by remember {
        mutableStateOf(false)
    }

    DisposableEffect(userId) {

        if (userId.isBlank()) {
            onDispose { }
        } else {

            val listener = repository.observeVehicles(
                userId = userId,

                onData = { data ->
                    vehicles = data
                },

                onError = { error ->
                    Toast.makeText(
                        context,
                        "Vehicle data load failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

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

                    onError = { error ->

                        Toast.makeText(
                            context,
                            "গাড়ি যোগ করা যায়নি: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )

    } else {

        VehicleScreen(
            modifier = modifier.fillMaxSize(),

            vehicles = vehicles,

            onAddVehicle = {
                showAddVehicle = true
            },

            onVehicleClick = { vehicle ->
                // Vehicle detail will be connected later.
            },

            onFuelClick = { vehicle ->
                // Fuel module will be connected next.
            },

            onServiceClick = { vehicle ->
                // Service module will be connected next.
            },

            onPartsClick = { vehicle ->
                // Parts module will be connected next.
            }
        )
    }
}