package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun HomeLendingReturnDialog(
    lending: LendingAccount,
    alreadyReturned: Double,
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: String, note: String) -> Unit
) {
    val remaining = (lending.amount - alreadyReturned).coerceAtLeast(0.0)
    var amount by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
    }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
        title = {
            Text("বাড়ির ধার ফেরত নিন", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp)) {
                        Icon(
                            Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text("কার কাছে: ${lending.person}", fontWeight = FontWeight.Bold)
                            Text("মোট ধার: ৳${formatMoney(lending.amount)}")
                            Text("আগে ফেরত: ৳${formatMoney(alreadyReturned)}")
                            Text("এখন বাকি: ৳${formatMoney(remaining)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ফেরত পাওয়া টাকা") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("তারিখ") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull() ?: return@Button
                    if (value > 0.0 && value <= remaining && date.isNotBlank()) {
                        onSave(value, date.trim(), note.trim())
                    }
                },
                enabled = remaining > 0.0
            ) {
                Text("ফেরত নিন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
