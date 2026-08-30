package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Vehicle

@Composable
fun VehicleScreen(
    modifier: Modifier = Modifier,
    vehicles: List<Vehicle> = emptyList(),
    onAddVehicle: () -> Unit = {},
    onVehicleClick: (Vehicle) -> Unit = {},
    onFuelClick: (Vehicle) -> Unit = {},
    onServiceClick: (Vehicle) -> Unit = {},
    onPartsClick: (Vehicle) -> Unit = {},
    onSummaryClick: (Vehicle) -> Unit = {},
    onEditVehicle: (Vehicle) -> Unit = {},
    onDeleteVehicle: (Vehicle) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        VehicleHeader(
            onAddVehicle = onAddVehicle
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (vehicles.isEmpty()) {

                item {
                    EmptyVehicleCard(
                        onAddVehicle = onAddVehicle
                    )
                }

            } else {

                item {
                    Text(
                        text = "আমার গাড়ি",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = vehicles,
                    key = { it.id }
                ) { vehicle ->

                    VehicleCard(
                        vehicle = vehicle,

                        onClick = {
                            onVehicleClick(vehicle)
                        },

                        onFuelClick = {
                            onFuelClick(vehicle)
                        },

                        onServiceClick = {
                            onServiceClick(vehicle)
                        },

                        onPartsClick = {
                            onPartsClick(vehicle)
                        },

                        onSummaryClick = {
                            onSummaryClick(vehicle)
                        },

                        onEditClick = {
                            onEditVehicle(vehicle)
                        },

                        onDeleteClick = {
                            onDeleteVehicle(vehicle)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleHeader(
    onAddVehicle: () -> Unit
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
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Vehicle & Maintenance",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "গাড়ির হিসাব",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            FilledTonalIconButton(
                onClick = onAddVehicle,
                modifier = Modifier.size(48.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "গাড়ি যোগ করুন"
                )
            }
        }
    }
}

@Composable
private fun EmptyVehicleCard(
    onAddVehicle: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "কোনো গাড়ি যোগ করা হয়নি",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onAddVehicle
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("গাড়ি যোগ করুন")
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onFuelClick: () -> Unit,
    onServiceClick: () -> Unit,
    onPartsClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(18.dp),
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
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = vehicle.name.ifBlank {
                            vehicle.type
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = vehicle.model.ifBlank {
                            "মডেল যোগ করা হয়নি"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (vehicle.registrationNumber.isNotBlank()) {

                        Text(
                            text = vehicle.registrationNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                VehicleAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalGasStation,
                    title = "জ্বালানি",
                    color = Color(0xFF1976D2),
                    onClick = onFuelClick
                )

                VehicleAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Build,
                    title = "সার্ভিস",
                    color = Color(0xFF16A05D),
                    onClick = onServiceClick
                )

                VehicleAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    title = "যন্ত্রাংশ",
                    color = Color(0xFFF59E0B),
                    onClick = onPartsClick
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSummaryClick()
                    },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.10f
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "মোট খরচের হিসাব",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "মিটার: ${"%.0f".format(vehicle.currentOdometer)} KM",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("এডিট")
                }

                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("ডিলিট")
                }
            }
        }
    }
}

@Composable
private fun VehicleAction(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(58.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(21.dp)
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}