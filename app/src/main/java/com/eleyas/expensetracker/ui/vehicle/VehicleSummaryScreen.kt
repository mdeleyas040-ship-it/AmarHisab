package com.eleyas.expensetracker.ui.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "পেছনে"
                    )
                }

                Text(
                    text = "গাড়ির মোট হিসাব",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = vehicle.name.ifBlank {
                    vehicle.type
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            ExpenseCard(
                title = "জ্বালানি",
                amount = fuelTotal,
                icon = Icons.Default.LocalGasStation
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            ExpenseCard(
                title = "সার্ভিসিং",
                amount = serviceTotal,
                icon = Icons.Default.Build
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            ExpenseCard(
                title = "যন্ত্রাংশ",
                amount = partsTotal,
                icon = Icons.Default.Settings
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {

                    Text(
                        text = "সর্বমোট খরচ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "৳${formatAmount(grandTotal)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "৳${formatAmount(amount)}",
                fontSize = 17.sp,
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