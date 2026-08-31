package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val today =
        remember {
            Calendar.getInstance()
        }

    val day =
        today.get(Calendar.DAY_OF_MONTH)

    val month =
        today.get(Calendar.MONTH) + 1

    val memories =
        remember(transactions) {
            OnThisDayManager.getTodayMemories(
                transactions
            )
        }

    val grouped =
        remember(memories) {
            OnThisDayManager.groupByYear(
                memories
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        // Premium Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,
                    contentDescription =
                        "Back"
                )
            }

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = "এই দিনে",
                    fontSize = 21.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "${OnThisDayManager.toBanglaNumber(day)} " +
                                "${OnThisDayManager.getMonthName(month)}",
                    fontSize = 13.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Surface(
                modifier =
                    Modifier.size(46.dp),
                shape = CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription =
                            null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        modifier =
                            Modifier.size(24.dp)
                    )
                }
            }
        }

        if (memories.isEmpty()) {

            EmptyOnThisDayState()

        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 30.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                // Summary
                item {

                    SummaryCard(
                        count = memories.size
                    )
                }

                grouped.forEach {
                        (year, yearTransactions) ->

                    item {

                        YearHeader(
                            year = year,
                            count =
                                yearTransactions.size
                        )
                    }

                    items(
                        items = yearTransactions,
                        key = {
                            "on_this_day_${it.id}"
                        }
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
private fun SummaryCard(
    count: Int
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier =
                    Modifier.size(54.dp),
                shape = CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "📜",
                        fontSize = 25.sp
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.size(14.dp)
            )

            Column {

                Text(
                    text =
                        "আপনার হিসাবের স্মৃতি",
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(
                    text =
                        "এই দিনে আগের বছরগুলোতে " +
                                "$count টি লেনদেন করেছিলেন",
                    fontSize = 12.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun YearHeader(
    year: Int,
    count: Int
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                OnThisDayManager.toBanglaNumber(
                    year
                ),
            fontSize = 17.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier =
                Modifier.size(8.dp)
        )

        Surface(
            shape =
                RoundedCornerShape(50.dp),
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        ) {

            Text(
                text =
                    "$count টি",
                modifier =
                    Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 4.dp
                    ),
                fontSize = 10.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnThisDayTransactionCard(
    transaction: Transaction
) {

    val isIncome =
        transaction.type
            .equals(
                "income",
                ignoreCase = true
            )

    val accent =
        if (isIncome) {
            Color(0xFF22C55E)
        } else {
            Color(0xFFEF4444)
        }

    val icon =
        if (isIncome) {
            Icons.Default.TrendingUp
        } else {
            Icons.Default.TrendingDown
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    modifier =
                        Modifier.size(44.dp),
                    shape = CircleShape,
                    color =
                        accent.copy(
                            alpha = 0.12f
                        )
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector = icon,
                            contentDescription =
                                null,
                            tint = accent,
                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.size(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            if (isIncome)
                                "আয়"
                            else
                                "খরচ",
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = accent
                    )

                    Text(
                        text =
                            transaction.reason
                                .ifBlank {
                                    transaction.category
                                }
                                .ifBlank {
                                    "লেনদেন"
                                },
                        fontSize = 15.sp,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 2
                    )
                }

                Text(
                    text =
                        formatTransactionAmount(
                            transaction
                        ),
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color = accent
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ReceiptLong,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(16.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.size(6.dp)
                )

                Text(
                    text =
                        transaction.category
                            .ifBlank {
                                "সাধারণ"
                            },
                    fontSize = 11.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text =
                        transaction.date,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTransactionAmount(
    transaction: Transaction
): String {

    val amount =
        if (transaction.amount % 1.0 == 0.0) {
            transaction.amount
                .toLong()
                .toString()
        } else {
            String.format(
                Locale.getDefault(),
                "%.2f",
                transaction.amount
            )
        }

    return when (
        transaction.currency.uppercase(
            Locale.getDefault()
        )
    ) {

        "BDT" -> "৳$amount"

        "MVR" -> "Rf $amount"

        "USD" -> "$$amount"

        else ->
            "$amount ${transaction.currency}"
    }
}

@Composable
private fun EmptyOnThisDayState() {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(30.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Surface(
            modifier =
                Modifier.size(76.dp),
            shape = CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "📅",
                    fontSize = 36.sp
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "এই দিনে কোনো পুরনো লেনদেন নেই",
            fontSize = 17.sp,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                "আগের বছরের একই দিনে লেনদেন থাকলে\n" +
                        "এখানে তার স্মৃতি দেখা যাবে।",
            fontSize = 12.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            modifier =
                Modifier.padding(
                    top = 6.dp
                )
        )
    }
}