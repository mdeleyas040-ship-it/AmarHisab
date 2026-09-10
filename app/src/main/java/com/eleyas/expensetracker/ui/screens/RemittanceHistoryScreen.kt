package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.RemittanceCalculator
import com.eleyas.expensetracker.util.formatMoney

/**
 * প্রবাস থেকে দেশে পাঠানো টাকার (Remittance) মাসিক ইতিহাস দেখায়,
 * সাথে প্রতি মাসে ব্যবহৃত exchange rate কিভাবে পরিবর্তন হয়েছে সেটাও ট্র্যাক করে।
 */
@Composable
fun RemittanceHistoryScreen(
    modifier: Modifier = Modifier,
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double,
    onBack: () -> Unit = {}
) {
    fun convertToBdt(amount: Double, currency: String): Double = when (currency) {
        "BDT" -> amount
        "USD" -> if (usdToBdt > 0) amount * usdToBdt else amount
        "MVR" -> if (usdToBdt > 0 && usdToMvr > 0) amount * (usdToBdt / usdToMvr) else amount
        else -> amount
    }

    val summary = RemittanceCalculator.summarize(
        transactions = transactions,
        monthsBack = 12,
        amountConverter = { convertToBdt(it.amount, it.currency) }
    )

    val maxMonthTotal = (summary.months.maxOfOrNull { it.totalSent } ?: 0.0).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "রেমিট্যান্স হিস্ট্রি",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "মাস অনুযায়ী দেশে পাঠানো টাকা ও এক্সচেঞ্জ রেট",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RemittanceSummaryCard(
                    totalSent = summary.totalSent,
                    totalCount = summary.totalCount,
                    latestRate = summary.latestExchangeRate,
                    rateChange = summary.rateChange
                )
            }

            item {
                Text(
                    "মাসিক পাঠানো টাকা",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                RemittanceBarChart(
                    months = summary.months,
                    maxTotal = maxMonthTotal
                )
            }

            item {
                Text(
                    "এক্সচেঞ্জ রেট ট্রেন্ড (মাস অনুযায়ী)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                RemittanceRateTrendChart(months = summary.months)
            }

            item {
                Text(
                    "বিস্তারিত মাসিক তালিকা",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(summary.months.reversed()) { month ->
                MonthlyRemittanceRow(month = month)
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun RemittanceSummaryCard(
    totalSent: Double,
    totalCount: Int,
    latestRate: Double?,
    rateChange: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "সর্বমোট পাঠানো (১২ মাস)",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "৳${formatMoney(totalSent)}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${totalCount}টি রেমিট্যান্স",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "সর্বশেষ এক্সচেঞ্জ রেট",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.sp
                    )
                    Text(
                        if (latestRate != null) "১ USD = ৳${formatMoney(latestRate)}" else "তথ্য নেই",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (rateChange != null) {
                    val isUp = rateChange > 0.0
                    val isFlat = kotlin.math.abs(rateChange) < 0.005
                    val icon = when {
                        isFlat -> Icons.Default.TrendingFlat
                        isUp -> Icons.Default.TrendingUp
                        else -> Icons.Default.TrendingDown
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${if (isUp) "+" else ""}${formatMoney(rateChange)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemittanceBarChart(
    months: List<RemittanceCalculator.MonthlyRemittance>,
    maxTotal: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            if (months.all { it.totalSent <= 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "এখনো কোনো রেমিট্যান্স যোগ করা হয়নি",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    months.forEach { month ->
                        val fraction = (month.totalSent / maxTotal).toFloat().coerceIn(0f, 1f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                                        .background(
                                            if (month.totalSent > 0.0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    months.forEach { month ->
                        Text(
                            monthShortLabel(month.monthLabel),
                            modifier = Modifier.weight(1f),
                            fontSize = 9.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemittanceRateTrendChart(
    months: List<RemittanceCalculator.MonthlyRemittance>
) {
    val ratedMonths = months.filter { it.averageExchangeRate != null }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            if (ratedMonths.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "রেট ট্রেন্ড দেখানোর জন্য অন্তত ২ মাসের ডেটা প্রয়োজন",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                val rates = ratedMonths.map { it.averageExchangeRate!! }
                val minRate = rates.min()
                val maxRate = rates.max()
                val range = (maxRate - minRate).let { if (it < 0.01) 1.0 else it }
                val lineColor = MaterialTheme.colorScheme.primary

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val stepX = if (rates.size > 1) size.width / (rates.size - 1) else size.width
                    val points = rates.mapIndexed { index, rate ->
                        val x = stepX * index
                        val normalized = ((rate - minRate) / range).toFloat()
                        val y = size.height - (normalized * size.height)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 5f
                        )
                    }

                    points.forEach { point ->
                        drawCircle(color = lineColor, radius = 7f, center = point)
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "৳${formatMoney(minRate)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "৳${formatMoney(maxRate)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyRemittanceRow(month: RemittanceCalculator.MonthlyRemittance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(month.monthLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    if (month.transactionCount > 0) "${month.transactionCount}টি লেনদেন" else "কোনো লেনদেন নেই",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "৳${formatMoney(month.totalSent)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (month.averageExchangeRate != null) {
                    Text(
                        "১ USD = ৳${formatMoney(month.averageExchangeRate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun monthShortLabel(label: String): String =
    label.split(" ").firstOrNull()?.take(3) ?: label
