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

    var selectedVehicle by remember {
        mutableStateOf<Vehicle?>(null)
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

                onError = { error ->

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

    // =========================
    // মোট হিসাব
    // =========================

    if (
        showSummaryScreen &&
        selectedVehicle != null
    ) {

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

    // =========================
    // জ্বালানি
    // =========================

    if (
        showFuelScreen &&
        selectedVehicle != null
    ) {

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

    // =========================
    // সার্ভিসিং
    // =========================

    if (
        showServiceScreen &&
        selectedVehicle != null
    ) {

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

    // =========================
    // যন্ত্রাংশ
    // =========================

    if (
        showPartsScreen &&
        selectedVehicle != null
    ) {

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

    // =========================
    // গাড়ি যোগ
    // =========================

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

    } else {

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
            }
        )
    }
}