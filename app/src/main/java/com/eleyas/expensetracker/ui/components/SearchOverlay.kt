package com.eleyas.expensetracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.model.Wallet
import com.eleyas.expensetracker.util.displayLoanDate
import com.eleyas.expensetracker.util.formatMoney
import kotlinx.coroutines.delay

@Composable
fun SearchOverlay(
    active: Boolean,
    searchScope: Int,
    transactions: List<Transaction>,
    loans: List<LoanAccount>,
    lendings: List<LendingAccount>,
    loanPayments: List<LoanPayment>,
    lendingReturns: List<LendingReturn>,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onShareResults: (String, Boolean) -> Unit,
    onOpenLoan: (LoanAccount) -> Unit = { LoanNavigationState.openLoan(it.id) },
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLoan by remember { mutableStateOf<LoanAccount?>(null) }
    val focusRequester = remember { FocusRequester() }

    val filteredTransactions = if (searchQuery.isBlank()) emptyList() else transactions.filter {
        (searchScope == 0 || (searchScope == 1 && it.type == "income") || (searchScope == 2 && (it.type == "expense" || it.type == "home"))) &&
            (it.reason.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                wallets.firstOrNull { w -> w.id == it.walletId }?.name?.contains(searchQuery, ignoreCase = true) == true)
    }

    val filteredWallets = if (query.isBlank()) emptyList() else wallets.filter {
        it.name.contains(query, true) || it.type.contains(query, true)
    }

    val filteredLoans = if (query.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else loans.filter {
        it.name.contains(query, true) || it.note.contains(query, true)
    }

    val filteredLendings = if (query.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else lendings.filter {
        it.person.contains(query, true) || it.note.contains(query, true)
    }

    val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type != "income" }.sumOf { it.amount }

    LaunchedEffect(active) {
        if (active) {
            delay(250)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
            selectedLoan = null
        }
    }

    if (selectedLoan != null) {
        val loan = selectedLoan!!
        val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
        val remaining = (loan.principal - paid).coerceAtLeast(0.0)
        val progress = if (loan.principal > 0.0) (paid / loan.principal).coerceIn(0.0, 1.0).toFloat() else 0f

        Dialog(onDismissRequest = { selectedLoan = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15181E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color(0xFF26364A), Color(0xFF171B22))))
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("ঋণের বিস্তারিত", color = Color(0xFF8FB7FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(5.dp))
                                    Text(loan.name.ifBlank { "ঋণ" }, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        if (loan.sourceType == "bank") "🏦 ব্যাংক ঋণ" else "👤 ব্যক্তিগত ঋণ",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }
                                Surface(shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = 0.08f)) {
                                    Icon(
                                        if (loan.sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF8FB7FF),
                                        modifier = Modifier.padding(12.dp).size(24.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Text("বাকি ঋণ", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Spacer(Modifier.height(3.dp))
                            Text("৳${formatMoney(remaining)}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                                color = Color(0xFF35D07F),
                                trackColor = Color.White.copy(alpha = 0.09f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "৳${formatMoney(paid)} পরিশোধ হয়েছে • ৳${formatMoney(remaining)} বাকি",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SearchLoanMetric("মূল ঋণ", "৳${formatMoney(loan.principal)}", Modifier.weight(1f))
                            SearchLoanMetric("পরিশোধ", "৳${formatMoney(paid)}", Modifier.weight(1f))
                            SearchLoanMetric("অবশিষ্ট", "৳${formatMoney(remaining)}", Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(14.dp))
                        SearchLoanInfoRow("শুরু", displayLoanDate(loan.startDate), Icons.Default.CalendarMonth)
                        if (loan.dueDate != null) SearchLoanInfoRow("পরিশোধের তারিখ", displayLoanDate(loan.dueDate), Icons.Default.Event)
                        if (loan.monthlyInstallment > 0.0) SearchLoanInfoRow("মাসিক কিস্তি", "৳${formatMoney(loan.monthlyInstallment)}", Icons.Default.Payments)
                        if (loan.note.isNotBlank()) SearchLoanInfoRow("নোট", loan.note, Icons.Default.Notes)

                        if (loanPayments.any { it.loanId == loan.id }) {
                            Spacer(Modifier.height(14.dp))
                            Text("পরিশোধের ইতিহাস", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            loanPayments.filter { it.loanId == loan.id }
                                .sortedByDescending { it.date }
                                .take(4)
                                .forEach { payment ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF35D07F), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(7.dp))
                                            Text(displayLoanDate(payment.date), color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
                                        }
                                        Text("৳${formatMoney(payment.amount)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                        }

                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { selectedLoan = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) { Text("বন্ধ") }
                            Button(
                                onClick = {
                                    val loanToOpen = loan
                                    selectedLoan = null
                                    onOpenLoan(loanToOpen)
                                    onClose()
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35D07F), contentColor = Color(0xFF0D1711))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("ঋণ খুলুন", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = active,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            placeholder = { Text("নাম, খরচ, আয়, ধার, পাওনা, তারিখ... খুঁজুন") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Clear, null) } },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        if (searchQuery.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("সার্চ ফলাফল", fontWeight = FontWeight.Bold)
                                        Text(
                                            "${filteredTransactions.size}টি লেনদেন • ${filteredLoans.size + filteredLendings.size}টি হিসাব",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("আয় ৳${formatMoney(totalIncome)}", color = Color(0xFF22C55E), fontSize = 11.sp)
                                        Text("খরচ ৳${formatMoney(totalExpense)}", color = Color(0xFFEF4444), fontSize = 11.sp)
                                        Text("নেট ৳${formatMoney(net)}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onShareResults(query, false) }) { Text("শেয়ার") }
                                TextButton(onClick = { onShareResults(query, true) }) { Text("PDF") }
                            }
                        }
                    }
                }

                if (searchQuery.isNotEmpty()) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { trans ->
                            val wallet = wallets.firstOrNull { it.id == trans.walletId }
                            TransactionCard(
                                transaction = trans,
                                usdToBdt = usdToBdt,
                                usdToMvr = usdToMvr,
                                walletName = wallet?.name ?: "",
                                onEdit = { onEditTransaction(it); onClose() },
                                onDelete = { onDeleteTransaction(it); onClose() }
                            )
                        }

                        if (filteredWallets.isNotEmpty()) {
                            item { Text("ব্যাংক / অ্যাকাউন্ট", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredWallets) { wallet ->
                                Card(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(wallet.color.toLong() and 0xFFFFFFFFL))
                                ) {
                                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(wallet.name, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text(wallet.type, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text("লেনদেন: ${filteredTransactions.count { it.walletId == wallet.id }}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (filteredLoans.isNotEmpty()) {
                            item { Text("ঋণ (Loans)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredLoans, key = { it.id }) { loan ->
                                val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                                val remaining = (loan.principal - paid).coerceAtLeast(0.0)
                                Card(
                                    onClick = { selectedLoan = loan },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E7EB)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 18.dp, vertical = 16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(loan.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF20242B))
                                            Spacer(Modifier.height(3.dp))
                                            Text("বাকি: ৳${formatMoney(remaining)}", fontSize = 12.sp, color = Color(0xFF5F6670))
                                        }
                                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD7DBE2)) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF343A40), modifier = Modifier.padding(9.dp).size(17.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredLendings.isNotEmpty()) {
                            item { Text("পাওনা", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)) }
                            items(filteredLendings, key = { "lending_${it.id}" }) { lending ->
                                val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
                                val remaining = (lending.amount - returned).coerceAtLeast(0.0)
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(lending.person, fontWeight = FontWeight.Bold)
                                        Text("পাওনা: ৳${formatMoney(remaining)}", fontSize = 12.sp)
                                        if (lending.note.isNotBlank()) Text(lending.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (filteredTransactions.isEmpty() && filteredLoans.isEmpty() && filteredLendings.isEmpty() && filteredWallets.isEmpty()) {
                            item {
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                    Text("কোনো মিল পাওয়া যায়নি। অন্য নাম, ক্যাটাগরি বা তারিখ দিয়ে চেষ্টা করুন।", Modifier.padding(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoanMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.055f)) {
        Column(Modifier.padding(11.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.52f), fontSize = 9.sp)
            Spacer(Modifier.height(3.dp))
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SearchLoanInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.055f)) {
            Icon(icon, null, tint = Color(0xFF8FB7FF), modifier = Modifier.padding(7.dp).size(16.dp))
        }
        Spacer(Modifier.width(9.dp))
        Text(label, color = Color.White.copy(alpha = 0.52f), fontSize = 11.sp, modifier = Modifier.width(110.dp))
        Text(value, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}
