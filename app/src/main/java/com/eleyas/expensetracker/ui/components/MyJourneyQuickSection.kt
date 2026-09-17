package com.eleyas.expensetracker.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun MyJourneyQuickSection(
    settings: MyJourneySettings,
    totalDebt: Double,
    transactions: List<Transaction>,
    onEdit: (MyJourneySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    val mainViewModel: MainViewModel = viewModel()
    val liveTransactions = mainViewModel.transactions
    val dataTransactions = if (liveTransactions.isNotEmpty()) liveTransactions else transactions
    val usdToBdt = mainViewModel.usdToBdt
    val usdToMvr = mainViewModel.usdToMvr

    val amountInBdt: (Transaction) -> Double = { transaction ->
        when (transaction.currency.trim().uppercase(Locale.getDefault())) {
            "BDT", "৳" -> transaction.amount
            "USD", "$" -> if (usdToBdt > 0.0) transaction.amount * usdToBdt else transaction.amount
            "MVR", "RF", "RUFIYAA" -> if (usdToBdt > 0.0 && usdToMvr > 0.0) {
                transaction.amount * (usdToBdt / usdToMvr)
            } else transaction.amount
            else -> transaction.amount
        }.coerceAtLeast(0.0)
    }

    val journey = MyJourneyCalculator.journeyLabel(settings.arrivalDate)
    val averageIncome = MyJourneyCalculator.averageMonthlyIncome(dataTransactions, 3, amountConverter = amountInBdt)
    val averageExpense = MyJourneyCalculator.averageMonthlyExpense(dataTransactions, 3, amountConverter = amountInBdt)
    val totalIncomeSinceArrival = MyJourneyCalculator.totalIncomeSinceArrival(
        dataTransactions,
        settings.arrivalDate,
        amountConverter = amountInBdt
    )
    val monthlyDebtCapacity = (averageIncome - averageExpense).coerceAtLeast(0.0)
    val repaymentDays = MyJourneyCalculator.repaymentDays(totalDebt, averageIncome, averageExpense)
    val debtFreeDate = repaymentDays?.let { MyJourneyCalculator.debtFreeDate(it) }

    Surface(
        modifier = modifier.fillMaxWidth().clickable { showDetails = true },
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF202631), Color(0xFF11151B))),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("🇲🇻 মালদ্বীপ আমার যাত্রা", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (settings.arrivalDate.isBlank()) "আসার তারিখ সেট করুন" else "মালদ্বীপে আছেন • $journey",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 11.sp
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "বিস্তারিত", tint = Color.White.copy(alpha = 0.65f))
        }
    }

    if (showDetails) {
        Dialog(onDismissRequest = { showDetails = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF24262D),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(15.dp), color = Color(0xFF3A404B)) {
                            Box(contentAlignment = Alignment.Center) { Text("🇲🇻", fontSize = 23.sp) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("মালদ্বীপ আমার যাত্রা", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text("আপনার প্রবাস জীবনের হিসাব", color = Color.White.copy(alpha = 0.58f), fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFF30343D)) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মালদ্বীপে আছেন", color = Color.White.copy(alpha = 0.60f), fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(journey, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        JourneyMetricCard(Modifier.weight(1f), "গড় আয়", "৳${formatMoney(averageIncome)}")
                        JourneyMetricCard(Modifier.weight(1f), "গড় খরচ", "৳${formatMoney(averageExpense)}")
                        JourneyMetricCard(Modifier.weight(1f), "ঋণে যাবে", "৳${formatMoney(monthlyDebtCapacity)}", true)
                    }

                    Spacer(Modifier.height(14.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFF10392B)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = Color(0xFF1A5B43)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF5DE49A), modifier = Modifier.size(23.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("মালদ্বীপে এ পর্যন্ত মোট আয়", color = Color.White.copy(alpha = 0.62f), fontSize = 10.sp)
                                Spacer(Modifier.height(3.dp))
                                Text("৳${formatMoney(totalIncomeSinceArrival)}", color = Color(0xFF5DE49A), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("আসার তারিখ থেকে সব আয় transaction", color = Color.White.copy(alpha = 0.45f), fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFF1D2026)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("মোট বাকি ঋণ", color = Color.White.copy(alpha = 0.60f), fontSize = 11.sp)
                            Spacer(Modifier.height(3.dp))
                            Text("৳${formatMoney(totalDebt)}", color = Color(0xFFFF5B61), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("আনুমানিক সময়", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                                    Text(repaymentDays?.let { "$it দিন" } ?: "বর্তমান হিসাবে সম্ভব নয়", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("প্রতি মাসে ঋণে", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                                    Text("৳${formatMoney(monthlyDebtCapacity)}", color = Color(0xFF45C77A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    debtFreeDate?.let {
                        Spacer(Modifier.height(12.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color(0xFF193427)) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF58D98C), modifier = Modifier.size(23.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("সম্ভাব্য ঋণমুক্তির তারিখ", color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
                                    Text(MyJourneyCalculator.formatDate(it), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("আয় ও খরচ গত ৩ মাসের transaction থেকে অটো হিসাব করা হচ্ছে।", color = Color.White.copy(alpha = 0.48f), fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showDetails = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.72f))) {
                            Text("বন্ধ", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF2E9E69)) {
                            TextButton(
                                onClick = { showEditor = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("তারিখ সম্পাদনা", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        MyJourneyEditorDialog(
            initial = settings,
            onDismiss = { showEditor = false },
            onSave = { updatedSettings ->
                onEdit(updatedSettings)
                showEditor = false
                showDetails = false
            }
        )
    }
}

@Composable
private fun MyJourneyEditorDialog(
    initial: MyJourneySettings,
    onDismiss: () -> Unit,
    onSave: (MyJourneySettings) -> Unit
) {
    var arrivalDate by remember(initial.arrivalDate) { mutableStateOf(initial.arrivalDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("মালদ্বীপে আসার তারিখ", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "আপনার মালদ্বীপে আসার সঠিক তারিখ দিন। এই তারিখ থেকেই যাত্রার সময় ও মোট আয় হিসাব হবে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = arrivalDate,
                    onValueChange = { arrivalDate = it },
                    label = { Text("তারিখ (DD/MM/YYYY)") },
                    placeholder = { Text("01/01/2026") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (arrivalDate.trim().isNotEmpty()) {
                        onSave(
                            MyJourneySettings(
                                arrivalDate = arrivalDate.trim(),
                                monthlySalary = 0.0,
                                monthlyExpense = 0.0
                            )
                        )
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E9E69))
            ) {
                Text("তারিখ সংরক্ষণ", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun JourneyMetricCard(modifier: Modifier = Modifier, label: String, value: String, emphasized: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = if (emphasized) Color(0xFF173A29) else Color(0xFF30343D)) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 11.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.58f), fontSize = 9.sp)
            Spacer(Modifier.height(3.dp))
            Text(value, color = if (emphasized) Color(0xFF55D58A) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}
