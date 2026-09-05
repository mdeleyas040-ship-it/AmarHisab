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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.TransactionCard
import com.eleyas.expensetracker.ui.theme.Green
import com.eleyas.expensetracker.ui.theme.IncomeGreen
import com.eleyas.expensetracker.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IncomeScreen(
    modifier: Modifier,
    transactions: List<Transaction>,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    searchQuery: String = "",
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    targetTransactionId: Long? = null
) {
    val filteredTransactions = transactions.filter {
        it.reason.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    val groupedTransactions = groupTransactionsByDate(filteredTransactions)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(targetTransactionId, groupedTransactions) {
        if (targetTransactionId != null) {
            var index = 2

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
    val totalIncome = transactions.sumOf { convertToBdt(it.amount, it.currency) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    IncomeGreen,
                                    IncomeGreen.copy(alpha = 0.82f),
                                    Green.copy(alpha = 0.78f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "আয়ের সারাংশ",
                                    color = Color.White.copy(alpha = 0.86f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "মোট আয়",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "৳${formatMoney(totalIncome)}",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(13.dp).size(32.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    "${transactions.size}টি লেনদেন",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (searchQuery.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        "সার্চ ফলাফল",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(9.dp))
                Text("নতুন আয় যোগ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        groupedTransactions.forEach { (date, list) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = RoundedCornerShape(50),
                            color = IncomeGreen
                        ) {}
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${list.size}টি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
