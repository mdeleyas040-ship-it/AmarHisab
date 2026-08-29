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
        AllHistoryView(
            transactions = transactions,
            wallets = wallets,
            usdToBdt = usdToBdt,
            usdToMvr = usdToMvr,
            onEdit = onEdit,
            onDelete = onDelete,
            onBack = { showAllHistory = false }
        )
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
    val maxAmount = maxOf(totalIncome, totalExpense, 1.0)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("মাসিক সারাংশ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(14.dp))
                    ReportBar("মোট আয়", totalIncome, maxAmount, IncomeGreen)
                    Spacer(Modifier.height(10.dp))
                    ReportBar("মোট খরচ", totalExpense, maxAmount, ExpenseRed)
                }
            }
        }

        // Category breakdown
        val expenseByCategory = monthTransactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { convertToBdt(it.amount, it.currency) } }

        if (expenseByCategory.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("ক্যাটাগরি ভিত্তিক খরচ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(14.dp))
                        
                        val sortedCategories = expenseByCategory.toList().sortedByDescending { it.second }
                        val topExpense = sortedCategories.firstOrNull()?.second ?: 1.0
                        
                        sortedCategories.forEach { (cat, amt) ->
                            CategoryBar(cat, amt, topExpense)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        item { 
            Button(
                onClick = { showAllHistory = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("সব লেনদেন দেখুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Text("সাম্প্রতিক লেনদেন", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        
        items(sortTransactionsByDate(transactions).take(20)) { transaction ->
            val wallet = wallets.firstOrNull { it.id == transaction.walletId }
            TransactionCard(transaction, usdToBdt, usdToMvr, walletName = wallet?.name ?: "", onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllHistoryView(
    transactions: List<Transaction>,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = transactions.filter { it.reason.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    val grouped = groupTransactionsByDate(filtered)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text("লেনদেনের ইতিহাস", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) } },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            grouped.forEach { (date, list) ->
                item {
                    Text(date, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(list) { trans ->
                    val wallet = wallets.firstOrNull { it.id == trans.walletId }
                    TransactionCard(trans, usdToBdt, usdToMvr, walletName = wallet?.name ?: "", onEdit = onEdit, onDelete = onDelete)
                }
            }
        }
    }
}
