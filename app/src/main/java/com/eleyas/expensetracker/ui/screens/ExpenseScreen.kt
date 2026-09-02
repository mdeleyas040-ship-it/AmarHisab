package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.eleyas.expensetracker.ui.components.SmallSummaryCard
import com.eleyas.expensetracker.ui.components.TransactionCard
import com.eleyas.expensetracker.ui.theme.Blue
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseScreen(
    modifier: Modifier,
    transactions: List<Transaction>,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    searchQuery: String = "",
    onAddExpense: () -> Unit,
    onAddHome: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    splitBills: List<SplitBillGroup> = emptyList(),
    onAddSplitBill: () -> Unit = {},
    onSplitBillClick: (SplitBillGroup) -> Unit = {},
    targetTransactionId: Long? = null
) {
    // “খরচ” Tab-এ শুধু প্রকৃত খরচগুলো থাকবে।
    // ব্যক্তিগত খরচ + বাড়ির খরচ একই History-তে দেখানো হবে।
    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type == "expense" || it.type == "home_expense" }
    }

    val filteredTransactions = expenseTransactions.filter {
        it.reason.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    val groupedTransactions = groupTransactionsByDate(filteredTransactions)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(targetTransactionId, groupedTransactions) {
        if (targetTransactionId != null) {
            var index = 2

            if (splitBills.isNotEmpty()) {
                index += 2
                index += splitBills.size
            }

            for ((_, list) in groupedTransactions) {
                index += 1

                val targetIndex = list.indexOfFirst { it.id == targetTransactionId }

                if (targetIndex >= 0) {
                    listState.animateScrollToItem(index + targetIndex)
                    break
                }

                index += list.size
            }
        }
    }

    fun convertToBdt(amount: Double, currency: String): Double = when (currency) {
        "BDT" -> amount
        "USD" -> amount * usdToBdt
        "MVR" -> if (usdToMvr > 0) amount * (usdToBdt / usdToMvr) else 0.0
        else -> 0.0
    }

    val totalExpense = expenseTransactions.sumOf {
        convertToBdt(it.amount, it.currency)
    }

    val totalHome = expenseTransactions
        .filter { it.type == "home_expense" }
        .sumOf { convertToBdt(it.amount, it.currency) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                SmallSummaryCard(
                    Modifier.weight(1f),
                    "মোট খরচ",
                    totalExpense,
                    ExpenseRed,
                    Icons.Default.Payments
                )
                SmallSummaryCard(
                    Modifier.weight(1f),
                    "বাড়ির খরচ",
                    totalHome,
                    Blue,
                    Icons.Default.Home
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("খরচ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAddHome,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("বাড়িতে", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAddSplitBill,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("স্প্লিট বিল", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (splitBills.isNotEmpty()) {
            item {
                Text(
                    "স্প্লিট বিল / খরচ ভাগাভাগি",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(splitBills, key = { it.id }) { split ->
                val perPerson = if (split.members.isNotEmpty()) {
                    split.totalAmount / split.members.size
                } else 0.0

                Card(
                    onClick = { onSplitBillClick(split) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(split.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "সদস্য: ${split.members.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "৳${formatMoney(split.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF673AB7)
                            )
                            Text(
                                "প্রতিজন: ৳${formatMoney(perPerson)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        groupedTransactions.forEach { (date, list) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = date,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(list) { trans ->
                val wallet = wallets.firstOrNull { it.id == trans.walletId }
                TransactionCard(
                    trans,
                    usdToBdt,
                    usdToMvr,
                    walletName = wallet?.name ?: "",
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}
