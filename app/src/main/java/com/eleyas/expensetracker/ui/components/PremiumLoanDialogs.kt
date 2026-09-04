package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LoanAccount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PremiumLoanDialog(
    onDismiss: () -> Unit,
    existingLoan: LoanAccount? = null,
    existingNames: List<String> = emptyList(),
    onSave: (String, String, Double, Double, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val editing = existingLoan != null

    var name by remember(existingLoan?.id) { mutableStateOf(existingLoan?.name ?: "") }
    var sourceType by remember(existingLoan?.id) { mutableStateOf(existingLoan?.sourceType ?: "bank") }
    var principal by remember(existingLoan?.id) { mutableStateOf(existingLoan?.principal?.let(::formatMoney) ?: "") }
    var installment by remember(existingLoan?.id) { mutableStateOf(existingLoan?.monthlyInstallment?.let(::formatMoney) ?: "") }
    var date by remember(existingLoan?.id) { mutableStateOf(existingLoan?.startDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember(existingLoan?.id) { mutableStateOf(existingLoan?.dueDate ?: "") }
    var note by remember(existingLoan?.id) { mutableStateOf(existingLoan?.note ?: "") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var namesExpanded by remember { mutableStateOf(false) }

    fun pickDate(initial: String, onSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(initial)?.let { calendar.time = it }
        } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, day -> onSelected("%02d/%02d/%04d".format(day, month + 1, year)) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).clip(CircleShape).background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (editing) "ঋণ এডিট করুন" else "নতুন ঋণ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary
                            )
                            Text(
                                if (sourceType == "bank") "ব্যাংক ঋণ" else "ব্যক্তিগত ঋণ",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("ঋণের তথ্য ও পরিশোধ পরিকল্পনা", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ")
                        }
                    }
                }

                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(15.dp)) {
                            Text("ঋণের ধরন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { sourceExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (sourceType == "bank") "ব্যাংক ঋণ" else "ব্যক্তিগত ঋণ", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("ব্যাংক ঋণ") },
                                        leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                                        onClick = { sourceType = "bank"; sourceExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("ব্যক্তিগত ঋণ") },
                                        leadingIcon = { Icon(Icons.Default.Person, null) },
                                        onClick = { sourceType = "person"; sourceExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (sourceType == "bank") "ব্যাংকের নাম" else "কার কাছ থেকে ঋণ নিয়েছেন") },
                        leadingIcon = { Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null) },
                        trailingIcon = {
                            if (!editing && existingNames.any { it.isNotBlank() }) {
                                IconButton(onClick = { namesExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "আগের নাম") }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp)
                    )
                    DropdownMenu(expanded = namesExpanded, onDismissRequest = { namesExpanded = false }) {
                        existingNames.filter { it.isNotBlank() }.distinct().forEach { existingName ->
                            DropdownMenuItem(text = { Text(existingName) }, onClick = { name = existingName; namesExpanded = false })
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = principal,
                        onValueChange = { principal = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("মোট ঋণের টাকা") },
                        placeholder = { Text("যেমন: 50000") },
                        leadingIcon = { Text("৳", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = installment,
                        onValueChange = { installment = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("মাসিক কিস্তি") },
                        placeholder = { Text("না থাকলে 0") },
                        leadingIcon = { Icon(Icons.Default.Payments, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp)
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanDatePickerCard(
                            modifier = Modifier.weight(1f),
                            title = "শুরুর তারিখ",
                            value = date,
                            icon = Icons.Default.CalendarMonth,
                            onClick = { pickDate(date) { date = it } }
                        )
                        LoanDatePickerCard(
                            modifier = Modifier.weight(1f),
                            title = "পরিশোধের তারিখ",
                            value = dueDate.ifBlank { "নির্ধারিত নয়" },
                            icon = Icons.Default.EventAvailable,
                            onClick = { pickDate(dueDate.ifBlank { date }) { dueDate = it } }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("নোট (ঐচ্ছিক)") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        maxLines = 3,
                        shape = RoundedCornerShape(15.dp)
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) { Text("বাতিল") }
                        Button(
                            onClick = {
                                val amount = principal.replace(",", "").trim().toDoubleOrNull()
                                val monthly = installment.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                                if (name.isBlank() || amount == null || amount <= 0.0 || monthly < 0.0) {
                                    Toast.makeText(context, "নাম ও সঠিক ঋণের টাকা দিন।", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSave(name.trim(), sourceType, amount, monthly, date, note.trim(), dueDate.takeIf { it.isNotBlank() })
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(7.dp))
                            Text(if (editing) "আপডেট করুন" else "ঋণ সংরক্ষণ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDatePickerCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(15.dp),
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

@Composable
fun PremiumBankLoanPaymentDialog(
    loan: LoanAccount,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var note by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).clip(CircleShape).background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalance, null, tint = scheme.primary, modifier = Modifier.size(27.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ব্যাংক ঋণ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = scheme.primary)
                        Text("ঋণ পরিশোধ", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text(loan.name, fontSize = 11.sp, color = scheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                }

                Spacer(Modifier.height(14.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("এই ঋণের পেমেন্ট রেকর্ড করুন", fontSize = 12.sp, color = scheme.onPrimaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("৳${formatMoney(loan.principal)}", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = scheme.onPrimaryContainer)
                        Text("মূল ঋণের পরিমাণ", fontSize = 10.sp, color = scheme.onPrimaryContainer.copy(alpha = 0.75f))
                    }
                }

                Spacer(Modifier.height(13.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("পরিশোধের টাকা") },
                    leadingIcon = { Icon(Icons.Default.Payments, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) },
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text("পরিশোধের তারিখ", fontSize = 10.sp, color = scheme.onSurfaceVariant)
                        Text(date, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("নোট (ঐচ্ছিক)") },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(15.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val value = amount.replace(",", "").trim().toDoubleOrNull()
                            if (value == null || value <= 0.0) {
                                Toast.makeText(context, "সঠিক পরিশোধের টাকা দিন।", Toast.LENGTH_SHORT).show()
                            } else {
                                onSave(value, date, note.trim())
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(7.dp))
                        Text("পরিশোধ সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
