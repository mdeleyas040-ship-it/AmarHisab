package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumOnThisDayHeader(
            day = day,
            month = month,
            onBack = onBack
        )

        if (memories.isEmpty()) {
            EmptyOnThisDayState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OnThisDaySummaryCard(
                        count = memories.size
                    )
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
                        OnThisDayTransactionCard(
                            transaction = transaction
                        )
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
                .padding(
                    start = 8.dp,
                    end = 18.dp,
                    top = 10.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "এই দিনে",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "${OnThisDayManager.toBanglaNumber(day)} ${OnThisDayManager.getMonthName(month)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnThisDaySummaryCard(
    count: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "📜",
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.size(13.dp))

                Column {
                    Text(
                        text = "আপনার হিসাবের স্মৃতি",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "এই দিনে আগের বছরগুলোর লেনদেন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${OnThisDayManager.toBanglaNumber(count)} টি লেনদেন পাওয়া গেছে",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "আপনার পুরনো হিসাবের স্মৃতি এক জায়গায় দেখা যাচ্ছে।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OnThisDayYearHeader(
    year: Int,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp,
                bottom = 1.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = OnThisDayManager.toBanglaNumber(year),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.size(8.dp))

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "${OnThisDayManager.toBanglaNumber(count)} টি",
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnThisDayTransactionCard(
    transaction: Transaction
) {
    val isIncome = transaction.type.equals(
        "income",
        ignoreCase = true
    )

    val accent = if (isIncome) {
        Color(0xFF22C55E)
    } else {
        Color(0xFFEF4444)
    }

    val icon = if (isIncome) {
        Icons.Default.TrendingUp
    } else {
        Icons.Default.TrendingDown
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(45.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isIncome) "আয়" else "খরচ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = transaction.reason
                            .ifBlank { transaction.category }
                            .ifBlank { "লেনদেন" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }

                Text(
                    text = formatTransactionAmount(transaction),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(11.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.size(6.dp))

                Text(
                    text = transaction.category.ifBlank { "সাধারণ" },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = transaction.date,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTransactionAmount(
    transaction: Transaction
): String {
    val amount = if (transaction.amount % 1.0 == 0.0) {
        transaction.amount.toLong().toString()
    } else {
        String.format(
            Locale.getDefault(),
            "%.2f",
            transaction.amount
        )
    }

    return when (
        transaction.currency.uppercase(Locale.getDefault())
    ) {
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
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(78.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "📅",
                    fontSize = 36.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(17.dp))

        Text(
            text = "এই দিনে কোনো পুরনো লেনদেন নেই",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "আগের বছরের একই দিনে লেনদেন থাকলে\nএখানে তার স্মৃতি দেখা যাবে।",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}
