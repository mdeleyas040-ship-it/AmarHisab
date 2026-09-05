package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.ui.theme.IncomeGreen
import com.eleyas.expensetracker.util.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    modifier: Modifier,
    transactions: List<Transaction>,
    wallets: List<Wallet>,
    categoryBudgets: List<CategoryBudget>,
    usdToBdt: Double,
    usdToMvr: Double,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var showAllHistory by remember { mutableStateOf(false) }

    if (showAllHistory) {
        AllHistoryView(transactions, wallets, usdToBdt, usdToMvr, onEdit, onDelete) { showAllHistory = false }
        return
    }

    fun convertToBdt(amount: Double, currency: String): Double = when (currency) {
        "BDT" -> amount
        "USD" -> amount * usdToBdt
        "MVR" -> if (usdToMvr > 0) amount * (usdToBdt / usdToMvr) else 0.0
        else -> 0.0
    }

    val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    val monthTransactions = transactions.filter { it.date.endsWith(currentMonth) }
    val totalIncome = monthTransactions.filter { it.type == "income" }.sumOf { convertToBdt(it.amount, it.currency) }
    val totalExpense = monthTransactions.filter { it.type == "expense" }.sumOf { convertToBdt(it.amount, it.currency) }
    val balance = totalIncome - totalExpense
    val maxAmount = maxOf(totalIncome, totalExpense, 1.0)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Default.Assessment, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("মাসিক রিপোর্ট", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                            Text(currentMonth, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportMetric("আয়", totalIncome, IncomeGreen, Modifier.weight(1f))
                        ReportMetric("খরচ", totalExpense, ExpenseRed, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("এই মাসের অবশিষ্ট", fontWeight = FontWeight.SemiBold)
                            Text("৳${formatMoney(balance)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    ReportBar("মোট আয়", totalIncome, maxAmount, IncomeGreen)
                    Spacer(Modifier.height(9.dp))
                    ReportBar("মোট খরচ", totalExpense, maxAmount, ExpenseRed)
                }
            }
        }

        val expenseByCategory = monthTransactions.filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { convertToBdt(it.amount, it.currency) } }

        if (expenseByCategory.isNotEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("ক্যাটাগরি ভিত্তিক খরচ", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        Text("কোথায় বেশি খরচ হচ্ছে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
                        val sortedCategories = expenseByCategory.toList().sortedByDescending { it.second }
                        val topExpense = sortedCategories.firstOrNull()?.second ?: 1.0
                        sortedCategories.forEachIndexed { index, (cat, amt) ->
                            CategoryBar(cat, amt, topExpense)
                            if (index != sortedCategories.lastIndex) Spacer(Modifier.height(9.dp))
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showAllHistory = true },
                Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.width(8.dp))
                Text("সব লেনদেন দেখুন", fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            Text("সাম্প্রতিক লেনদেন", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
        items(sortTransactionsByDate(transactions).take(20)) { transaction ->
            val wallet = wallets.firstOrNull { it.id == transaction.walletId }
            TransactionCard(transaction, usdToBdt, usdToMvr, walletName = wallet?.name ?: "", onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
private fun ReportMetric(title: String, amount: Double, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.10f)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("৳${formatMoney(amount)}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllHistoryView(
    transactions: List<Transaction>, wallets: List<Wallet>, usdToBdt: Double, usdToMvr: Double,
    onEdit: (Transaction) -> Unit, onDelete: (Transaction) -> Unit, onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = transactions.filter { it.reason.contains(searchQuery, true) || it.category.contains(searchQuery, true) }
    val grouped = groupTransactionsByDate(filtered)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text("লেনদেনের ইতিহাস", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে") } }
        )
        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("লেনদেন খুঁজুন...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) } },
            shape = RoundedCornerShape(16.dp), singleLine = true
        )
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            grouped.forEach { (date, list) ->
                item { Text(date, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                items(list) { trans ->
                    val wallet = wallets.firstOrNull { it.id == trans.walletId }
                    TransactionCard(trans, usdToBdt, usdToMvr, walletName = wallet?.name ?: "", onEdit = onEdit, onDelete = onDelete)
                }
            }
        }
    }
}
