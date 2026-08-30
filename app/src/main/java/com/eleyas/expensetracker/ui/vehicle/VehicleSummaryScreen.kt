// VehicleSummaryScreen.kt
package com.eleyas.expensetracker.ui.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.FuelRepository
import com.eleyas.expensetracker.repository.ServiceRepository
import com.eleyas.expensetracker.repository.PartRepository

@Composable
fun VehicleSummaryScreen(
    userId: String,
    vehicle: Vehicle,
    onBack: () -> Unit = {}
) {
    val fuelRepository = remember { FuelRepository() }
    val serviceRepository = remember { ServiceRepository() }
    val partRepository = remember { PartRepository() }

    var fuelTotal by remember { mutableStateOf(0.0) }
    var serviceTotal by remember { mutableStateOf(0.0) }
    var partsTotal by remember { mutableStateOf(0.0) }

    DisposableEffect(userId, vehicle.id) {

        val fuelListener = fuelRepository.observeFuel(
            userId = userId,
            vehicleId = vehicle.id,
            onData = { entries ->
                fuelTotal = entries.sumOf { it.totalCost }
            }
        )

        val serviceListener = serviceRepository.observeServices(
            userId = userId,
            vehicleId = vehicle.id,
            onData = { entries ->
                serviceTotal = entries.sumOf { it.cost }
            }
        )

        val partsListener = partRepository.observeParts(
            userId = userId,
            vehicleId = vehicle.id,
            onData = { entries ->
                partsTotal = entries.sumOf { it.cost }
            }
        )

        onDispose {
            fuelListener.remove()
            serviceListener.remove()
            partsListener.remove()
        }
    }

    val grandTotal =
        fuelTotal + serviceTotal + partsTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← পেছনে")
        }

        Text(
            text = "গাড়ির মোট হিসাব",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = vehicle.name.ifBlank {
                vehicle.type
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        SummaryCard(
            title = "জ্বালানি",
            amount = fuelTotal
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        SummaryCard(
            title = "সার্ভিসিং",
            amount = serviceTotal
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        SummaryCard(
            title = "যন্ত্রাংশ",
            amount = partsTotal
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "সর্বমোট খরচ",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "৳${formatAmount(grandTotal)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "৳${formatAmount(amount)}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatAmount(
    amount: Double
): String {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        String.format("%.2f", amount)
    }
}