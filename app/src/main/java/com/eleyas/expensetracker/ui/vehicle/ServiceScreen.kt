package com.eleyas.expensetracker.ui.vehicle

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.ServiceEntry
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.ServiceRepository
import com.eleyas.expensetracker.ui.screens.AddServiceScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun ServiceScreen(
    userId: String,
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val repository = remember {
        ServiceRepository()
    }

    var services by remember {
        mutableStateOf<List<ServiceEntry>>(emptyList())
    }

    var showAddService by remember {
        mutableStateOf(false)
    }

    DisposableEffect(
        userId,
        vehicle.id
    ) {
        if (
            userId.isBlank() ||
            vehicle.id.isBlank()
        ) {
            onDispose { }
        } else {

            val listener = repository.observeServices(
                userId = userId,
                vehicleId = vehicle.id,

                onData = { data ->
                    services = data.reversed()
                },

                onError = { error ->
                    Toast.makeText(
                        context,
                        "সার্ভিসের তথ্য লোড করা যায়নি",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    if (showAddService) {

        AddServiceScreen(
            vehicle = vehicle,
            modifier = modifier.fillMaxSize(),

            onBack = {
                showAddService = false
            },

            onSave = { entry ->

                repository.addService(
                    userId = userId,
                    entry = entry,

                    onSuccess = {
                        showAddService = false

                        Toast.makeText(
                            context,
                            "সার্ভিসের তথ্য সফলভাবে যোগ হয়েছে",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onError = {
                        Toast.makeText(
                            context,
                            "সার্ভিসের তথ্য যোগ করা যায়নি",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )

        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
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

                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "পেছনে"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = vehicle.name.ifBlank {
                            vehicle.type
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "সার্ভিসিং",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        showAddService = true
                    },
                    modifier = Modifier.size(48.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "সার্ভিস যোগ করুন"
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (services.isEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint =
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "এখনো কোনো সার্ভিসের তথ্য নেই",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "গাড়ির সার্ভিসিংয়ের তথ্য এখানে সংরক্ষণ করুন।",
                                fontSize = 13.sp,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )

                            Button(
                                onClick = {
                                    showAddService = true
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    text = "সার্ভিস যোগ করুন"
                                )
                            }
                        }
                    }

                }

            } else {

                item {

                    Text(
                        text = "সার্ভিসের ইতিহাস",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = services,
                    key = { it.id }
                ) { service ->

                    ServiceHistoryCard(
                        service = service
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceHistoryCard(
    service: ServiceEntry
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    color =
                        MaterialTheme.colorScheme.primary
                            .copy(alpha = 0.10f)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme.primary
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
                        text = service.serviceType.ifBlank {
                            "সার্ভিস"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (service.date.isNotBlank()) {

                        Text(
                            text = service.date,
                            fontSize = 12.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "৳${formatAmount(service.cost)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (service.odometer > 0) {

                ServiceInfoRow(
                    title = "মিটার",
                    value =
                        "${formatAmount(service.odometer)} KM"
                )
            }

            if (service.workshop.isNotBlank()) {

                ServiceInfoRow(
                    title = "ওয়ার্কশপ",
                    value = service.workshop
                )
            }

            if (service.partsChanged.isNotBlank()) {

                ServiceInfoRow(
                    title = "পরিবর্তিত যন্ত্রাংশ",
                    value = service.partsChanged
                )
            }

            if (
                service.nextServiceDate.isNotBlank() ||
                service.nextServiceOdometer > 0
            ) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "পরবর্তী সার্ভিস",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (service.nextServiceDate.isNotBlank()) {

                    Text(
                        text =
                            "তারিখ: ${service.nextServiceDate}",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                if (service.nextServiceOdometer > 0) {

                    Text(
                        text =
                            "মিটার: ${
                                formatAmount(
                                    service.nextServiceOdometer
                                )
                            } KM",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            if (service.notes.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "নোট",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = service.notes,
                    fontSize = 12.sp,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ServiceInfoRow(
    title: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {

        Text(
            text = "$title:",
            modifier = Modifier.width(105.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            fontSize = 12.sp,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

private fun formatAmount(
    value: Double
): String {

    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(
            "%.2f",
            value
        )
    }
}