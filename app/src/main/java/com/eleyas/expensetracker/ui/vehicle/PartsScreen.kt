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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.PartEntry
import com.eleyas.expensetracker.model.Vehicle
import com.eleyas.expensetracker.repository.PartRepository
import com.eleyas.expensetracker.ui.screens.AddPartScreen

@Composable
fun PartsScreen(
    userId: String,
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val repository = remember {
        PartRepository()
    }

    var parts by remember {
        mutableStateOf<List<PartEntry>>(emptyList())
    }

    var showAddPart by remember {
        mutableStateOf(false)
    }

    DisposableEffect(userId, vehicle.id) {

        if (userId.isBlank() || vehicle.id.isBlank()) {
            onDispose { }
        } else {

            val listener = repository.observeParts(
                userId = userId,
                vehicleId = vehicle.id,

                onData = {
                    parts = it.reversed()
                },

                onError = {
                    Toast.makeText(
                        context,
                        "যন্ত্রাংশের তথ্য লোড করা যায়নি",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

            onDispose {
                listener.remove()
            }
        }
    }

    if (showAddPart) {

        AddPartScreen(
            vehicle = vehicle,
            modifier = modifier.fillMaxSize(),

            onBack = {
                showAddPart = false
            },

            onSave = { entry ->

                repository.addPart(
                    userId = userId,
                    entry = entry,

                    onSuccess = {
                        showAddPart = false

                        Toast.makeText(
                            context,
                            "যন্ত্রাংশ সফলভাবে যোগ হয়েছে",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onError = {
                        Toast.makeText(
                            context,
                            "যন্ত্রাংশ যোগ করা যায়নি",
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "যন্ত্রাংশ",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        showAddPart = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "যন্ত্রাংশ যোগ করুন"
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (parts.isEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
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
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "এখনো কোনো যন্ত্রাংশের তথ্য নেই",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Button(
                                onClick = {
                                    showAddPart = true
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
                                    text = "যন্ত্রাংশ যোগ করুন"
                                )
                            }
                        }
                    }
                }

            } else {

                item {

                    Text(
                        text = "যন্ত্রাংশের ইতিহাস",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = parts,
                    key = { it.id }
                ) { part ->

                    PartHistoryCard(part)
                }
            }
        }
    }
}

@Composable
private fun PartHistoryCard(
    part: PartEntry
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.10f
                    )
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
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
                        text = part.partName.ifBlank {
                            "যন্ত্রাংশ"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (part.category.isNotBlank()) {

                        Text(
                            text = part.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (part.date.isNotBlank()) {

                        Text(
                            text = part.date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "৳${formatNumber(part.cost)}",
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

            InfoRow(
                title = "পরিমাণ",
                value = formatNumber(part.quantity)
            )

            if (part.workshop.isNotBlank()) {
                InfoRow(
                    title = "দোকান / ওয়ার্কশপ",
                    value = part.workshop
                )
            }

            if (part.odometer > 0) {
                InfoRow(
                    title = "মিটার",
                    value = "${formatNumber(part.odometer)} KM"
                )
            }

            if (part.nextChangeOdometer > 0) {
                InfoRow(
                    title = "পরবর্তী পরিবর্তন",
                    value =
                        "${formatNumber(part.nextChangeOdometer)} KM"
                )
            }

            if (part.notes.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "নোট",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = part.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
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
            modifier = Modifier.width(125.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatNumber(
    value: Double
): String {

    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }
}