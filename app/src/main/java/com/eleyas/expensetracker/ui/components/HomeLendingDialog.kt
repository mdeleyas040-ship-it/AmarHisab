package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.util.FundSource
import com.eleyas.expensetracker.viewmodel.MainViewModel

@Composable
fun HomeLendingDialog(
    onDismiss: () -> Unit,
    recentPeople: List<String> = emptyList(),
    onSave: (person: String, amount: Double, date: String, note: String) -> Unit
) {
    val appViewModel: MainViewModel = viewModel()
    val homeLendingPeople = appViewModel.lendings.filter { FundSource.isHomeLending(it) }.map { it.person }
    val suggestionSource = if (recentPeople.isNotEmpty()) recentPeople else homeLendingPeople
    var person by remember { mutableStateOf("") }
    var personFocused by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()))
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
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(primary, RoundedCornerShape(26.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(58.dp), RoundedCornerShape(20.dp), color = onPrimary.copy(alpha = 0.12f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Handshake, null, tint = onPrimary, modifier = Modifier.size(31.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("বাড়ির হিসাব", fontSize = 11.sp, letterSpacing = 1.2.sp, color = onPrimary.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
                            Text("বাড়ির টাকা দিয়ে ধার দিন", fontSize = 18.sp, color = onPrimary, fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false)
                            Text("বাড়ির হিসাব থেকে কাউকে টাকা ধার দিন", fontSize = 11.sp, color = onPrimary.copy(alpha = 0.72f), maxLines = 1, softWrap = false)
                        }
                        Surface(onClick = onDismiss, modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = onPrimary.copy(alpha = 0.10f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, "বন্ধ করুন", tint = onPrimary, modifier = Modifier.size(25.dp))
                            }
                        }
                    }
                }

                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = primary.copy(alpha = 0.10f)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(38.dp), RoundedCornerShape(13.dp), color = primary.copy(alpha = 0.12f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Info, null, tint = primary, modifier = Modifier.size(21.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("বাড়ির পাওনা হিসেবে সংরক্ষণ হবে", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("এই টাকা সাধারণ খরচ হিসেবে ধরা হবে না।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = person, onValueChange = { person = it },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { personFocused = it.isFocused },
                        label = { Text("যাকে বাড়ির টাকা দিয়ে ধার দিচ্ছেন") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = primary) }, singleLine = true, shape = RoundedCornerShape(17.dp)
                    )
                    if (personFocused && peopleSuggestions.isNotEmpty()) {
                        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                peopleSuggestions.forEach { name ->
                                    Surface(onClick = { person = name; personFocused = false }, Modifier.fillMaxWidth(), color = Color.Transparent) {
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, tint = primary, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(9.dp)); Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("ধারের টাকা") },
                        leadingIcon = { Text("৳", color = primary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(17.dp)
                    )

                    HomeDatePickerField(value = date, label = "দেওয়ার তারিখ", onDateSelected = { date = it })

                    OutlinedTextField(
                        value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("কারণ / নোট") }, leadingIcon = { Icon(Icons.Default.ReceiptLong, null, tint = primary) },
                        minLines = 2, shape = RoundedCornerShape(17.dp)
                    )

                    Spacer(Modifier.height(2.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(0.8f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("বাতিল", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val value = amount.replace(",", "").trim().toDoubleOrNull() ?: return@Button
                                if (person.isNotBlank() && value > 0.0 && date.isNotBlank()) onSave(person.trim(), value, date.trim(), note.trim())
                            },
                            modifier = Modifier.weight(1.2f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = onPrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                            Text("বাড়ির টাকা দিয়ে ধার দিন", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, null, tint = primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp))
                        Text("বাড়ির হিসাব থেকে টাকা যাবে এবং ফেরত এলে আবার যোগ হবে", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
