package com.eleyas.expensetracker.ui.components

import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.convertToBdt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

object MyJourneyCalculator {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun daysInMaldives(arrivalDate: String, today: Date = Date()): Long? {
        val arrival = parseDate(arrivalDate) ?: return null
        if (arrival.after(today)) return 0L
        return ((today.time - arrival.time) / DAY_MS).coerceAtLeast(0L)
    }

    fun journeyLabel(arrivalDate: String, today: Date = Date()): String {
        val arrival = parseDate(arrivalDate) ?: return "তারিখ সেট করুন"
        if (arrival.after(today)) return "আগমনের তারিখ ভবিষ্যতে"

        val start = Calendar.getInstance().apply { time = arrival }
        val end = Calendar.getInstance().apply { time = today }
        var years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR)
        var months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH)
        var days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val previousMonth = Calendar.getInstance().apply {
                set(Calendar.YEAR, end.get(Calendar.YEAR))
                set(Calendar.MONTH, end.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DAY_OF_MONTH, -1)
            }
            days += previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        return "${years.coerceAtLeast(0)} বছর ${months.coerceAtLeast(0)} মাস ${days.coerceAtLeast(0)} দিন"
    }

    /** Average monthly income from the last [months] calendar months, converted to BDT. */
    fun averageMonthlyIncome(
        transactions: List<Transaction>,
        months: Int = 3,
        today: Date = Date()
    ): Double = averageMonthlyAmount(transactions, "income", months, today)

    /** Average monthly expense from the last [months] calendar months, converted to BDT. */
    fun averageMonthlyExpense(
        transactions: List<Transaction>,
        months: Int = 3,
        today: Date = Date()
    ): Double = averageMonthlyAmount(transactions, "expense", months, today)

    private fun averageMonthlyAmount(
        transactions: List<Transaction>,
        type: String,
        months: Int,
        today: Date
    ): Double {
        if (months <= 0) return 0.0

        val current = Calendar.getInstance().apply {
            time = today
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val monthTotals = DoubleArray(months)

        transactions.asSequence()
            .filter { it.type.equals(type, ignoreCase = true) }
            .forEach { transaction ->
                val date = parseDate(transaction.date) ?: return@forEach
                val transactionMonth = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.DAY_OF_MONTH, 1)
                }

                val monthDistance =
                    ((current.get(Calendar.YEAR) - transactionMonth.get(Calendar.YEAR)) * 12) +
                        (current.get(Calendar.MONTH) - transactionMonth.get(Calendar.MONTH))

                if (monthDistance in 0 until months) {
                    monthTotals[monthDistance] += convertToBdt(transaction.amount, transaction.currency)
                        .coerceAtLeast(0.0)
                }
            }

        // Three calendar months are used consistently, including months with no entry.
        return monthTotals.sum() / months
    }

    fun repaymentDays(debt: Double, salary: Double, monthlyExpense: Double): Long? {
        val available = salary - monthlyExpense
        if (debt <= 0.0) return 0L
        if (available <= 0.0) return null
        return ceil(debt / (available / 30.0)).toLong().coerceAtLeast(1L)
    }

    fun debtFreeDate(days: Long, today: Date = Date()): Date? {
        if (days < 0) return null
        return Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, days.toInt())
        }.time
    }

    fun formatDate(date: Date): String = dateFormat.format(date)

    private fun parseDate(value: String): Date? = try {
        dateFormat.isLenient = false
        dateFormat.parse(value)
    } catch (_: Exception) {
        null
    }
}
