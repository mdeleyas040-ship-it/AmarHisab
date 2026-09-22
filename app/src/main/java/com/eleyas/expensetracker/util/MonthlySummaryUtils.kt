package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlySummary(
    val start: Date,
    val end: Date,
    val transactions: List<Transaction>
) {
    val incomeTransactions: List<Transaction>
        get() = transactions.filter { it.type == "income" }

    val expenseTransactions: List<Transaction>
        get() = transactions.filter { it.type == "expense" || it.type == "home" || it.type == "home_expense" }
}

object MonthlySummaryUtils {

    fun currentCycle(reference: Calendar = Calendar.getInstance()): Pair<Date, Date> {
        val end = (reference.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, 15)
            if (reference.before(this)) {
                add(Calendar.MONTH, -1)
            }
        }

        val start = (end.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        return start.time to end.time
    }

    fun forPeriod(
        transactions: List<Transaction>,
        start: Date,
        end: Date
    ): MonthlySummary {
        val filtered = transactions.filter { transaction ->
            val date = parseDate(transaction.date) ?: return@filter false
            date >= start && date < end
        }.sortedByDescending { parseDate(it.date)?.time ?: Long.MIN_VALUE }
        return MonthlySummary(start, end, filtered)
    }

    fun parseDate(value: String): Date? = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }.parse(value)
    } catch (_: Exception) {
        null
    }

    fun displayPeriod(start: Date, end: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return "${format.format(start)} – ${format.format(end)}"
    }

    fun notificationPeriod(start: Date, end: Date): String {
        val format = SimpleDateFormat("dd/MM", Locale.getDefault())
        return "${format.format(start)}–${format.format(end)}"
    }
}
