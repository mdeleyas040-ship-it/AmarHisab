package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PersonalLoanPaymentDialog(
    loan: LoanAccount,
    payments: List<LoanPayment>,
    onDismiss: () -> Unit,
    onSavePayment: (Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var showPaymentForm by remember { mutableStateOf(false) }
    var showPaymentConfirmation by remember { mutableStateOf(false) }
    var pendingPaymentAmount by remember { mutableStateOf(0.0) }
    var amount by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var note by remember { mutableStateOf("") }

    val loanPayments = remember(payments, loan.id) {
        payments.filter { it.loanId == loan.id }.sortedByDescending { it.id }
    }
    val paidAmount = loanPayments.sumOf { it.amount }
    val totalAmount = loan.principal.coerceAtLeast(0.0)
    val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
    val progress = if (totalAmount > 0.0) (paidAmount / totalAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val progressPercent = (progress * 100).roundToInt()
    val isCompleted = remainingAmount <= 0.01

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(scheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(27.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("ব্যক্তিগত ঋণ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = scheme.primary)
                            Text(loan.name, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Loan overview & payment history", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                    }
                }

                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isCompleted) scheme.tertiaryContainer else scheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(if (isCompleted) "ঋণ সম্পূর্ণ পরিশোধ" else "বর্তমান বাকি", fontSize = 12.sp,
                                        color = if (isCompleted) scheme.onTertiaryContainer else scheme.onPrimaryContainer)
                                    Spacer(Modifier.height(3.dp))
                                    Text("৳${money(remainingAmount)}", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (isCompleted) scheme.onTertiaryContainer else scheme.onPrimaryContainer)
                                }
                                Box(Modifier.size(58.dp).clip(CircleShape).background(if (isCompleted) scheme.tertiary else scheme.primary), contentAlignment = Alignment.Center) {
                                    Icon(if (isCompleted) Icons.Default.Check else Icons.Default.AccountBalanceWallet, contentDescription = null,
                                        tint = if (isCompleted) scheme.onTertiary else scheme.onPrimary)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পরিশোধের অগ্রগতি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$progressPercent%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(7.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), trackColor = scheme.surface.copy(alpha = 0.35f))
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanSummaryCard("মোট ঋণ", "৳${money(totalAmount)}", Icons.Default.AccountBalance, Modifier.weight(1f))
                        LoanSummaryCard("পরিশোধ", "৳${money(paidAmount)}", Icons.Default.Payments, Modifier.weight(1f))
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanInfoCard(Icons.Default.Event, "শুরু", loan.startDate, Modifier.weight(1f))
                        LoanInfoCard(Icons.Default.EventAvailable, "পরিশোধের তারিখ", loan.dueDate ?: "নির্ধারিত নয়", Modifier.weight(1f))
                    }
                }

                if (loan.note.isNotBlank()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
                                Box(Modifier.size(38.dp).clip(CircleShape).background(scheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Notes, contentDescription = null, tint = scheme.primary)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("নোট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.primary)
                                    Spacer(Modifier.height(3.dp))
                                    Text(loan.note, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            amount = ""
                            note = ""
                            paymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            showPaymentForm = !showPaymentForm
                        },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)
                    ) {
                        Icon(if (showPaymentForm) Icons.Default.KeyboardArrowUp else Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (showPaymentForm) "পেমেন্ট ফর্ম বন্ধ করুন" else "নতুন পেমেন্ট যোগ করুন", fontWeight = FontWeight.Bold)
                    }
                }

                if (showPaymentForm) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("নতুন পেমেন্ট", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                Text("এই ব্যক্তিগত ঋণের পেমেন্ট রেকর্ড করুন", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                                Spacer(Modifier.height(13.dp))
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("পরিশোধের টাকা") },
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                    singleLine = true
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val calendar = Calendar.getInstance()
                                        DatePickerDialog(context, { _, year, month, day -> paymentDate = "%02d/%02d/%04d".format(day, month + 1, year) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(paymentDate)
                                }
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("নোট") },
                                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                                    maxLines = 3
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val value = amount.replace(",", "").trim().toDoubleOrNull()
                                        pendingPaymentAmount = value ?: 0.0
                                        showPaymentConfirmation = true
                                    },
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("পেমেন্ট সংরক্ষণ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("পেমেন্ট হিস্টোরি", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text("${loanPayments.size}টি পেমেন্ট", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.History, contentDescription = null, tint = scheme.primary)
                    }
                }

                if (loanPayments.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(36.dp), tint = scheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text("এখনও কোনো পেমেন্ট নেই", fontWeight = FontWeight.Bold)
                                Text("নতুন পেমেন্ট যোগ করলে এখানে দেখা যাবে", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                items(loanPayments, key = { it.id }) { payment -> PaymentHistoryCard(payment) }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }

        if (showPaymentConfirmation) {
            val isInvalidAmount = pendingPaymentAmount <= 0.0
            val isOverpayment = pendingPaymentAmount > remainingAmount + 0.01
            WarningDialog(
                title = when {
                    isInvalidAmount -> "টাকার পরিমাণ সঠিক নয়"
                    isOverpayment -> "বেশি টাকা পরিশোধ"
                    else -> "পেমেন্ট নিশ্চিত করুন"
                },
                message = when {
                    isInvalidAmount -> "অনুগ্রহ করে ০ টাকার বেশি একটি সঠিক পেমেন্টের পরিমাণ লিখুন।"
                    isOverpayment -> "আপনি ৳${money(pendingPaymentAmount)} পরিশোধ করতে যাচ্ছেন, কিন্তু বর্তমানে বাকি আছে ৳${money(remainingAmount)}। এই পেমেন্ট সংরক্ষণ করবেন?"
                    else -> "৳${money(pendingPaymentAmount)} পেমেন্ট হিসেবে সংরক্ষণ করা হবে।\nতারিখ: $paymentDate\nপরিশোধের পর বাকি থাকবে ৳${money((remainingAmount - pendingPaymentAmount).coerceAtLeast(0.0))}।"
                },
                confirmText = if (isInvalidAmount) "ঠিক আছে" else "নিশ্চিত করুন",
                dismissText = "বাতিল",
                showDismissButton = !isInvalidAmount,
                onDismiss = { showPaymentConfirmation = false },
                onConfirm = {
                    if (!isInvalidAmount) {
                        onSavePayment(pendingPaymentAmount, paymentDate, note.trim())
                        showPaymentForm = false
                    }
                    showPaymentConfirmation = false
                }
            )
        }
    }
}

@Composable
private fun LoanSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun LoanInfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(7.dp))
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: LoanPayment) {
    val scheme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(scheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = scheme.primary)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text("৳${money(payment.amount)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(2.dp))
                Text(payment.date, fontSize = 11.sp, color = scheme.onSurfaceVariant)
                if (payment.note.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(payment.note, fontSize = 11.sp, color = scheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(21.dp))
        }
    }
}

private fun money(value: Double): String = String.format(Locale.US, "%,.2f", value).removeSuffix(".00")
