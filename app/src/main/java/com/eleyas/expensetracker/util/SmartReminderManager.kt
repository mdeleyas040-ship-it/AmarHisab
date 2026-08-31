package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SmartReminder(
    val id: String,
    val title: String,
    val message: String,
    val transactionId: Long,
    val type: String
)

object SmartReminderManager {

    /**
     * "এই দিনে" reminder
     *
     * আজকের দিন + মাসের সঙ্গে মিলে যায় এমন
     * আগের বছরের transaction খুঁজে বের করে।
     *
     * উদাহরণ:
     * আজ 01/09/2026 হলে
     *
     * 01/09/2025
     * 01/09/2024
     * 01/09/2023
     *
     * সব matching transaction পাওয়া যাবে।
     */
    fun getTransactionReminders(
        transactions: List<Transaction>
    ): List<SmartReminder> {

        val dateFormat =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

        val today =
            Calendar.getInstance()

        val todayDay =
            today.get(Calendar.DAY_OF_MONTH)

        val todayMonth =
            today.get(Calendar.MONTH) + 1

        val currentYear =
            today.get(Calendar.YEAR)

        return transactions
            .mapNotNull { transaction ->

                try {

                    val parsedDate =
                        dateFormat.parse(
                            transaction.date
                        )
                            ?: return@mapNotNull null

                    val transactionCalendar =
                        Calendar.getInstance().apply {
                            time = parsedDate
                        }

                    val transactionDay =
                        transactionCalendar.get(
                            Calendar.DAY_OF_MONTH
                        )

                    val transactionMonth =
                        transactionCalendar.get(
                            Calendar.MONTH
                        ) + 1

                    val transactionYear =
                        transactionCalendar.get(
                            Calendar.YEAR
                        )

                    /*
                     * শুধু আগের বছরের
                     * একই দিন + একই মাস।
                     */
                    if (
                        transactionDay ==
                        todayDay &&
                        transactionMonth ==
                        todayMonth &&
                        transactionYear <
                        currentYear
                    ) {

                        val amountText =
                            formatAmount(
                                transaction.amount,
                                transaction.currency
                            )

                        val reason =
                            transaction.reason
                                .ifBlank {
                                    transaction.category
                                }
                                .ifBlank {
                                    "এই লেনদেন"
                                }

                        SmartReminder(
                            id =
                                "on_this_day_${transaction.id}",

                            title =
                                "🔔 এই দিনে",

                            message =
                                "এই দিনে $amountText এর " +
                                        "\"$reason\" " +
                                        "লেনদেন করেছিলে।",

                            transactionId =
                                transaction.id,

                            type =
                                "on_this_day"
                        )

                    } else {
                        null
                    }

                } catch (_: Exception) {
                    null
                }
            }
            .sortedByDescending {
                extractYear(
                    it.message
                )
            }
    }

    /**
     * একই দিনের সব transaction-কে
     * একটি summary reminder-এ পরিণত করে।
     */
    fun getTodaySummary(
        transactions: List<Transaction>
    ): List<SmartReminder> {

        val reminders =
            getTransactionReminders(
                transactions
            )

        if (reminders.isEmpty()) {
            return emptyList()
        }

        val count =
            reminders.size

        val message =
            if (count == 1) {
                "এই দিনে আগের বছরে " +
                        "আপনি একটি লেনদেন করেছিলেন।"
            } else {
                "এই দিনে আগের বছরগুলোতে " +
                        "$count টি লেনদেন করেছিলেন।"
            }

        return listOf(
            SmartReminder(
                id =
                    "on_this_day_summary",

                title =
                    "📅 এই দিনে আপনার হিসাব",

                message =
                    message,

                transactionId =
                    reminders.first().transactionId,

                type =
                    "on_this_day"
            )
        )
    }

    private fun extractYear(
        text: String
    ): Int {

        return try {

            val match =
                Regex(
                    """\b(\d{4})\b"""
                ).find(text)

            match
                ?.groupValues
                ?.getOrNull(1)
                ?.toInt()
                ?: 0

        } catch (_: Exception) {
            0
        }
    }

    private fun formatAmount(
        amount: Double,
        currency: String
    ): String {

        val formatted =
            if (amount % 1.0 == 0.0) {

                amount
                    .toLong()
                    .toString()

            } else {

                String.format(
                    Locale.getDefault(),
                    "%.2f",
                    amount
                )
            }

        return when (
            currency.uppercase(
                Locale.getDefault()
            )
        ) {

            "BDT" ->
                "৳$formatted"

            "MVR" ->
                "Rf $formatted"

            "USD" ->
                "$$formatted"

            else ->
                "$formatted $currency"
        }
    }
}