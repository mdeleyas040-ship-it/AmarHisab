package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*

@Composable
fun LoansScreen(
    modifier: Modifier,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    onAddLoan: () -> Unit,
    onAddLoanPayment: (LoanAccount) -> Unit,
    onEditLoan: (LoanAccount) -> Unit,
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onAddLending: () -> Unit,
    onAddLendingReturn: (LendingAccount) -> Unit,
    loanInterestTerms: List<LoanInterestTerms>,
    onShowCalculator: () -> Unit = {},
    onShareLoan: (LoanAccount, Boolean) -> Unit = { _, _ -> },
    onShareLending: (LendingAccount, Boolean) -> Unit = { _, _ -> },
    searchQuery: String = ""
) {
    var shareOptionsLoan by remember { mutableStateOf<LoanAccount?>(null) }
    var shareOptionsLending by remember { mutableStateOf<LendingAccount?>(null) }
    var showPaymentHistoryLoan by remember { mutableStateOf<LoanAccount?>(null) }

    if (shareOptionsLoan != null) {
        AlertDialog(onDismissRequest = { shareOptionsLoan = null }, title = { Text("স্টেটমেন্ট শেয়ার করুন") }, text = { Text("${shareOptionsLoan!!.name} ঋণের স্টেটমেন্ট কিভাবে শেয়ার করতে চান?") }, confirmButton = { Button(onClick = { onShareLoan(shareOptionsLoan!!, true); shareOptionsLoan = null }) { Text("PDF ফাইল") } }, dismissButton = { TextButton(onClick = { onShareLoan(shareOptionsLoan!!, false); shareOptionsLoan = null }) { Text("মেসেজ (Text)") } })
    }
    if (shareOptionsLending != null) {
        AlertDialog(onDismissRequest = { shareOptionsLending = null }, title = { Text("স্টেটমেন্ট শেয়ার করুন") }, text = { Text("${shareOptionsLending!!.person}-এর পাওনা স্টেটমেন্ট কিভাবে শেয়ার করতে চান?") }, confirmButton = { Button(onClick = { onShareLending(shareOptionsLending!!, true); shareOptionsLending = null }) { Text("PDF ফাইল") } }, dismissButton = { TextButton(onClick = { onShareLending(shareOptionsLending!!, false); shareOptionsLending = null }) { Text("মেসেজ (Text)") } })
    }

    if (showPaymentHistoryLoan != null) {
        val historyLoan = showPaymentHistoryLoan!!
        val paymentHistory = loanPayments.filter { it.loanId == historyLoan.id }.sortedByDescending { it.date }
        val historyTotal = paymentHistory.sumOf { it.amount }
        AlertDialog(
            onDismissRequest = { showPaymentHistoryLoan = null },
            title = { Text("${historyLoan.name} — পরিশোধের History") },
            text = {
                Column {
                    if (paymentHistory.isEmpty()) Text("কোনো পরিশোধের তথ্য নেই")
                    else {
                        Text("মোট পরিশোধ: ৳${formatMoney(historyTotal)}", fontWeight = FontWeight.Bold, color = IncomeGreen)
                        Spacer(Modifier.height(10.dp))
                        paymentHistory.forEachIndexed { index, payment ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("${index + 1}. ৳${formatMoney(payment.amount)}", fontWeight = FontWeight.Bold, color = IncomeGreen)
                                    Text("তারিখ: ${displayLoanDate(payment.date)}", fontSize = 12.sp)
                                    if (payment.note.isNotBlank()) Text("নোট: ${payment.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPaymentHistoryLoan = null }) { Text("ঠিক আছে") } }
        )
    }

    val filteredLoans = loans.filter { loan -> loan.name.contains(searchQuery, ignoreCase = true) || loan.note.contains(searchQuery, ignoreCase = true) }
    var expandedLoanId by remember { mutableStateOf<Long?>(null) }
    val totalBorrowed = loans.sumOf { it.principal }
    val totalInterest = loans.sumOf { loan -> loanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0 }
    val totalPaid = loanPayments.sumOf { it.amount }
    val totalRemaining = (totalBorrowed + totalInterest - totalPaid).coerceAtLeast(0.0)
    val totalLent = lendings.sumOf { it.amount }
    val totalReturned = lendingReturns.sumOf { it.amount }
    val totalReceivable = (totalLent - totalReturned).coerceAtLeast(0.0)

    LazyColumn(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PremiumLoansDashboard(
                totalBorrowed = totalBorrowed,
                totalPaid = totalPaid,
                totalRemaining = totalRemaining,
                totalLent = totalLent,
                totalReturned = totalReturned,
                totalReceivable = totalReceivable
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddLoan, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("ঋণ যোগ করুন", fontWeight = FontWeight.Bold) } }
                Button(onClick = onAddLending, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("ধার দিন", fontWeight = FontWeight.Bold) } }
            }
        }
        item { Text("আমার নেওয়া ঋণ", fontSize = 19.sp, fontWeight = FontWeight.Bold) }
        if (loans.isEmpty()) {
            item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("এখনও কোনো ঋণ যোগ করা হয়নি।", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(filteredLoans, key = { it.id }) { loan ->
                val paymentHistory = loanPayments.filter { it.loanId == loan.id }.sortedByDescending { it.date }
                val paid = paymentHistory.sumOf { it.amount }
                val interest = loanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0
                val totalPayable = loan.principal + interest
                val remaining = (totalPayable - paid).coerceAtLeast(0.0)
                Card(onClick = { expandedLoanId = if (expandedLoanId == loan.id) null else loan.id }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(CardRadius), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) { Text(loan.name.ifBlank { "ঋণ" }, fontWeight = FontWeight.Bold, fontSize = 17.sp); if (loan.note.isNotBlank()) Text(loan.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (loan.sourceType == "bank") "🏦 ব্যাংক ঋণ" else "👤 ব্যক্তিগত ঋণ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { shareOptionsLoan = loan }, Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) }; Text("৳${formatMoney(if (remaining > 0.0) remaining else paid)}", color = if (remaining > 0) ExpenseRed else IncomeGreen, fontWeight = FontWeight.ExtraBold) }
                                Text(if (remaining > 0) "বাকি আছে" else "পরিশোধিত", fontSize = 11.sp, color = if (remaining > 0) ExpenseRed else IncomeGreen)
                                if (loan.dueDate != null && remaining > 0.0) Surface(shape = RoundedCornerShape(4.dp), color = ExpenseRed.copy(alpha = 0.1f)) { Text("পরিশোধ: ${displayLoanDate(loan.dueDate)}", color = ExpenseRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                            }
                        }
                        if (expandedLoanId == loan.id) {
                            Spacer(Modifier.height(8.dp))
                            LoanInfoRow("মোট ঋণ", loan.principal); LoanInfoRow("মোট সুদ", interest); LoanInfoRow("মোট পরিশোধযোগ্য", totalPayable); LoanInfoRow("পরিশোধ", paid); LoanInfoRow("বাকি", remaining)
                            if (loan.monthlyInstallment > 0) LoanInfoRow("মাসিক কিস্তি", loan.monthlyInstallment)
                            LoanInfoRowText("শুরু", displayLoanDate(loan.startDate)); if (loan.dueDate != null) LoanInfoRowText("পরিশোধের তারিখ", displayLoanDate(loan.dueDate)); if (loan.lastEditedDate.isNotBlank()) LoanInfoRowText("সর্বশেষ Edit", loan.lastEditedDate)

                            if (paymentHistory.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("💳 পরিশোধের History (${paymentHistory.size} বার)", fontSize = 13.sp, fontWeight = FontWeight.Bold); TextButton(onClick = { showPaymentHistoryLoan = loan }) { Text("সব দেখুন") } }
                                paymentHistory.take(3).forEach { payment ->
                                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(9.dp)) { Text("৳${formatMoney(payment.amount)} — ${displayLoanDate(payment.date)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen); if (payment.note.isNotBlank()) Text(payment.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                                }
                                if (paymentHistory.size > 3) TextButton(onClick = { showPaymentHistoryLoan = loan }, Modifier.fillMaxWidth()) { Text("আরও ${paymentHistory.size - 3}টি পরিশোধ দেখুন") }
                            }

                            if (loan.editHistory.isNotEmpty()) { Text("Edit History (${loan.editHistory.size} বার)", fontSize = 13.sp, fontWeight = FontWeight.Bold); loan.editHistory.forEachIndexed { index, date -> Text("Edit ${index + 1}: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                            if (loan.borrowings.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text("📋 নেওয়ার History (${loan.borrowings.size} বার)", fontSize = 13.sp, fontWeight = FontWeight.Bold); loan.borrowings.sortedByDescending { it.date }.forEachIndexed { index, borrowing -> Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("${index + 1}. ৳${formatMoney(borrowing.amount)} — ${displayLoanDate(borrowing.date)}", fontSize = 12.sp, fontWeight = FontWeight.Medium); if (borrowing.note.isNotBlank()) Text(borrowing.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { onEditBorrowing(loan, borrowing) }, Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)) }; IconButton(onClick = { onDeleteBorrowing(loan, borrowing) }, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = ExpenseRed) } } } } }
                            if (loan.note.isNotBlank()) Text("নোট: ${loan.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onEditLoan(loan) }, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") } }
                                OutlinedButton(onClick = { if (remaining > 0.0) onAddLoanPayment(loan) else showPaymentHistoryLoan = loan }, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = if (remaining > 0.0) ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White) else ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (remaining > 0.0) Icons.Default.Add else Icons.Default.CheckCircle, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(if (remaining > 0.0) "পরিশোধ" else "পরিশোধিত") } }
                            }
                        }
                    }
                }
            }
        }
        item { Text("আমি যাদের ধার দিয়েছি", fontSize = 19.sp, fontWeight = FontWeight.Bold) }
        if (lendings.isEmpty()) item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("এখনও কাউকে ধার দেওয়ার হিসাব নেই।", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        else items(lendings, key = { it.id }) { lending ->
            val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
            val remaining = (lending.amount - returned).coerceAtLeast(0.0)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(lending.person.ifBlank { "ব্যক্তি" }, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text("দেওয়ার তারিখ: ${displayLoanDate(lending.date)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); if (lending.dueDate != null && remaining > 0.0) Text("ফেরত পাওয়ার তারিখ: ${displayLoanDate(lending.dueDate)}", fontSize = 11.sp, color = ExpenseRed, fontWeight = FontWeight.Bold) }; Column(horizontalAlignment = Alignment.End) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { shareOptionsLending = lending }, Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) }; Text("৳${formatMoney(remaining)}", color = if (remaining > 0) Blue else IncomeGreen, fontWeight = FontWeight.ExtraBold) } } }
                Spacer(Modifier.height(8.dp)); LoanInfoRow("দেওয়া", lending.amount); LoanInfoRow("ফেরত", returned); LoanInfoRow("পাওনা", remaining); if (lending.note.isNotBlank()) Text("নোট: ${lending.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); Button(onClick = { onAddLendingReturn(lending) }, enabled = remaining > 0.0, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (remaining > 0.0) Icons.Default.Add else Icons.Default.CheckCircle, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (remaining > 0.0) "ধার ফেরত যোগ করুন" else "পুরো টাকা ফেরত") } }
            } }
        }
    }
}
