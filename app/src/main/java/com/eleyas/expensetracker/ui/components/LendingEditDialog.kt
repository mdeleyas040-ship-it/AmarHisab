package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LendingAccount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun LendingEditDialog(
    lending: LendingAccount,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var person by remember(lending.id) { mutableStateOf(lending.person) }
    var amount by remember(lending.id) { mutableStateOf(lending.amount.toString()) }
    var date by remember(lending.id) { mutableStateOf(lending.date) }
    var note by remember(lending.id) { mutableStateOf(lending.note) }
    var dueDate by remember(lending.id) { mutableStateOf(lending.dueDate ?: "") }

    fun pickDate(initial: String, onSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(initial)?.let { calendar.time = it } } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, day -> onSelected("%02d/%02d/%04d".format(day, month + 1, year)) },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = scheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ধার দেওয়া এডিট", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text("ধারের তথ্য পরিবর্তন করুন", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                }

                OutlinedTextField(
                    value = person, onValueChange = { person = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("ব্যক্তির নাম") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    shape = RoundedCornerShape(15.dp)
                )

                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("ধারের টাকা") },
                    leadingIcon = { Icon(Icons.Default.Payments, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(15.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(
                        onClick = { pickDate(date) { date = it } },
                        modifier = Modifier.weight(1f).height(62.dp),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("তারিখ", fontSize = 9.sp, color = scheme.onSurfaceVariant)
                            Text(date, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { pickDate(dueDate.ifBlank { date }) { dueDate = it } },
                        modifier = Modifier.weight(1f).height(62.dp),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("ফেরতের তারিখ", fontSize = 9.sp, color = scheme.onSurfaceVariant)
                            Text(dueDate.ifBlank { "নির্ধারিত নয়" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("নোট") }, maxLines = 3,
                    shape = RoundedCornerShape(15.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val value = amount.replace(",", "").trim().toDoubleOrNull()
                            if (person.isBlank() || value == null || value <= 0.0) {
                                android.widget.Toast.makeText(context, "নাম ও সঠিক টাকার পরিমাণ দিন।", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                onSave(person.trim(), value, date, note.trim(), dueDate.takeIf { it.isNotBlank() })
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp)
                    ) { Text("আপডেট করুন", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
