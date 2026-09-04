package com.eleyas.expensetracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    searchScope: Int,
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
    onShareResults: (String, Boolean) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("সব") }
    val focusRequester = remember { FocusRequester() }

    val query = searchQuery.trim()

    val filteredTransactions = remember(query, selectedFilter, transactions, wallets, searchScope) {
        if (query.isBlank()) emptyList() else transactions.filter { t ->
            val walletName = wallets.firstOrNull { it.id == t.walletId }?.name.orEmpty()
            val searchable = listOf(
                t.reason, t.category, t.date, t.type, t.currency,
                walletName, t.addedByName.orEmpty()
            ).joinToString(" ")

            val matchesText = searchable.contains(query, ignoreCase = true)
            val matchesScope = when (searchScope) {
                1 -> t.type == "income"
                2 -> t.type == "expense" || t.type == "home"
                else -> true
            }
            val matchesFilter = when (selectedFilter) {
                "আয়" -> t.type == "income"
                "খরচ" -> t.type == "expense" || t.type == "home"
                "ধার" -> t.type.contains("loan", true) || t.category.contains("ধার", true) || t.reason.contains("ধার", true)
                "পাওনা" -> t.type.contains("lend", true) || t.category.contains("পাওনা", true) || t.reason.contains("পাওনা", true)
                "পরিশোধ" -> t.type.contains("payment", true) || t.category.contains("পরিশোধ", true) || t.reason.contains("পরিশোধ", true)
                "নেওয়া" -> t.reason.contains("নেওয়া", true) || t.category.contains("নেওয়া", true)
                else -> true
            }
            matchesText && matchesScope && matchesFilter
        }
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
    val net = totalIncome - totalExpense

    LaunchedEffect(active) {
        if (active) {
            delay(250)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
            selectedFilter = "সব"
        }
    }

    AnimatedVisibility(
        visible = active,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            Column(Modifier.fillMaxSize()) {
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
                            trailingIcon = {
                                IconButton(onClick = onClose) {
                                    Icon(Icons.Default.Clear, contentDescription = "বন্ধ")
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            listOf("সব", "আয়", "খরচ", "ধার", "পাওনা", "পরিশোধ").forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (query.isNotBlank()) {
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

                if (query.isBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(10.dp))
                            Text("Smart Search", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("যে হিসাব খুঁজছেন তার নাম বা তথ্য লিখুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
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
                            item { Text("ব্যাংক / অ্যাকাউন্ট", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)) }
                            items(filteredWallets, key = { "wallet_${it.id}" }) { wallet ->
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(wallet.name, fontWeight = FontWeight.Bold)
                                        Text(wallet.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (filteredLoans.isNotEmpty()) {
                            item { Text("ঋণ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)) }
                            items(filteredLoans, key = { "loan_${it.id}" }) { loan ->
                                val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                                val remaining = (loan.principal - paid).coerceAtLeast(0.0)
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(loan.name, fontWeight = FontWeight.Bold)
                                        Text("বাকি: ৳${formatMoney(remaining)}", fontSize = 12.sp)
                                        if (loan.note.isNotBlank()) Text(loan.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
