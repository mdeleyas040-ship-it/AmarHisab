package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.FinancialMilestone
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object FinancialMilestoneTimeline {
    fun sorted(milestones: List<FinancialMilestone>): List<FinancialMilestone> =
        milestones.sortedWith(
            compareByDescending<FinancialMilestone> {
                parseDate(it.date)?.time ?: Long.MIN_VALUE
            }.thenByDescending { it.id }
        )

    fun isValidDate(value: String): Boolean = parseDate(value) != null

    private fun parseDate(value: String): Date? = try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        format.parse(value)?.takeIf { format.format(it) == value }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun FinancialMemoryTimeline(
    milestones: List<FinancialMilestone>,
    transactions: List<Transaction>,
    onSave: (FinancialMilestone) -> Unit,
    onDelete: (Long) -> Unit
) {
    var editingMilestone by remember { mutableStateOf<FinancialMilestone?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val sortedMilestones = remember(milestones) { FinancialMilestoneTimeline.sorted(milestones) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = Color(0xFF00695C).copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Flag, null, tint = Color(0xFF00695C))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("ফিন্যান্সিয়াল মেমোরি", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "টাকার সাথে জীবনের বড় মুহূর্তগুলো",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    editingMilestone = null
                    showEditor = true
                }) {
                    Icon(Icons.Default.Add, "নতুন মাইলস্টোন যোগ করুন")
                }
            }

            Spacer(Modifier.height(14.dp))
            if (sortedMilestones.isEmpty()) {
                Text(
                    "এখনও কোনো আর্থিক মাইলস্টোন নেই। প্রথম ল্যাপটপ বা DPS সম্পন্ন হওয়ার স্মৃতি যোগ করুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            } else {
                sortedMilestones.forEachIndexed { index, milestone ->
                    FinancialMilestoneItem(
                        milestone = milestone,
                        isLast = index == sortedMilestones.lastIndex,
                        onEdit = {
                            editingMilestone = milestone
                            showEditor = true
                        }
                    )
                }
            }
        }
    }

    if (showEditor) {
        FinancialMilestoneEditor(
            initial = editingMilestone,
            transactions = transactions,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            },
            onDelete = editingMilestone?.let { milestone ->
                {
                    onDelete(milestone.id)
                    showEditor = false
                }
            }
        )
    }
}

@Composable
private fun FinancialMilestoneItem(
    milestone: FinancialMilestone,
    isLast: Boolean,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(14.dp), shape = CircleShape, color = Color(0xFF00695C)) {}
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(62.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(bottom = if (isLast) 0.dp else 10.dp)) {
            Text(milestone.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text(milestone.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (milestone.note.isNotBlank()) {
                Text(
                    milestone.note,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${milestone.currency} ${formatMoney(milestone.amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00695C)
            )
        }
    }
}

@Composable
private fun FinancialMilestoneEditor(
    initial: FinancialMilestone?,
    transactions: List<Transaction>,
    onDismiss: () -> Unit,
    onSave: (FinancialMilestone) -> Unit,
    onDelete: (() -> Unit)?
) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var currency by remember(initial) { mutableStateOf(initial?.currency ?: "BDT") }
    var date by remember(initial) { mutableStateOf(initial?.date.orEmpty()) }
    var note by remember(initial) { mutableStateOf(initial?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val linkedTransaction = initial?.linkedTransactionId?.let { id -> transactions.firstOrNull { it.id == id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন আর্থিক স্মৃতি" else "আর্থিক স্মৃতি এডিট") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("মুহূর্তের নাম") },
                    placeholder = { Text("প্রথম ল্যাপটপ কিনেছি") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.uppercase(Locale.getDefault()) },
                    label = { Text("কারেন্সি") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ (dd/MM/yyyy)") },
                    placeholder = { Text("01/09/2026") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিস্তারিত (ঐচ্ছিক)") }
                )
                linkedTransaction?.let {
                    Text(
                        "যুক্ত লেনদেন: ${it.reason}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                error = when {
                    title.isBlank() -> "মুহূর্তের নাম দিন।"
                    parsedAmount == null || parsedAmount <= 0.0 -> "০-এর বেশি সঠিক টাকার পরিমাণ দিন।"
                    currency.isBlank() -> "কারেন্সি দিন।"
                    !FinancialMilestoneTimeline.isValidDate(date.trim()) -> "সঠিক তারিখ দিন (dd/MM/yyyy)।"
                    else -> null
                }
                if (error == null) {
                    onSave(
                        FinancialMilestone(
                            id = initial?.id ?: System.currentTimeMillis(),
                            title = title,
                            amount = parsedAmount!!,
                            currency = currency,
                            date = date.trim(),
                            note = note,
                            linkedTransactionId = initial?.linkedTransactionId
                        )
                    )
                }
            }) {
                Text("সেভ")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "মুছে ফেলুন", tint = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("বাতিল") }
            }
        }
    )
}
