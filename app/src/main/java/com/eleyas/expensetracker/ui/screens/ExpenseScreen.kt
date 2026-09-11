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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.*
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
    onShare: (Transaction, String) -> Unit = { _, _ -> },
    splitBills: List<SplitBillGroup> = emptyList(),
    onAddSplitBill: () -> Unit = {},
    onSplitBillClick: (SplitBillGroup) -> Unit = {},
    targetTransactionId: Long? = null
) {
    var receiptTransaction by remember { mutableStateOf<Transaction?>(null) }
    var receiptNote by remember { mutableStateOf(TextFieldValue("")) }

    receiptTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = { receiptTransaction = null },
            title = { Text("কাস্টম রসিদ শেয়ার") },
            text = {
                Column {
                    Text(
                        "রসিদের সঙ্গে একটি অতিরিক্ত নোট যোগ করতে পারেন।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = receiptNote,
                        onValueChange = { receiptNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("অতিরিক্ত নোট (ঐচ্ছিক)") },
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onShare(transaction, receiptNote.text)
                        receiptTransaction = null
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("শেয়ার করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptTransaction = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = ExpenseRed.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(11.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "খরচ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "সব খরচের সম্পূর্ণ হিসাব",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ExpenseRed.copy(alpha = 0.96f),
                                    ExpenseRed.copy(alpha = 0.74f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }

                        Spacer(Modifier.size(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                "মোট খরচ",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp
                            )
                            Text(
                                "৳ ${formatMoney(totalExpense)}",
                                color = Color.White,
                                fontSize = 27.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${expenseTransactions.size}টি খরচ",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        ExpenseStat(
                            "সাধারণ খরচ",
                            (totalExpense - totalHome).coerceAtLeast(0.0),
                            Modifier.weight(1f)
                        )
                        ExpenseStat(
                            "বাড়ির খরচ",
                            totalHome,
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "সার্চ ফলাফল: ${filteredTransactions.size}টি",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (splitBills.isNotEmpty()) {
            item {
                Text(
                    "স্প্লিট বিল / খরচ ভাগাভাগি",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            items(splitBills, key = { it.id }) { split ->
                val perPerson = if (split.members.isNotEmpty()) {
                    split.totalAmount / split.members.size
                } else 0.0

                Card(
                    onClick = { onSplitBillClick(split) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = Color(0xFF673AB7).copy(alpha = 0.10f)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.padding(10.dp).size(23.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(split.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(3.dp))
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
                                "প্রতিজন ৳${formatMoney(perPerson)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "খরচের ইতিহাস",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${filteredTransactions.size}টি এন্ট্রি",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(list, key = { it.id }) { trans ->
                val wallet = wallets.firstOrNull { it.id == trans.walletId }
                TransactionCard(
                    trans,
                    usdToBdt,
                    usdToMvr,
                    walletName = wallet?.name ?: "",
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onShare = {
                        receiptNote = TextFieldValue("")
                        receiptTransaction = it
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseStat(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                label,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 10.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "৳${formatMoney(amount)}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
