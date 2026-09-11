package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.util.Green
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LendingEditDialog(
    lending: LendingAccount,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    var person by remember(lending.id) { mutableStateOf(lending.person) }
    var amount by remember(lending.id) { mutableStateOf(formatMoney(lending.amount)) }
    var date by remember(lending.id) { mutableStateOf(lending.date) }
    var note by remember(lending.id) { mutableStateOf(lending.note) }
    var dueDate by remember(lending.id) { mutableStateOf(lending.dueDate ?: "") }

    fun pickDate(current: String, onPicked: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(current)?.let { calendar.time = it } }
        DatePickerDialog(
            context,
            { _, year, month, day -> onPicked("%02d/%02d/%04d".format(day, month + 1, year)) },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("ধারের তথ্য এডিট", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(person, { person = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ব্যক্তির নাম") }, singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(amount, { amount = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ধারের পরিমাণ") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { pickDate(date) { date = it } }, modifier = Modifier.fillMaxWidth()) { Text("তারিখ: ${date.ifBlank { "নির্বাচন করুন" }}") }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { pickDate(dueDate.ifBlank { date }) { dueDate = it } }, modifier = Modifier.fillMaxWidth()) { Text("পরিশোধের শেষ তারিখ: ${dueDate.ifBlank { "নেই" }}") }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("নোট (ঐচ্ছিক)") }, minLines = 2)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(onClick = {
                        val parsed = amount.replace(",", "").trim().toDoubleOrNull()
                        if (person.trim().isBlank()) Toast.makeText(context, "ব্যক্তির নাম দিন।", Toast.LENGTH_SHORT).show()
                        else if (parsed == null || parsed <= 0.0) Toast.makeText(context, "সঠিক টাকার পরিমাণ দিন।", Toast.LENGTH_SHORT).show()
                        else onSave(person.trim(), parsed, date, note.trim(), dueDate.trim().ifBlank { null })
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("Save") }
                }
            }
        }
    }
}
