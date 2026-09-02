package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.ui.theme.Green
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
    val accent = if (existingLoan?.sourceType == "person") Color(0xFF3B82F6) else Green

    var name by remember { mutableStateOf(existingLoan?.name ?: "") }
    var sourceType by remember { mutableStateOf(existingLoan?.sourceType ?: "bank") }
    var principal by remember { mutableStateOf(existingLoan?.principal?.let(::formatMoney) ?: "") }
    var installment by remember { mutableStateOf(existingLoan?.monthlyInstallment?.let(::formatMoney) ?: "") }
    var date by remember { mutableStateOf(existingLoan?.startDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember { mutableStateOf(existingLoan?.dueDate ?: "") }
    var note by remember { mutableStateOf(existingLoan?.note ?: "") }
    var sourceMenu by remember { mutableStateOf(false) }
    var nameMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(54.dp).background(accent.copy(alpha = .14f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (existingLoan == null) "নতুন ঋণ" else "ঋণ এডিট", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (sourceType == "bank") "Bank Loan • ঋণের বিস্তারিত তথ্য" else "Personal Loan • ঋণের বিস্তারিত তথ্য",
                                fontSize = 11.sp,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "ঋণের মূল টাকা, কিস্তি ও পরিশোধের সময়সূচি এক জায়গায় রাখুন।",
                        fontSize = 12.sp,
                        color = scheme.onSurfaceVariant
                    )
                }

                item {
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(if (sourceType == "bank") "ব্যাংকের নাম" else "ব্যক্তির নাম") },
                            leadingIcon = { Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent) },
                            trailingIcon = if (existingLoan == null && existingNames.isNotEmpty()) ({
                                TextButton(onClick = { nameMenu = true }) { Text("আগের নাম") }
                            }) else null,
                            shape = RoundedCornerShape(17.dp)
                        )
                        DropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) {
                            existingNames.filter { it.isNotBlank() }.distinct().forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { name = it; nameMenu = false })
                            }
                        }
                    }
                }

                item {
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { sourceMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent)
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("ঋণের ধরন", fontSize = 10.sp, color = scheme.onSurfaceVariant)
                                Text(if (sourceType == "bank") "Bank Loan" else "Personal Loan", fontWeight = FontWeight.Bold)
                            }
                            Text("⌄", fontSize = 20.sp, color = accent)
                        }
                        DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("ব্যাংক ঋণ") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                                onClick = { sourceType = "bank"; sourceMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("ব্যক্তিগত ঋণ") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                onClick = { sourceType = "person"; sourceMenu = false }
                            )
                        }
                    }
                }

                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .08f))) {
                        Column(Modifier.padding(15.dp)) {
                            Text("ঋণের পরিমাণ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
                            Spacer(Modifier.height(7.dp))
                            OutlinedTextField(
                                value = principal,
                                onValueChange = { principal = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("মোট ঋণের টাকা") },
                                leadingIcon = { Text("৳", color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(Modifier.height(9.dp))
                            OutlinedTextField(
                                value = installment,
                                onValueChange = { installment = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("মাসিক কিস্তি (না থাকলে 0)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanDateSelector("শুরুর তারিখ", date, accent, Modifier.weight(1f)) {
                            val c = Calendar.getInstance()
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.let { c.time = it }
                            DatePickerDialog(context, { _, y, m, d -> date = "%02d/%02d/%04d".format(d, m + 1, y) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                        }
                        LoanDateSelector("পরিশোধের তারিখ", dueDate.ifBlank { "নির্ধারিত নয়" }, accent, Modifier.weight(1f)) {
                            val c = Calendar.getInstance()
                            if (dueDate.isNotBlank()) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dueDate)?.let { c.time = it }
                            DatePickerDialog(context, { _, y, m, d -> dueDate = "%02d/%02d/%04d".format(d, m + 1, y) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("নোট (ঐচ্ছিক)") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = accent) },
                        maxLines = 3,
                        shape = RoundedCornerShape(17.dp)
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("বাতিল") }
                        Button(
                            onClick = {
                                val amount = principal.replace(",", "").trim().toDoubleOrNull()
                                val monthly = installment.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                                if (name.isBlank()) {
                                    Toast.makeText(context, "নাম দিন।", Toast.LENGTH_SHORT).show()
                                } else if (amount == null || amount <= 0.0 || monthly < 0.0) {
                                    Toast.makeText(context, "সঠিক ঋণের টাকা দিন।", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSave(name.trim(), sourceType, amount, monthly, date, note.trim(), dueDate.takeIf { it.isNotBlank() })
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDateSelector(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(65.dp), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
