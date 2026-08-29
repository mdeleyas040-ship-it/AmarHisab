package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.PartEntry
import com.eleyas.expensetracker.model.Vehicle

@Composable
fun PartHistoryScreen(
    vehicle: Vehicle,
    partEntries: List<PartEntry> = emptyList(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddPart: () -> Unit = {},
    onEditPart: (PartEntry) -> Unit = {},
    onDeletePart: (PartEntry) -> Unit = {}
) {
    val totalCost = partEntries.sumOf { it.cost }
    val totalQuantity = partEntries.sumOf { it.quantity }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
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
                        text = "Parts & Repairs",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                FilledTonalButton(
                    onClick = onAddPart,
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        text = "+ Part",
                        fontWeight = FontWeight.Bold
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    PartSummaryCard(
                        title = "Total Cost",
                        value = "৳${"%.0f".format(totalCost)}",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )

                    PartSummaryCard(
                        title = "Quantity",
                        value = "%.0f".format(totalQuantity),
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )

                    PartSummaryCard(
                        title = "Records",
                        value = partEntries.size.toString(),
                        color = Color(0xFF16A05D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (partEntries.isEmpty()) {

                item {
                    EmptyPartHistory(
                        onAddPart = onAddPart
                    )
                }

            } else {

                item {
                    Text(
                        text = "Parts Records",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = partEntries,
                    key = { it.id }
                ) { entry ->

                    PartHistoryCard(
                        entry = entry,
                        onEdit = {
                            onEditPart(entry)
                        },
                        onDelete = {
                            onDeletePart(entry)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PartSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
private fun PartHistoryCard(
    entry: PartEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.10f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B)
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
                        text = entry.partName.ifBlank {
                            "Part"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = entry.date.ifBlank {
                            "Unknown date"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (entry.category.isNotBlank()) {
                        Text(
                            text = entry.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "৳${"%.0f".format(entry.cost)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF59E0B)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                PartInfo(
                    label = "Quantity",
                    value = "%.0f".format(entry.quantity)
                )

                PartInfo(
                    label = "Odometer",
                    value = "${"%.0f".format(entry.odometer)} KM"
                )

                PartInfo(
                    label = "Next Change",
                    value =
                        if (entry.nextChangeOdometer > 0)
                            "${"%.0f".format(entry.nextChangeOdometer)} KM"
                        else
                            "Not set"
                )
            }

            if (entry.workshop.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Text(
                    text = "Shop: ${entry.workshop}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entry.notes.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text = entry.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text("Edit")
                }

                TextButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun PartInfo(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyPartHistory(
    onAddPart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🛠️",
                fontSize = 42.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "কোনো Part Record নেই",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "এই গাড়ির প্রথম part/repair entry যোগ করুন।",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onAddPart,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Add Part")
            }
        }
    }
}