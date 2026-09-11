package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(primary, RoundedCornerShape(26.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = onPrimary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = onPrimary,
                                    modifier = Modifier.size(29.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "বাড়ির হিসাব",
                                fontSize = 10.sp,
                                letterSpacing = 1.1.sp,
                                color = onPrimary.copy(alpha = 0.78f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "বাড়ির ধার ফেরত নিন",
                                fontSize = 18.sp,
                                color = onPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                "যার কাছে ধার দিয়েছেন, তার কাছ থেকে টাকা ফেরত নিন",
                                fontSize = 10.sp,
                                color = onPrimary.copy(alpha = 0.78f),
                                maxLines = 2
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(13.dp),
                            color = onPrimary.copy(alpha = 0.10f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "বন্ধ করুন",
                                    tint = onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(17.dp),
                        color = primary.copy(alpha = 0.10f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(36.dp),
                                RoundedCornerShape(12.dp),
                                color = primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "কার কাছ থেকে টাকা ফেরত আসছে",
                                    color = primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "এই টাকা আবার বাড়ির হিসাবে যোগ হবে।",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                        )
                    ) {
                        Row(
                            Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(42.dp),
                                RoundedCornerShape(14.dp),
                                color = primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Handshake,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${lending.person}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("মোট ধার  ৳${formatMoney(lending.amount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("এখন বাকি", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${formatMoney(remaining)}", fontSize = 16.sp, color = primary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 13.dp, end = 13.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                Modifier.weight(1f),
                                RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text("ফেরত দেওয়া", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${formatMoney(alreadyReturned)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                Modifier.weight(1f),
                                RoundedCornerShape(12.dp),
                                color = primary.copy(alpha = 0.10f)
                            ) {
                                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text("বাকি", fontSize = 9.sp, color = primary)
                                    Text("৳${formatMoney(remaining)}", fontSize = 12.sp, color = primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ফেরত পাওয়া টাকা") },
                        leadingIcon = {
                            Text("৳", color = primary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    HomeDatePickerField(
                        value = date,
                        label = "ফেরত তারিখ",
                        onDateSelected = { date = it }
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("নোট (ঐচ্ছিক)") },
                        leadingIcon = {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = primary)
                        },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(1.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(0.78f).height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("বাতিল", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val value = amount.replace(",", "").trim().toDoubleOrNull() ?: return@Button
                                if (value > 0.0 && value <= remaining && date.isNotBlank()) {
                                    onSave(value, date.trim(), note.trim())
                                }
                            },
                            enabled = remaining > 0.0,
                            modifier = Modifier.weight(1.22f).height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                contentColor = onPrimary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "বাড়ির ধার সংরক্ষণ করুন",
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "ফেরত তারিখ অনুযায়ী হিসাব আপডেট হবে",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
