package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.model.Wallet
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.ui.theme.IncomeGreen
import com.eleyas.expensetracker.util.MonthlySummary
import com.eleyas.expensetracker.util.MonthlySummaryUtils
import com.eleyas.expensetracker.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    modifier: Modifier = Modifier,
    summary: MonthlySummary,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    onBack: () -> Unit,
    onDownloadPdf: (MonthlySummary) -> Unit
) {
    fun toBdt(transaction: Transaction): Double = when (transaction.currency) {
        "BDT" -> transaction.amount
        "USD" -> transaction.amount * usdToBdt
        "MVR" -> if (usdToMvr > 0) transaction.amount * (usdToBdt / usdToMvr) else 0.0
        else -> transaction.amount
    }

    val totalIncome = summary.incomeTransactions.sumOf(::toBdt)
    val totalExpense = summary.expenseTransactions.sumOf(::toBdt)
    val net = totalIncome - totalExpense
    val categoryTotals = summary.expenseTransactions
        .groupBy { it.category.ifBlank { "অন্যান্য" } }
        .mapValues { (_, list) -> list.sumOf(::toBdt) }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("মাসিক হিসাব", fontWeight = FontWeight.ExtraBold)
                        Text(
                            MonthlySummaryUtils.displayPeriod(summary.start, summary.end),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { onDownloadPdf(summary) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("PDF ডাউনলোড করুন", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(15.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("১৫–১৫ মাসিক সারসংক্ষেপ", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("এই সময়ের সব হিসাব এক জায়গায়", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMetric("মোট আয়", totalIncome, IncomeGreen, Modifier.weight(1f))
                    SummaryMetric("মোট খরচ", totalExpense, ExpenseRed, Modifier.weight(1f))
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = if (net >= 0) IncomeGreen.copy(alpha = 0.10f) else ExpenseRed.copy(alpha = 0.10f)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("নিট ক্যাশফ্লো", fontWeight = FontWeight.SemiBold)
                            Text(
                                summary.transactions.size.toString() + "টি লেনদেন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "৳" + formatMoney(net),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (net >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            if (categoryTotals.isNotEmpty()) {
                item {
                    SummarySectionCard("খরচের বিভাগ", "এই সময়ে কোন খাতে কত খরচ হয়েছে") {
                        val max = categoryTotals.first().second.coerceAtLeast(1.0)
                        categoryTotals.forEachIndexed { index, pair ->
                            CategoryRow(pair.first, pair.second, max)
                            if (index < categoryTotals.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            item {
                SummarySectionCard("লেনদেনের তালিকা", "১৫–১৫ সময়সীমার সব transaction") {}
            }

            items(summary.transactions) { transaction ->
                val wallet = wallets.firstOrNull { it.id == transaction.walletId }
                MonthlyTransactionRow(transaction, toBdt(transaction), wallet?.name.orEmpty())
            }

            if (summary.transactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("এই সময়ে কোনো লেনদেন নেই", fontWeight = FontWeight.Bold)
                            Text(
                                "পরবর্তী ১৫ তারিখে নতুন হিসাব তৈরি হবে।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SummaryMetric(title: String, amount: Double, accent: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.10f)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(5.dp))
            Text("৳" + formatMoney(amount), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
private fun SummarySectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun CategoryRow(category: String, amount: Double, max: Double) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("৳" + formatMoney(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (amount / max).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MonthlyTransactionRow(
    transaction: Transaction,
    amountBdt: Double,
    walletName: String
) {
    val isIncome = transaction.type == "income"
    val accent = if (isIncome) IncomeGreen else ExpenseRed

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.10f)) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.padding(9.dp), tint = accent)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.reason.ifBlank { transaction.category }, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    listOf(transaction.date, transaction.category, walletName).filter { it.isNotBlank() }.joinToString(" • "),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isIncome) "+" else "-") + "৳" + formatMoney(amountBdt),
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
                if (transaction.currency != "BDT") {
                    Text(
                        formatMoney(transaction.amount) + " " + transaction.currency,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
