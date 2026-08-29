package com.eleyas.expensetracker.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun defaultCategoryOptions(type: String): List<String> = when (type) {
    "income" -> listOf("Salary", "Freelance", "Business", "Tips", "Bonus", "Other Income")
    "expense" -> listOf("Food", "Transport", "Mobile", "Shopping", "Bills", "Health", "Travel", "Education", "Other Expense")
    "home_expense" -> listOf("বাজার", "বিদ্যুৎ বিল", "চিকিৎসা", "বাড়ি মেরামত", "পড়াশোনা", "পরিবার", "ভ্রমণ", "অন্যান্য")
    else -> listOf("Family", "Home", "Education", "Emergency", "Other")
}

private fun mergeCustomCategories(type: String, customCategories: List<String>): List<String> {
    val defaults = defaultCategoryOptions(type)
    return (defaults + customCategories.filter { it.isNotBlank() && it !in defaults }).distinct()
}

private fun normalizeCategoryName(value: String): String = value.trim().replace(Regex("\\s+"), " ")

private fun createReceiptImageUri(context: android.content.Context): Uri {
    val dir = java.io.File(context.cacheDir, "receipts").apply { mkdirs() }
    val file = java.io.File(dir, "receipt_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun CategoryBudgetDialog(
    existingBudgets: List<CategoryBudget>,
    customCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (CategoryBudget) -> Unit,
    onCategoryAdded: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    var category by remember { mutableStateOf("Food") }
    var limit by remember { mutableStateOf("") }
    var categoryMenu by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }

    val categories = mergeCustomCategories("expense", customCategories)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Monthly Budget", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("মাস: $currentMonth", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(category) }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    categoryMenu = false
                                    val oldBudget = existingBudgets.firstOrNull { it.month == currentMonth && it.category == item }
                                    limit = oldBudget?.limit?.let(::formatMoney) ?: ""
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("➕ নতুন ক্যাটাগরি") },
                            onClick = {
                                categoryMenu = false
                                customCategoryName = ""
                                showCustomCategoryDialog = true
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Monthly Limit") },
                    placeholder = { Text("যেমন: 10000") },
                    leadingIcon = { Text("৳") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = limit.replace(",", "").trim().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onSave(CategoryBudget(month = currentMonth, category = category, limit = amount))
                } else {
                    Toast.makeText(context, "সঠিক Budget Limit দিন।", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Save Budget") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )

    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCategoryDialog = false },
            title = { Text("নতুন ক্যাটাগরি") },
            text = {
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    label = { Text("ক্যাটাগরির নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val candidate = normalizeCategoryName(customCategoryName)
                    if (candidate.isBlank()) {
                        Toast.makeText(context, "ক্যাটাগরির নাম লিখুন।", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (candidate in categories) {
                        category = candidate
                        showCustomCategoryDialog = false
                        return@Button
                    }
                    onCategoryAdded(candidate)
                    category = candidate
                    showCustomCategoryDialog = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showCustomCategoryDialog = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
fun BorrowingDialog(
    existingBorrowing: LoanBorrowing,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var amount by remember { mutableStateOf(formatMoney(existingBorrowing.amount)) }
    var date by remember { mutableStateOf(existingBorrowing.date) }
    var note by remember { mutableStateOf(existingBorrowing.note) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = scheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("নেওয়া ঋণের এন্ট্রি এডিট", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("টাকার পরিমাণ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {
                    val calendar = Calendar.getInstance()
                    try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.let { calendar.time = it } } catch (_: Exception) {}
                    DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(displayLoanDate(date)) 
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("নোট (ঐচ্ছিক)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(onClick = { val parsedAmount = amount.replace(",", "").trim().toDoubleOrNull(); if (parsedAmount == null || parsedAmount <= 0.0) Toast.makeText(context, "সঠিক টাকার পরিমাণ দিন।", Toast.LENGTH_SHORT).show() else onSave(parsedAmount, date, note) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("Save") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, display) ->
                DropdownMenuItem(text = { Text(display) }, onClick = { onSelected(key); expanded = false })
            }
        }
    }
}

@Composable
fun DatePresetButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier.height(42.dp), contentPadding = PaddingValues(horizontal = 5.dp), shape = RoundedCornerShape(12.dp)) { Text(text, fontSize = 11.sp) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.height(42.dp), contentPadding = PaddingValues(horizontal = 5.dp), shape = RoundedCornerShape(12.dp)) { Text(text, fontSize = 11.sp) }
    }
}

@Composable
fun DateFilterField(modifier: Modifier, label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (value.isBlank()) "📅 Select date" else value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AddTransactionDialog(
    type: String,
    existingTransaction: Transaction? = null,
    loans: List<LoanAccount> = emptyList(),
    loanPayments: List<LoanPayment> = emptyList(),
    wallets: List<Wallet> = emptyList(),
    customCategories: List<String> = emptyList(),
    onCategoryAdded: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (Double, String, String, String, String, String, String?) -> Unit,
    onLoanPayment: (Long, Double, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var amount by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.amount?.let(::formatMoney) ?: "") }
    var selectedLoanId by remember(existingTransaction?.id) { mutableStateOf<Long?>(null) }
    var loanExpanded by remember { mutableStateOf(false) }
    var currency by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.currency ?: "BDT") }
    var category by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.category ?: when (type) { "income" -> "Salary"; "expense" -> "Food"; else -> "Family" }) }
    var selectedWalletId by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.walletId ?: wallets.firstOrNull()?.id ?: "default_cash") }
    var walletExpanded by remember { mutableStateOf(false) }
    var loanPaymentAmount by remember { mutableStateOf("") }
    var reason by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.reason ?: "") }
    var date by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.date ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var receiptImage by remember(existingTransaction?.id) { mutableStateOf(existingTransaction?.receiptImage) }

    val scope = rememberCoroutineScope()
    val receiptPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            receiptImage = it.toString()
            if (amount.isBlank()) {
                scope.launch {
                    val result = ReceiptScanner.scanFromUri(context, it)
                    if (result != null) {
                        amount = formatMoney(result.amount)
                        date = result.date
                        if (reason.isBlank()) reason = "Receipt scan"
                        Toast.makeText(context, "✅ বিল স্ক্যান করা হয়েছে: ৳${formatMoney(result.amount)}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "বিল থেকে টাকার পরিমাণ বের করা যায়নি।", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            val uri = cameraImageUri.value ?: return@rememberLauncherForActivityResult
            receiptImage = uri.toString()
            scope.launch {
                val result = ReceiptScanner.scanFromUri(context, uri)
                if (result != null) {
                    amount = formatMoney(result.amount)
                    date = result.date
                    if (reason.isBlank()) reason = "Receipt scan"
                    Toast.makeText(context, "✅ ক্যামেরা থেকে বিল স্ক্যান করা হয়েছে: ৳${formatMoney(result.amount)}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "বিলের তথ্য পড়া যায়নি — আলো ভালো করে ছবি তুলুন।", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createReceiptImageUri(context)
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Scan করতে Camera permission দরকার।", Toast.LENGTH_SHORT).show()
        }
    }
    fun launchCameraScan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = createReceiptImageUri(context)
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    var currencyMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }

    val categories = mergeCustomCategories(type, customCategories)

    val accent = when (type) { "income" -> Color(0xFF22C55E); "expense" -> Color(0xFFEF4444); "home_expense" -> Color(0xFFF59E0B); else -> Color(0xFF3B82F6) }
    val icon = when (type) { "income" -> Icons.Default.AddCard; "expense" -> Icons.Default.Payments; "home_expense" -> Icons.Default.HomeWork; else -> Icons.Default.Home }
    val title = when { existingTransaction != null -> "হিসাব এডিট করুন"; type == "income" -> "আয় যোগ করুন"; type == "expense" -> "খরচ যোগ করুন"; type == "home_expense" -> "বাড়ির খরচ যোগ করুন"; else -> "বাড়িতে টাকা পাঠান" }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = scheme.surface), elevation = CardDefaults.cardElevation(12.dp)) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.16f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp)) } }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = scheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(2.dp))
                            Text(when (type) { "income" -> "আপনার আয়ের তথ্য দিন"; "expense" -> "খরচের তথ্য দিন"; "home_expense" -> "বাড়িতে পাঠানো টাকা থেকে খরচের তথ্য দিন"; else -> "বাড়িতে পাঠানো টাকার তথ্য দিন" }, color = scheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Amount", color = scheme.onSurfaceVariant) }, placeholder = { Text("যেমন: 100", color = scheme.onSurfaceVariant) }, leadingIcon = { Text("৳", color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                    Spacer(Modifier.height(11.dp))
                    
                    // Wallet Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { walletExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(19.dp), tint = accent)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) { Text("অ্যাকাউন্ট / ওয়ালেট", color = scheme.onSurfaceVariant, fontSize = 11.sp); Text(wallets.firstOrNull { it.id == selectedWalletId }?.name ?: "অ্যাকাউন্ট নির্বাচন করুন", color = scheme.onSurface, fontWeight = FontWeight.Bold) }
                                Text("⌄", color = accent, fontSize = 20.sp)
                            }
                        }
                        DropdownMenu(expanded = walletExpanded, onDismissRequest = { walletExpanded = false }) {
                            wallets.forEach { wallet -> DropdownMenuItem(text = { Text(wallet.name) }, onClick = { selectedWalletId = wallet.id; walletExpanded = false }) }
                        }
                    }
                    Spacer(Modifier.height(11.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { currencyMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(19.dp), tint = accent)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) { Text("Currency", color = scheme.onSurfaceVariant, fontSize = 11.sp); Text(currency, color = scheme.onSurface, fontWeight = FontWeight.Bold) }
                                Text("⌄", color = accent, fontSize = 20.sp)
                            }
                        }
                        DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
                            listOf("MVR", "BDT", "USD").forEach { item -> DropdownMenuItem(text = { Text(when (item) { "MVR" -> "🇲🇻  MVR"; "BDT" -> "🇧🇩  BDT"; else -> "🇺🇸  USD" }) }, onClick = { currency = item; currencyMenu = false }) }
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(19.dp), tint = accent)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) { Text("Category", color = scheme.onSurfaceVariant, fontSize = 11.sp); Text(category, color = scheme.onSurface, fontWeight = FontWeight.Bold) }
                                Text("⌄", color = accent, fontSize = 20.sp)
                            }
                        }
                        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            categories.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryMenu = false }) }
                            DropdownMenuItem(text = { Text("➕ নতুন ক্যাটাগরি") }, onClick = { categoryMenu = false; customCategoryName = ""; showCustomCategoryDialog = true })
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    OutlinedTextField(value = date, onValueChange = { }, readOnly = true, label = { Text("Date", color = scheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(20.dp)) }, trailingIcon = {
                        TextButton(onClick = {
                            val calendar = Calendar.getInstance()
                            try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.let { calendar.time = it } } catch (_: Exception) {}
                            DatePickerDialog(context, { _, year, month, dayOfMonth -> date = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                    Spacer(Modifier.height(11.dp))
                    OutlinedTextField(value = reason, onValueChange = {
                        reason = it
                        // নোট থেকে ক্যাটাগরি auto-detect (ম্যানুয়ালি সিলেক্ট করলেও পরে টাইপ করলে আবার হবে)
                        CategoryDetector.detect(type, it)?.let { detected ->
                            if (detected in categories) category = detected
                        }
                    }, label = { Text(when (type) { "income" -> "Income Source / Note"; "expense" -> "Expense Note"; else -> "Note" }, color = scheme.onSurfaceVariant) }, placeholder = { Text("বিস্তারিত লিখুন", color = scheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }, maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { receiptPickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (receiptImage != null) accent else scheme.outline)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (receiptImage != null) Icons.Default.CheckCircle else Icons.Default.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (receiptImage != null) "Receipt" else "Attach", color = if (receiptImage != null) accent else scheme.onSurface)
                            }
                        }
                        OutlinedButton(onClick = { launchCameraScan() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Scan", color = accent)
                            }
                        }
                    }
                    if (receiptImage != null) { TextButton(onClick = { receiptImage = null }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Remove Receipt", color = ExpenseRed, fontSize = 11.sp) } }
                    if (type == "home" && loans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp)); Text(text = "🏦 এই টাকার অংশ কি Loan payment?", fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedButton(onClick = { loanExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) { Text(selectedLoanId?.let { id -> loans.firstOrNull { it.id == id }?.name ?: "Loan নির্বাচন করুন" } ?: "Loan নির্বাচন করুন") }
                            DropdownMenu(expanded = loanExpanded, onDismissRequest = { loanExpanded = false }) { loans.forEach { loan -> DropdownMenuItem(text = { Text(loan.name) }, onClick = { selectedLoanId = loan.id; loanExpanded = false }) } }
                        }
                        if (selectedLoanId != null) { Spacer(Modifier.height(10.dp)); OutlinedTextField(value = loanPaymentAmount, onValueChange = { loanPaymentAmount = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), label = { Text("এই Loan-এ কত টাকা দিলেন?") }, placeholder = { Text("যেমন 20000") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) { Text("বাতিল", fontWeight = FontWeight.SemiBold) }; Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val value = amount.replace(",", "").trim().toDoubleOrNull()
                            if (value != null && value > 0 && date.isNotBlank()) {
                                onSave(value, currency, category, reason.ifBlank { category }, date, selectedWalletId, receiptImage)
                                if (type == "home" && selectedLoanId != null && loanPaymentAmount.isNotBlank()) {
                                    val loanAmount = loanPaymentAmount.replace(",", "").trim().toDoubleOrNull()
                                    if (loanAmount != null && loanAmount > 0) onLoanPayment(selectedLoanId!!, loanAmount, date)
                                }
                            } else Toast.makeText(context, "সঠিক টাকার পরিমাণ দিন", Toast.LENGTH_SHORT).show()
                        }, shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White), elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)) {
                            Text(if (existingTransaction != null) "Update করুন" else "সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCategoryDialog = false },
            title = { Text("নতুন ক্যাটাগরি") },
            text = {
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    label = { Text("ক্যাটাগরির নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val candidate = normalizeCategoryName(customCategoryName)
                    if (candidate.isBlank()) {
                        Toast.makeText(context, "ক্যাটাগরির নাম লিখুন।", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (candidate in categories) {
                        category = candidate
                        showCustomCategoryDialog = false
                        return@Button
                    }
                    onCategoryAdded(candidate)
                    category = candidate
                    showCustomCategoryDialog = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showCustomCategoryDialog = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
fun LoanDialog(
    onDismiss: () -> Unit,
    existingLoan: LoanAccount? = null,
    existingNames: List<String> = emptyList(),
    onSave: (
        String,
        String,
        Double,
        Double,
        String,
        String,
        String?
    ) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var name by remember { mutableStateOf(existingLoan?.name ?: "") }
    var sourceType by remember { mutableStateOf(existingLoan?.sourceType ?: "bank") }
    var principal by remember { mutableStateOf(existingLoan?.principal?.let(::formatMoney) ?: "") }
    var installment by remember { mutableStateOf(existingLoan?.monthlyInstallment?.let(::formatMoney) ?: "") }
    var date by remember { mutableStateOf(existingLoan?.startDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember { mutableStateOf(existingLoan?.dueDate ?: "") }
    var note by remember { mutableStateOf(existingLoan?.note ?: "") }
    var sourceMenu by remember { mutableStateOf(false) }
    var nameMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = scheme.surface)) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = scheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("ঋণের তথ্য", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("ব্যাংক বা মানুষের কাছ থেকে নেওয়া ঋণ", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("কার কাছ থেকে / ব্যাংকের নাম") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        if (existingLoan == null && existingNames.isNotEmpty()) {
                            TextButton(onClick = { nameMenu = true }, modifier = Modifier.align(Alignment.CenterEnd)) { Text("আগের নাম") }
                            DropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) {
                                existingNames.filter { it.isNotBlank() }.distinct().forEach { existingName -> DropdownMenuItem(text = { Text(existingName) }, onClick = { name = existingName; nameMenu = false }) }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { sourceMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (sourceType == "bank") "ব্যাংক ঋণ" else "ব্যক্তিগত ঋণ")
                            }
                        }
                        DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                            DropdownMenuItem(text = { Text("ব্যাংক ঋণ") }, leadingIcon = { Icon(Icons.Default.AccountBalance, null) }, onClick = { sourceType = "bank"; sourceMenu = false })
                            DropdownMenuItem(text = { Text("ব্যক্তিগত ঋণ") }, leadingIcon = { Icon(Icons.Default.Person, null) }, onClick = { sourceType = "person"; sourceMenu = false })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = principal, onValueChange = { principal = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("মোট ঋণের টাকা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = installment, onValueChange = { installment = it }, label = { Text("মাসিক কিস্তি (না থাকলে 0)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val calendar = Calendar.getInstance()
                            try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.let { calendar.time = it } } catch (_: Exception) {}
                            DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { 
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("শুরুর তারিখ", fontSize = 10.sp)
                                Text(date, fontSize = 12.sp)
                            }
                        }
                        
                        OutlinedButton(onClick = {
                            val calendar = Calendar.getInstance()
                            try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dueDate.ifBlank { date })?.let { calendar.time = it } } catch (_: Exception) {}
                            DatePickerDialog(context, { _, year, month, day -> dueDate = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("পরিশোধের তারিখ", fontSize = 10.sp)
                                Text(dueDate.ifBlank { "সিলেক্ট করুন" }, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("নোট") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("বাতিল") }; Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val amount = principal.replace(",", "").trim().toDoubleOrNull()
                            val monthly = installment.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && amount != null && amount > 0.0 && monthly >= 0.0) onSave(name.trim(), sourceType, amount, monthly, date, note.trim(), dueDate.takeIf { it.isNotBlank() })
                            else Toast.makeText(context, "নাম ও সঠিক ঋণের টাকা দিন।", Toast.LENGTH_SHORT).show()
                        }, colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(14.dp)) { Text("সংরক্ষণ করুন") }
                    }
                }
            }
        }
    }
}

@Composable
fun LoanPaymentDialog(loan: LoanAccount, onDismiss: () -> Unit, onSave: (Double, String, String) -> Unit) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var note by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Green)
                    Spacer(Modifier.width(8.dp))
                    Text("ঋণ পরিশোধ", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(loan.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("পরিশোধের টাকা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { val calendar = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(date)
                    }
                }
                Spacer(Modifier.height(10.dp)); OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("নোট") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }; Spacer(Modifier.width(8.dp))
                    Button(onClick = { val value = amount.replace(",", "").trim().toDoubleOrNull(); if (value != null && value > 0.0) onSave(value, date, note.trim()) else Toast.makeText(context, "সঠিক পরিশোধের টাকা দিন。", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("পরিশোধ সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun LendingDialog(onDismiss: () -> Unit, onSave: (String, Double, String, String, String?) -> Unit) {
    val context = LocalContext.current
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("কাউকে ধার দিন", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text("এই টাকা খরচ হিসেবে ধরা হবে না; আলাদা পাওনা থাকবে。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = person, onValueChange = { person = it }, label = { Text("ব্যক্তির নাম") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("ধারের টাকা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { val calendar = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { 
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("দেওয়ার তারিখ", fontSize = 10.sp)
                            Text(date, fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(onClick = { val calendar = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> dueDate = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { 
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("ফেরত পাওয়ার তারিখ", fontSize = 10.sp)
                            Text(dueDate.ifBlank { "সিলেক্ট করুন" }, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(10.dp)); OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("নোট") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }; Spacer(Modifier.width(8.dp))
                    Button(onClick = { val value = amount.replace(",", "").trim().toDoubleOrNull(); if (person.isNotBlank() && value != null && value > 0.0) onSave(person.trim(), value, date, note.trim(), dueDate.takeIf { it.isNotBlank() }) else Toast.makeText(context, "নাম ও সঠিক টাকার পরিমাণ দিন。", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text("ধার সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun LendingReturnDialog(lending: LendingAccount, onDismiss: () -> Unit, onSave: (Double, String, String) -> Unit) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var note by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, contentDescription = null, tint = IncomeGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("ধার ফেরত পেলাম", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(lending.person, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("ফেরত পাওয়া টাকা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { val calendar = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("📅  $date") }
                Spacer(Modifier.height(10.dp)); OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("নোট") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }; Spacer(Modifier.width(8.dp))
                    Button(onClick = { val value = amount.replace(",", "").trim().toDoubleOrNull(); if (value != null && value > 0.0) onSave(value, date, note.trim()) else Toast.makeText(context, "সঠিক ফেরতের টাকা দিন。", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)) { Text("ফেরত সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun WalletDialog(
    onDismiss: () -> Unit,
    existingWallet: Wallet? = null,
    onSave: (String, String, Double, String, Int) -> Unit,
    onDelete: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    var name by remember { mutableStateOf(existingWallet?.name ?: "") }
    var type by remember { mutableStateOf(existingWallet?.type ?: "Cash") }
    var initialBalance by remember { mutableStateOf(existingWallet?.initialBalance?.toString() ?: "") }
    var currency by remember { mutableStateOf(existingWallet?.currency ?: "BDT") }
    var selectedColor by remember { mutableIntStateOf(existingWallet?.color ?: 0xFF4CAF50.toInt()) }
    var typeExpanded by remember { mutableStateOf(false) }

    val walletTypes = listOf("Cash", "Bank Account", "Bikash", "Nagad", "Rocket", "Credit Card", "Debit Card", "Other")
    val colors = listOf(0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFF44336.toInt(), 0xFFFF9800.toInt(), 0xFF9C27B0.toInt(), 0xFF795548.toInt(), 0xFF607D8B.toInt(), 0xFFE91E63.toInt())

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (existingWallet == null) Icons.Default.AddBusiness else Icons.Default.Edit, contentDescription = null, tint = scheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(if (existingWallet == null) "নতুন অ্যাকাউন্ট যোগ করুন" else "অ্যাকাউন্ট এডিট করুন", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("অ্যাকাউন্টের নাম (যেমন: My Bank)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                
                Box {
                    OutlinedButton(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("ধরন: $type")
                    }
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        walletTypes.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                
                OutlinedTextField(value = initialBalance, onValueChange = { initialBalance = it }, label = { Text("শুরুর ব্যালেন্স (ঐচ্ছিক)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                
                Text("রঙ নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    colors.forEach { c ->
                        Surface(
                            modifier = Modifier.size(35.dp),
                            shape = CircleShape,
                            color = Color(c.toLong() and 0xFFFFFFFFL),
                            border = if (selectedColor == c) androidx.compose.foundation.BorderStroke(3.dp, scheme.onSurface) else null,
                            onClick = { selectedColor = c }
                        ) {}
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (existingWallet != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = existingWallet.id != "default_cash"
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Wallet",
                                tint = if (existingWallet.id == "default_cash")
                                    MaterialTheme.colorScheme.outline
                                else
                                    ExpenseRed
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            onSave(name, type, initialBalance.toDoubleOrNull() ?: 0.0, currency, selectedColor)
                        }
                    }, shape = RoundedCornerShape(12.dp)) { Text("সংরক্ষণ করুন") }
                }
            }
        }
    }
}
