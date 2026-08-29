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
    onDelete: (Transaction) -> Unit
) {
    val filteredTransactions = transactions.filter {
        it.reason.contains(searchQuery, ignoreCase = true) || 
        it.category.contains(searchQuery, ignoreCase = true)
    }

    val groupedTransactions = groupTransactionsByDate(filteredTransactions)

    fun convertToBdt(amount: Double, currency: String): Double = when (currency) {
        "BDT" -> amount
        "USD" -> amount * usdToBdt
        "MVR" -> if (usdToMvr > 0) amount * (usdToBdt / usdToMvr) else 0.0
        else -> 0.0
    }
    val totalIncome = transactions.sumOf { convertToBdt(it.amount, it.currency) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IncomeGreen)
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("মোট আয়", color = Color.White, fontSize = 14.sp)
                        Text("৳${formatMoney(totalIncome)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("${transactions.size}টি লেনদেন", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(60.dp))
                }
            }
        }
        item {
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("নতুন আয় যোগ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                TransactionCard(trans, usdToBdt, usdToMvr, walletName = wallet?.name ?: "", onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}
