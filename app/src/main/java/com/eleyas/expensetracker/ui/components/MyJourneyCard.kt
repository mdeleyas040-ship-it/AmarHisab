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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.viewmodel.MainViewModel

@Composable
fun MyJourneyCard(
    settings: MyJourneySettings,
    totalDebt: Double,
    transactions: List<com.eleyas.expensetracker.model.Transaction> = emptyList(),
    onSave: (MyJourneySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditor by remember { mutableStateOf(false) }

    // Use the same Activity-scoped MainViewModel as the rest of the app.
    // This keeps income/expense fully automatic without changing HomeScreen.kt.
    val mainViewModel: MainViewModel = viewModel()
    val liveTransactions = mainViewModel.transactions
    val effectiveTransactions = if (liveTransactions.isNotEmpty()) liveTransactions else transactions

    val averageIncome = remember(effectiveTransactions) {
        MyJourneyCalculator.averageMonthlyIncome(effectiveTransactions, months = 3)
    }
    val averageExpense = remember(effectiveTransactions) {
        MyJourneyCalculator.averageMonthlyExpense(effectiveTransactions, months = 3)
    }
    val availableForDebt = (averageIncome - averageExpense).coerceAtLeast(0.0)
    val repaymentDays = MyJourneyCalculator.repaymentDays(
        debt = totalDebt,
        salary = averageIncome,
        monthlyExpense = averageExpense
    )
    val debtFreeDate = repaymentDays?.let { MyJourneyCalculator.debtFreeDate(it) }
    val journey = MyJourneyCalculator.journeyLabel(settings.arrivalDate)

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
                    title = "গড় আয় / মাস",
                    value = "৳ ${formatMoney(averageIncome)}"
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
                "অটো মাসিক খরচ (৩ মাসের গড়): ৳ ${formatMoney(averageExpense)}",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "ঋণে যাবে: ৳ ${formatMoney(availableForDebt)} / মাস",
                color = Color.White.copy(alpha = 0.55f),
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
                    "বর্তমান আয়-খরচ অনুযায়ী ঋণ পরিশোধের জন্য অতিরিক্ত টাকা থাকছে না।",
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Journey সেটআপ", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "শুধু মালদ্বীপে আসার তারিখ দিন। আয় ও খরচ আপনার এন্ট্রি থেকে অ্যাপ নিজে হিসাব করবে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = arrivalDate,
                    onValueChange = { arrivalDate = it },
                    label = { Text("মালদ্বীপে আসার তারিখ") },
                    placeholder = { Text("01/01/2024") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        "গড় আয় + গড় খরচ: গত ৩ মাসের transaction থেকে অটো হিসাব হবে।",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    MyJourneySettings(
                        arrivalDate = arrivalDate.trim(),
                        // Legacy fields are kept only for backward compatibility.
                        monthlySalary = 0.0,
                        monthlyExpense = 0.0
                    )
                )
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
