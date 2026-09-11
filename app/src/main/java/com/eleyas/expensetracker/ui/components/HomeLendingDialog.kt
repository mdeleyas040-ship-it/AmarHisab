package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.util.FundSource
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.eleyas.expensetracker.ui.components.WarningPopupManager

@Composable
fun HomeLendingDialog(
    onDismiss: () -> Unit,
    recentPeople: List<String> = emptyList(),
    onSave: (person: String, amount: Double, date: String, note: String) -> Unit
) {
    val context = LocalContext.current
    val appViewModel: MainViewModel = viewModel()
    val homeLendingPeople = appViewModel.lendings.filter { FundSource.isHomeLending(it) }.map { it.person }
    val suggestionSource = if (recentPeople.isNotEmpty()) recentPeople else homeLendingPeople
    var person by remember { mutableStateOf("") }
    var personFocused by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 12) }.time)
        )
    }
    var note by remember { mutableStateOf("") }
    val peopleSuggestions = remember(person, suggestionSource) {
        suggestionSource.map { it.trim() }.filter { it.isNotBlank() }
            .distinctBy { it.lowercase(java.util.Locale.getDefault()) }
            .filter { person.isBlank() || it.contains(person.trim(), ignoreCase = true) }.take(5)
    }
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
                Modifier.fillMaxWidth().heightIn(max = 680.dp).verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(primary, RoundedCornerShape(26.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(52.dp), RoundedCornerShape(18.dp), color = onPrimary.copy(alpha = 0.12f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Handshake, null, tint = onPrimary, modifier = Modifier.size(29.dp))
                            }
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text("বাড়ির হিসাব", fontSize = 10.sp, letterSpacing = 1.1.sp, color = onPrimary.copy(alpha = 0.78f), fontWeight = FontWeight.Bold)
                            Text("বাড়ির টাকা দিয়ে ধার দিন", fontSize = 17.sp, color = onPrimary, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                            Text("বাড়ির হিসাব থেকে কাউকে টাকা ধার দিন", fontSize = 10.sp, color = onPrimary.copy(alpha = 0.78f), maxLines = 2)
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(onClick = onDismiss, modifier = Modifier.size(40.dp), shape = RoundedCornerShape(13.dp), color = onPrimary.copy(alpha = 0.10f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, "বন্ধ করুন", tint = onPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(17.dp), color = primary.copy(alpha = 0.10f)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(36.dp), RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.12f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Info, null, tint = primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text("বাড়ির পাওনা হিসেবে সংরক্ষণ হবে", color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                Text("এই টাকা সাধারণ খরচ হিসেবে ধরা হবে না।", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = person, onValueChange = { person = it },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { personFocused = it.isFocused },
                        label = { Text("যাকে বাড়ির টাকা দিয়ে ধার দিচ্ছেন") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = primary) }, singleLine = true, shape = RoundedCornerShape(16.dp)
                    )
                    if (personFocused && peopleSuggestions.isNotEmpty()) {
                        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                peopleSuggestions.forEach { name ->
                                    Surface(onClick = { person = name; personFocused = false }, Modifier.fillMaxWidth(), color = Color.Transparent) {
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, tint = primary, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp)); Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("ধারের টাকা") },
                        leadingIcon = { Text("৳", color = primary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(16.dp)
                    )

                    HomeDatePickerField(value = date, label = "দেওয়ার তারিখ", onDateSelected = { date = it })

                    OutlinedTextField(
                        value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("কারণ / নোট") }, leadingIcon = { Icon(Icons.Default.ReceiptLong, null, tint = primary) },
                        minLines = 2, maxLines = 3, shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(1.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(0.78f).height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("বাতিল", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val value = amount.replace(",", "").trim().toDoubleOrNull() ?: return@Button
                                if (person.isBlank() || value <= 0.0 || date.isBlank()) return@Button
                                val availableHome = appViewModel.homeBalance
                                if (value > availableHome) {
                                    WarningPopupManager.show(
                                        title = "পর্যাপ্ত টাকা নেই",
                                        message = "বাড়ির হিসাবে পর্যাপ্ত টাকা নেই.\n\nঅবশিষ্ট: ৳${formatMoney(availableHome)}"
                                    )
                                    return@Button
                                }
                                val homeNote = listOf("[HOME]", note.trim()).filter { it.isNotBlank() }.joinToString(" ")
                                appViewModel.addLending(
                                    context = context,
                                    person = person.trim(),
                                    amount = value,
                                    date = date.trim(),
                                    note = homeNote,
                                    dueDate = date.trim(),
                                    fundSource = "home"
                                )
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.22f).height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = onPrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp))
                            Text("বাড়ির টাকা দিয়ে ধার দিন", fontSize = 9.sp, maxLines = 1, softWrap = false, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 1.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, null, tint = primary, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp))
                        Text("বাড়ির হিসাব থেকে টাকা যাবে এবং ফেরত এলে আবার যোগ হবে", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }
}
