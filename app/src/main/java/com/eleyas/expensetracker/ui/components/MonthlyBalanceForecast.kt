package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class MonthlyBalanceForecast(
    val currentMonthIncome: Double,
    val currentMonthExpense: Double,
    val projectedBalance: Double,
    val remainingDays: Int,
    val elapsedDays: Int
)

object MonthlyBalanceForecastCalculator {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun calculate(
        balance: Double,
        transactions: List<Transaction>,
        today: Date = Date(),
        amountConverter: (Transaction) -> Double = { it.amount }
    ): MonthlyBalanceForecast {
        val current = Calendar.getInstance().apply {
            time = today
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val month = current.get(Calendar.MONTH)
        val year = current.get(Calendar.YEAR)
        var income = 0.0
        var expense = 0.0

        transactions.forEach { transaction ->
            val transactionDate = parseDate(transaction.date) ?: return@forEach
            val transactionCalendar = Calendar.getInstance().apply { time = transactionDate }
            if (transactionCalendar.get(Calendar.YEAR) != year ||
                transactionCalendar.get(Calendar.MONTH) != month ||
                transactionDate.after(today)
            ) {
                return@forEach
            }

            val amount = amountConverter(transaction).coerceAtLeast(0.0)
            when {
                transaction.type.equals("income", ignoreCase = true) -> income += amount
                transaction.type.equals("expense", ignoreCase = true) -> expense += amount
            }
        }

        val elapsedDays = current.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = current.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = (daysInMonth - elapsedDays).coerceAtLeast(0)
        val projectedDailyNet = (income - expense) / elapsedDays
        val projectedBalance = balance + projectedDailyNet * remainingDays

        return MonthlyBalanceForecast(
            currentMonthIncome = income,
            currentMonthExpense = expense,
            projectedBalance = projectedBalance,
            remainingDays = remainingDays,
            elapsedDays = elapsedDays
        )
    }

    private fun parseDate(value: String): Date? = try {
        dateFormat.isLenient = false
        dateFormat.parse(value)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun MonthlyBalanceForecastCard(
    balance: Double,
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double,
    modifier: Modifier = Modifier
) {
    val forecast = remember(transactions, balance, usdToBdt, usdToMvr) {
        MonthlyBalanceForecastCalculator.calculate(
            balance = balance,
            transactions = transactions,
            amountConverter = { transaction ->
                when (transaction.currency.trim().uppercase(Locale.getDefault())) {
                    "BDT", "৳" -> transaction.amount
                    "USD", "$" -> if (usdToBdt > 0.0) transaction.amount * usdToBdt else transaction.amount
                    "MVR", "RF", "RUFIYAA" ->
                        if (usdToBdt > 0.0 && usdToMvr > 0.0) {
                            transaction.amount * (usdToBdt / usdToMvr)
                        } else {
                            transaction.amount
                        }
                    else -> transaction.amount
                }
            }
        )
    }
    val forecastColor = if (forecast.projectedBalance >= 0.0) {
        Color(0xFF168A55)
    } else {
        MaterialTheme.colorScheme.error
    }
    val trendIcon = if (forecast.projectedBalance >= balance) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val change = (forecast.projectedBalance - balance).roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "মাস শেষে আনুমানিক ব্যালেন্স",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "চলমান খরচের গতিতে পূর্বাভাস",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.size(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (forecast.projectedBalance >= 0.0) "সম্ভাব্য উদ্বৃত্ত" else "সম্ভাব্য ঘাটতি",
                        color = forecastColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "৳ ${formatMoney(forecast.projectedBalance)}",
                        color = forecastColor,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = forecastColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.size(8.dp))
            Text(
                text = "এখন পর্যন্ত আয় ৳ ${formatMoney(forecast.currentMonthIncome)} • খরচ ৳ ${formatMoney(forecast.currentMonthExpense)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = "${forecast.remainingDays} দিন বাকি • বর্তমান ব্যালেন্স থেকে ৳ ${formatMoney(kotlin.math.abs(change.toDouble()))} ${if (change >= 0) "বাড়তে" else "কমতে"} পারে",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
