package com.eleyas.expensetracker.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.WishlistItem
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun WishlistScreen(
    items: List<WishlistItem>,
    balance: Double,
    onBack: () -> Unit,
    onAdd: (String, Double) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান")
                }
                Text("পরে কিনব", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "উইশলিস্টে যোগ করুন")
                }
            }
            Text(
                "পছন্দের জিনিস সংরক্ষণ করুন, সামর্থ্য হলে আমরা জানাব।",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("বর্তমান ব্যালেন্স", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "৳${formatMoney(balance.coerceAtLeast(0.0))}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (items.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("আপনার উইশলিস্ট খালি", fontWeight = FontWeight.Bold)
                    Text("পছন্দের কিছু পরে কেনার জন্য যোগ করুন")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.id }) { item ->
                        WishlistItemCard(item, balance, onDelete)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWishlistItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, price ->
                onAdd(name, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WishlistItemCard(item: WishlistItem, balance: Double, onDelete: (Long) -> Unit) {
    val affordable = balance >= item.price
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (affordable) Color(0xFFDCF8E5) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, null, tint = if (affordable) Color(0xFF16803C) else MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("দাম: ৳${formatMoney(item.price)}")
                Text(
                    if (affordable) "এখন কিনতে পারবেন 🎉" else "আরও ৳${formatMoney(item.price - balance.coerceAtLeast(0.0))} লাগবে",
                    color = if (affordable) Color(0xFF16803C) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Default.Delete, "মুছুন")
            }
        }
    }
}

@Composable
private fun AddWishlistItemDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("উইশলিস্টে যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("জিনিসের নাম") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { character -> character.isDigit() || character == '.' } },
                    label = { Text("দাম (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), price.toDouble()) },
                enabled = name.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
