package com.eleyas.expensetracker.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.SavingsGoal
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SavingsScreen(
    goals: List<SavingsGoal>,
    onBack: () -> Unit,
    onAddGoal: (String, Double, String, String) -> Unit,
    onAddContribution: (Long, Double) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var contributionGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(60_000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান")
                }
                Text("সেভিংস লক্ষ্য", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "নতুন লক্ষ্য")
                }
            }
            Text("ডেডলাইন পর্যন্ত প্রতিদিন বা প্রতি সপ্তাহের টার্গেট", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            if (goals.isEmpty()) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Savings, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("এখনও কোনো সেভিংস লক্ষ্য নেই", fontWeight = FontWeight.Bold)
                        Text("নতুন লক্ষ্য যোগ করে কাউন্টডাউন শুরু করুন")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(goals, key = { it.id }) { goal ->
                        SavingsGoalCard(goal, now, { contributionGoal = goal }, { onDeleteGoal(goal.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, date, frequency ->
                onAddGoal(name, amount, date, frequency)
                showAddDialog = false
            }
        )
    }
    contributionGoal?.let { goal ->
        ContributionDialog(
            goal = goal,
            onDismiss = { contributionGoal = null },
            onSave = { amount ->
                onAddContribution(goal.id, amount)
                contributionGoal = null
            }
        )
    }
}

@Composable
private fun SavingsGoalCard(goal: SavingsGoal, now: Long, onContribute: () -> Unit, onDelete: () -> Unit) {
    val parser = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val deadline = runCatching { parser.parse(goal.targetDate)?.time ?: now }.getOrDefault(now)
    val days = TimeUnit.MILLISECONDS.toDays((deadline - now).coerceAtLeast(0L))
    val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
    val periods = if (goal.frequency == "weekly") (days / 7.0).coerceAtLeast(1.0) else (days + 1).toDouble()
    val perPeriod = if (remaining == 0.0) 0.0 else remaining / periods
    val progress = (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Savings, null, tint = MaterialTheme.colorScheme.primary)
                Text(goal.name, Modifier.padding(start = 10.dp).weight(1f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "মুছুন") }
            }
            Text("৳${formatMoney(goal.savedAmount)} / ৳${formatMoney(goal.targetAmount)}")
            androidx.compose.material3.LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().padding(vertical = 10.dp))
            Text(if (remaining == 0.0) "লক্ষ্য পূর্ণ হয়েছে 🎉" else "বাকি $days দিন • ${if (goal.frequency == "weekly") "সাপ্তাহিক" else "দৈনিক"} ৳${formatMoney(perPeriod)} জমাতে হবে", color = if (remaining == 0.0) Color(0xFF16803C) else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text("ডেডলাইন: ${goal.targetDate}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onContribute, Modifier.fillMaxWidth()) { Text("আজকের জমা যোগ করুন") }
        }
    }
}

@Composable
private fun AddSavingsGoalDialog(onDismiss: () -> Unit, onSave: (String, Double, String, String) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var frequency by remember { mutableStateOf("daily") }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন সেভিংস লক্ষ্য") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("লক্ষ্যের নাম") }, singleLine = true)
                OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("মোট লক্ষ্য (৳)") }, singleLine = true)
                OutlinedButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }, Modifier.fillMaxWidth()) { Text("ডেডলাইন: $date") }
                androidx.compose.foundation.layout.Box {
                    OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) {
                        Text(if (frequency == "weekly") "প্রতি সপ্তাহে" else "প্রতিদিন")
                    }
                    DropdownMenu(expanded, { expanded = false }) {
                        DropdownMenuItem({ Text("প্রতিদিন") }, { frequency = "daily"; expanded = false })
                        DropdownMenuItem({ Text("প্রতি সপ্তাহে") }, { frequency = "weekly"; expanded = false })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { amount.toDoubleOrNull()?.takeIf { it > 0 }?.let { value -> if (name.isNotBlank()) onSave(name, value, date, frequency) } }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
private fun ContributionDialog(goal: SavingsGoal, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${goal.name}-এ জমা") },
        text = { OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("জমার পরিমাণ (৳)") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { amount.toDoubleOrNull()?.takeIf { it > 0 }?.let(onSave) }) { Text("যোগ করুন") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
