package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * "home" টাইপের transaction গুলো (দেশে পাঠানো রেমিট্যান্স) থেকে
 * মাস-ভিত্তিক ইতিহাস এবং exchange rate পরিবর্তন হিসাব করে।
 *
 * এটি বিদ্যমান কোনো data বা model পরিবর্তন করে না, শুধু existing
 * Transaction তালিকা থেকে read-only view তৈরি করে।
 */
object RemittanceCalculator {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthKeyFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
    private val monthLabelFormat = SimpleDateFormat("MMM yyyy", Locale("bn", "BD"))

    data class MonthlyRemittance(
        val monthKey: String,
        val monthLabel: String,
        val totalSent: Double,
        val transactionCount: Int,
        val averageExchangeRate: Double?
    )

    data class RemittanceSummary(
        val months: List<MonthlyRemittance>,
        val totalSent: Double,
        val totalCount: Int,
        val latestExchangeRate: Double?,
        val previousExchangeRate: Double?
    ) {
        val rateChange: Double?
            get() {
                val latest = latestExchangeRate
                val previous = previousExchangeRate
                return if (latest != null && previous != null) latest - previous else null
            }
    }

    /**
     * @param monthsBack কত মাসের ইতিহাস দেখানো হবে (default সর্বশেষ ১২ মাস)।
     */
    fun summarize(
        transactions: List<Transaction>,
        monthsBack: Int = 12,
        today: Date = Date(),
        amountConverter: (Transaction) -> Double = { it.amount }
    ): RemittanceSummary {
        val remittances = transactions.filter { it.type.equals("home", ignoreCase = true) }

        val current = Calendar.getInstance().apply {
            time = today
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // সাম্প্রতিক মাস থেকে পুরনোর দিকে monthsBack সংখ্যক মাসের বাকেট তৈরি করা হয়।
        val monthSlots = (monthsBack - 1 downTo 0).map { offset ->
            Calendar.getInstance().apply {
                time = current.time
                add(Calendar.MONTH, -offset)
            }
        }

        val buckets = monthSlots.associate { calendar ->
            monthKeyFormat.format(calendar.time) to MutableMonthBucket(calendar.time)
        }.toMutableMap()

        remittances.forEach { transaction ->
            val date = parseDate(transaction.date) ?: return@forEach
            val key = monthKeyFormat.format(date)
            val bucket = buckets[key] ?: return@forEach
            bucket.total += amountConverter(transaction).coerceAtLeast(0.0)
            bucket.count += 1
            transaction.exchangeRateUsed?.let { rate ->
                bucket.rateSum += rate
                bucket.rateCount += 1
            }
        }

        val months = monthSlots.map { calendar ->
            val key = monthKeyFormat.format(calendar.time)
            val bucket = buckets.getValue(key)
            MonthlyRemittance(
                monthKey = key,
                monthLabel = monthLabelFormat.format(calendar.time),
                totalSent = bucket.total,
                transactionCount = bucket.count,
                averageExchangeRate = if (bucket.rateCount > 0) bucket.rateSum / bucket.rateCount else null
            )
        }

        val monthsWithRate = months.filter { it.averageExchangeRate != null }
        val latestRate = monthsWithRate.lastOrNull()?.averageExchangeRate
        val previousRate = monthsWithRate.dropLast(1).lastOrNull()?.averageExchangeRate

        return RemittanceSummary(
            months = months,
            totalSent = remittances.sumOf { amountConverter(it).coerceAtLeast(0.0) },
            totalCount = remittances.size,
            latestExchangeRate = latestRate,
            previousExchangeRate = previousRate
        )
    }

    private class MutableMonthBucket(val monthDate: Date) {
        var total: Double = 0.0
        var count: Int = 0
        var rateSum: Double = 0.0
        var rateCount: Int = 0
    }

    private fun parseDate(value: String): Date? = try {
        dateFormat.isLenient = false
        dateFormat.parse(value)
    } catch (_: Exception) {
        null
    }
}
