package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.OnThisDayManager
import java.util.Calendar
import java.util.Locale

@Composable
fun OnThisDayScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit
) {
    val today = remember { Calendar.getInstance() }
    val day = today.get(Calendar.DAY_OF_MONTH)
    val month = today.get(Calendar.MONTH) + 1

    val memories = remember(transactions) {
        OnThisDayManager.getTodayMemories(transactions)
    }

    val grouped = remember(memories) {
        OnThisDayManager.groupByYear(memories)
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PremiumOnThisDayHeader(
                    day = day,
                    month = month,
                    onBack = onBack
                )

                if (memories.isEmpty()) {
                    EmptyOnThisDayState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 18.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OnThisDaySummaryCard(count = memories.size)
                        }

                        grouped.forEach { (year, yearTransactions) ->
                            item {
                                OnThisDayYearHeader(
                                    year = year,
                                    count = yearTransactions.size
                                )
                            }

                            items(
                                items = yearTransactions,
                                key = { "on_this_day_${it.id}" }
                            ) { transaction ->
                                OnThisDayTransactionCard(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumOnThisDayHeader(
    day: Int,
    month: Int,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "এই দিনে",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${OnThisDayManager.toBanglaNumber(day)} ${OnThisDayManager.getMonthName(month)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnThisDaySummaryCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📜", fontSize = 21.sp)
                    }
                }

                Spacer(Modifier.size(11.dp))

                Column {
                    Text(
                        "আপনার হিসাবের স্মৃতি",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "আগের বছরগুলোর একই দিনের লেনদেন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(11.dp))

            Text(
                "${OnThisDayManager.toBanglaNumber(count)} টি লেনদেন পাওয়া গেছে",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnThisDayYearHeader(year: Int, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            OnThisDayManager.toBanglaNumber(year),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.size(8.dp))

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                "${OnThisDayManager.toBanglaNumber(count)} টি",
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnThisDayTransactionCard(transaction: Transaction) {
    val isIncome = transaction.type.equals("income", ignoreCase = true)
    val accent = if (isIncome) Color(0xFF22C55E) else Color(0xFFEF4444)
    val icon = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.size(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isIncome) "আয়" else "খরচ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Text(
                        transaction.reason.ifBlank { transaction.category }.ifBlank { "লেনদেন" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }

                Text(
                    formatTransactionAmount(transaction),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(9.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    transaction.category.ifBlank { "সাধারণ" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    transaction.date,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTransactionAmount(transaction: Transaction): String {
    val amount = if (transaction.amount % 1.0 == 0.0) {
        transaction.amount.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", transaction.amount)
    }

    return when (transaction.currency.uppercase(Locale.getDefault())) {
        "BDT" -> "৳$amount"
        "MVR" -> "Rf $amount"
        "USD" -> "$$amount"
        else -> "$amount ${transaction.currency}"
    }
}

@Composable
private fun EmptyOnThisDayState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📅", fontSize = 30.sp)
            }
        }

        Spacer(Modifier.height(13.dp))

        Text(
            "এই দিনে কোনো পুরনো লেনদেন নেই",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "আগের বছরের একই দিনে লেনদেন থাকলে এখানে দেখা যাবে।",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
