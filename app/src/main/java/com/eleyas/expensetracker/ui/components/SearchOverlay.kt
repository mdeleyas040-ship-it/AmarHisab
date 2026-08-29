package com.eleyas.expensetracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney
import kotlinx.coroutines.delay

@Composable
fun SearchOverlay(
    active: Boolean,
    searchScope: Int, // 0: Global, 1: Income, 2: Expense, 4: Loans
    transactions: List<com.eleyas.expensetracker.model.Transaction>,
    loans: List<com.eleyas.expensetracker.model.LoanAccount>,
    lendings: List<com.eleyas.expensetracker.model.LendingAccount>,
    loanPayments: List<com.eleyas.expensetracker.model.LoanPayment>,
    lendingReturns: List<com.eleyas.expensetracker.model.LendingReturn>,
    wallets: List<com.eleyas.expensetracker.model.Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    onEditTransaction: (com.eleyas.expensetracker.model.Transaction) -> Unit,
    onDeleteTransaction: (com.eleyas.expensetracker.model.Transaction) -> Unit,
    onShareResults: (String, Boolean) -> Unit, // Query, isPdf
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filteredTransactions = if (searchQuery.isBlank()) emptyList() else transactions.filter {
        (searchScope == 0 || (searchScope == 1 && it.type == "income") || (searchScope == 2 && (it.type == "expense" || it.type == "home"))) &&
        (it.reason.contains(searchQuery, ignoreCase = true) ||
         it.category.contains(searchQuery, ignoreCase = true) ||
         wallets.firstOrNull { w -> w.id == it.walletId }?.name?.contains(searchQuery, ignoreCase = true) == true)
    }

    val filteredWallets = if (searchQuery.isBlank()) emptyList() else wallets.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.type.contains(searchQuery, ignoreCase = true)
    }

    val filteredLoans = if (searchQuery.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else loans.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true)
    }

    val filteredLendings = if (searchQuery.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else lendings.filter {
        it.person.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true)
    }

    val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type != "income" }.sumOf { it.amount }
    
    LaunchedEffect(active) {
        if (active) {
            delay(300)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
        }
    }

    AnimatedVisibility(
        visible = active,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            placeholder = { Text(if (searchScope == 0) "সবকিছু খুঁজুন (নাম, ক্যাটাগরি, ব্যাংক...)" else "কী খুঁজছেন?") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = {
                                IconButton(onClick = onClose) { Icon(Icons.Default.Clear, null) }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        
                        if (searchQuery.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("আয়: ৳${formatMoney(totalIncome)}", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(12.dp))
                                    Text("খরচ: ৳${formatMoney(totalExpense)}", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    TextButton(onClick = { onShareResults(searchQuery, false) }) { Text("Share Text", fontSize = 11.sp) }
                                    TextButton(onClick = { onShareResults(searchQuery, true) }) { Text("PDF", fontSize = 11.sp) }
                                }
                            }
                        }
                    }
                }

                // Results List
                if (searchQuery.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions) { trans ->
                            val wallet = wallets.firstOrNull { it.id == trans.walletId }
                            TransactionCard(
                                transaction = trans,
                                usdToBdt = usdToBdt,
                                usdToMvr = usdToMvr,
                                walletName = wallet?.name ?: "",
                                onEdit = {
                                    onEditTransaction(it)
                                    onClose()
                                },
                                onDelete = {
                                    onDeleteTransaction(it)
                                    onClose()
                                }
                            )
                        }
                        
                        if (filteredWallets.isNotEmpty()) {
                            item { Text("ব্যাংক / অ্যাকাউন্ট", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredWallets) { wallet ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(wallet.color.toLong() and 0xFFFFFFFFL))) {
                                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(wallet.name, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text(wallet.type, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text(
                                            "লেনদেন: ${filteredTransactions.count { it.walletId == wallet.id }}",
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredLoans.isNotEmpty()) {
                            item { Text("ঋণ (Loans)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredLoans) { loan ->
                                val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                                val remaining = loan.principal - paid
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(loan.name, fontWeight = FontWeight.Bold)
                                            Text("বাকি: ৳${formatMoney(remaining)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (filteredLendings.isNotEmpty()) {
                            item { Text("পাওনা (Lendings)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredLendings) { lending ->
                                val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
                                val remaining = lending.amount - returned
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(lending.person, fontWeight = FontWeight.Bold)
                                            Text("পাওনা: ৳${formatMoney(remaining)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("সার্চ করার জন্য কিছু লিখুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
