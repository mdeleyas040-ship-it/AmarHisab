package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyJourneyCard(
    settings: MyJourneySettings,
    totalDebt: Double,
    onSave: (MyJourneySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditor by remember { mutableStateOf(false) }

    val journey = MyJourneyCalculator.journeyLabel(settings.arrivalDate)
    val availableForDebt = (settings.monthlySalary - settings.monthlyExpense).coerceAtLeast(0.0)
    val repaymentDays = MyJourneyCalculator.repaymentDays(
        debt = totalDebt,
        salary = settings.monthlySalary,
        monthlyExpense = settings.monthlyExpense
    )
    val debtFreeDate = repaymentDays?.let { MyJourneyCalculator.debtFreeDate(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF202631), Color(0xFF101319))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "MY JOURNEY",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "মালদ্বীপে আমার যাত্রা",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = { showEditor = true }) {
                    Icon(
                        Icons.Default.EditCalendar,
                        contentDescription = "My Journey সেটিংস",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("মালদ্বীপে আছেন", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                        Text(journey, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                JourneyStatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Payments,
                    title = "বেতন / মাস",
                    value = "৳ ${formatMoney(settings.monthlySalary)}"
                )
                JourneyStatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    title = "বাকি ঋণ",
                    value = "৳ ${formatMoney(totalDebt)}"
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "মাসিক খরচ: ৳ ${formatMoney(settings.monthlyExpense)}  •  ঋণে যাবে: ৳ ${formatMoney(availableForDebt)}",
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(14.dp))

            if (totalDebt <= 0.0) {
                Text(
                    "🎉 কোনো বাকি ঋণ নেই",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (repaymentDays == null) {
                Text(
                    "বেতন ও মাসিক খরচ ঠিক করে দিলে ঋণ পরিশোধের সময় দেখা যাবে।",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            } else {
                Text(
                    "আনুমানিক ${repaymentDays} দিন লাগবে",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                debtFreeDate?.let {
                    Text(
                        "সম্ভাব্য ঋণমুক্তির তারিখ: ${MyJourneyCalculator.formatDate(it)}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = {
                        val ratio = if (availableForDebt > 0.0) {
                            1.0 - (totalDebt / (totalDebt + availableForDebt)).coerceIn(0.0, 1.0)
                        } else 0.0
                        ratio.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
            }
        }
    }

    if (showEditor) {
        MyJourneyEditorDialog(
            initial = settings,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            }
        )
    }
}

@Composable
private fun JourneyStatBox(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = Color.White.copy(alpha = 0.06f)
    ) {
        Column(Modifier.padding(13.dp)) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(7.dp))
            Text(title, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MyJourneyEditorDialog(
    initial: MyJourneySettings,
    onDismiss: () -> Unit,
    onSave: (MyJourneySettings) -> Unit
) {
    var arrivalDate by remember { mutableStateOf(initial.arrivalDate) }
    var salary by remember { mutableStateOf(if (initial.monthlySalary == 0.0) "" else initial.monthlySalary.toString()) }
    var expense by remember { mutableStateOf(if (initial.monthlyExpense == 0.0) "" else initial.monthlyExpense.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Journey সেটআপ", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("আপনার নিয়মিত মাসিক খরচ আনুমানিক দিন।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = arrivalDate,
                    onValueChange = { arrivalDate = it },
                    label = { Text("মালদ্বীপে আসার তারিখ") },
                    placeholder = { Text("01/01/2024") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("মাসিক বেতন (BDT)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Payments, null) }
                )
                OutlinedTextField(
                    value = expense,
                    onValueChange = { expense = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("মাসিক খরচ (BDT)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Payments, null) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val salaryValue = salary.toDoubleOrNull() ?: 0.0
                val expenseValue = expense.toDoubleOrNull() ?: 0.0
                onSave(
                    MyJourneySettings(
                        arrivalDate = arrivalDate.trim(),
                        monthlySalary = salaryValue.coerceAtLeast(0.0),
                        monthlyExpense = expenseValue.coerceAtLeast(0.0)
                    )
                )
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
