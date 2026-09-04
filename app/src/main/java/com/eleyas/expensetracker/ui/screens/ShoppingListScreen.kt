package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.eleyas.expensetracker.model.ShoppingItem
import com.eleyas.expensetracker.ui.components.WarningDialog
import com.eleyas.expensetracker.ui.theme.AccentGreen
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    items: List<ShoppingItem>,
    onBack: () -> Unit,
    onAdd: (name: String, amount: Double, currency: String, category: String, note: String) -> Unit,
    onToggle: (id: Long) -> Unit,
    onRemove: (ShoppingItem) -> Unit,
    onEdit: (ShoppingItem) -> Unit,
    onConvert: (onComplete: (Int) -> Unit) -> Unit,
    onClearAdded: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var itemToRemove by remember { mutableStateOf<ShoppingItem?>(null) }
    var showClearAddedWarning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val totalItems = items.size
    val checkedCount = items.count { it.checked }
    val addedCount = items.count { it.addedToExpense }
    val pendingConvert = items.count { it.checked && !it.addedToExpense }
    val estimatedTotal = items.filter { it.checked }.sumOf { it.amount }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("বাজারের ফর্দ", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                }
            },
            actions = {
                IconButton(onClick = { editingItem = null; showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "আইটেম যোগ করুন")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট আইটেম: $totalItems", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("টিক করা: $checkedCount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("আনুমানিক খরচ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Text("৳${formatMoney(estimatedTotal)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onConvert { count ->
                            if (count > 0) {
                                Toast.makeText(context, "✅ $count টি আইটেম খরচের খাতায় যোগ হয়েছে", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "কোনো নতুন আইটেম টিক করা নেই", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = pendingConvert > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (pendingConvert > 0) "টিক করা $pendingConvert টি আইটেম খরচে যোগ করুন" else "কোনো আইটেম টিক করা নেই",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("আপনার বাজারের ফর্দ খালি", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("উপরের + বোতামে টিপে আইটেম যোগ করুন", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }

            items(items, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { onToggle(item.id) },
                    onEdit = { editingItem = item; showDialog = true },
                    onRemove = { itemToRemove = item }
                )
            }

            if (addedCount > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { showClearAddedWarning = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("কেনা শেষ হওয়া $addedCount টি মুছুন")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ShoppingItemDialog(
            existing = editingItem,
            onDismiss = { showDialog = false; editingItem = null },
            onSave = { name, amount, currency, category, note ->
                if (editingItem != null) {
                    onEdit(editingItem!!.copy(name = name, amount = amount, currency = currency, category = category, note = note))
                } else {
                    onAdd(name, amount, currency, category, note)
                }
                showDialog = false
                editingItem = null
            }
        )
    }

    itemToRemove?.let { item ->
        WarningDialog(
            title = "আইটেম মুছে ফেলবেন?",
            message = "${item.name}\n${currencySymbol(item.currency)}${formatMoney(item.amount)}\n\nএই বাজারের আইটেমটি তালিকা থেকে স্থায়ীভাবে মুছে যাবে।",
            confirmText = "মুছে ফেলুন",
            dismissText = "বাতিল",
            onConfirm = {
                itemToRemove = null
                onRemove(item)
            },
            onDismiss = { itemToRemove = null }
        )
    }

    if (showClearAddedWarning) {
        WarningDialog(
            title = "কেনা শেষ হওয়া আইটেমগুলো মুছবেন?",
            message = "$addedCount টি কেনা শেষ হওয়া আইটেম তালিকা থেকে স্থায়ীভাবে মুছে যাবে।\n\nএই কাজটি করার আগে নিশ্চিত হয়ে নিন।",
            confirmText = "মুছে ফেলুন",
            dismissText = "বাতিল",
            onConfirm = {
                showClearAddedWarning = false
                onClearAdded()
            },
            onDismiss = { showClearAddedWarning = false }
        )
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val colorStrike = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.addedToExpense) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorStrike,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGreen.copy(alpha = 0.12f)
                        ) {
                            Text(item.category, fontSize = 11.sp, color = AccentGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                    if (item.addedToExpense) {
                        Spacer(Modifier.width(6.dp))
                        Text("✓ খরচে গেছে", fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                currencySymbol(item.currency) + formatMoney(item.amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.checked) AccentGreen else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "মুছুন", tint = ExpenseRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private val SHOPPING_CATEGORIES = listOf(
    "অন্যান্য", "মুদি", "সবজি", "ফল", "মাংস", "মাছ",
    "দুগ্ধজাত", "ঔষধ", "পোশাক", "খাবার", "বাসস্থান"
)

private val SHOPPING_CURRENCIES = listOf("BDT", "USD", "MVR")

private fun currencySymbol(currency: String): String = when (currency) {
    "USD" -> "$"
    "MVR" -> "MVR "
    else -> "৳"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemDialog(
    existing: ShoppingItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, currency: String, category: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var amountText by remember { mutableStateOf(if (existing != null && existing.amount > 0) existing.amount.toString() else "") }
    var currency by remember { mutableStateOf(existing?.currency ?: "BDT") }
    var category by remember { mutableStateOf(existing?.category ?: "অন্যান্য") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing != null) "আইটেম এডিট করুন" else "নতুন আইটেম") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম (যেমন: চাল, ডিম)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("আনুমানিক দাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("কারেন্সি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        SHOPPING_CURRENCIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { currency = option; currencyExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        SHOPPING_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { category = option; categoryExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onSave(name, amount, currency, category, note)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text(if (existing != null) "আপডেট" else "যোগ করুন", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
