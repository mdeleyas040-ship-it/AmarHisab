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

    private const val REMINDER_DAYS = 7

    /**
     * ৭ দিন বা তার বেশি পুরোনো transaction থেকে reminder তৈরি করে।
     *
     * একই transaction-এর জন্য শুধু একবার reminder তৈরি হবে।
     */
    fun getTransactionReminders(
        transactions: List<Transaction>
    ): List<SmartReminder> {

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false

        val today = Calendar.getInstance()

        return transactions
            .mapNotNull { transaction ->

                try {
                    val transactionDate = dateFormat.parse(transaction.date)
                        ?: return@mapNotNull null

                    val transactionCalendar = Calendar.getInstance().apply {
                        time = transactionDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val todayCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val diffMillis =
                        todayCalendar.timeInMillis -
                                transactionCalendar.timeInMillis

                    val diffDays =
                        diffMillis / (24L * 60L * 60L * 1000L)

                    if (diffDays >= REMINDER_DAYS) {

                        val amountText = formatAmount(
                            transaction.amount,
                            transaction.currency
                        )

                        val reasonText = transaction.reason
                            .ifBlank { transaction.category }
                            .ifBlank { "এই লেনদেন" }

                        SmartReminder(
                            id = "transaction_${transaction.id}",
                            title = "🔔 লেনদেনের স্মরণ করানো",
                            message = "${transaction.date}-এ $amountText এর \"$reasonText\" লেনদেন করেছিলে।",
                            transactionId = transaction.id,
                            type = "transaction"
                        )
                    } else {
                        null
                    }

                } catch (_: Exception) {
                    null
                }
            }
            .sortedByDescending { it.transactionId }
    }

    /**
     * টাকা সুন্দরভাবে দেখানোর জন্য।
     */
    private fun formatAmount(
        amount: Double,
        currency: String
    ): String {

        val formatted = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(
                Locale.getDefault(),
                "%.2f",
                amount
            )
        }

        return when (currency.uppercase(Locale.getDefault())) {
            "BDT" -> "৳$formatted"
            "MVR" -> "Rf $formatted"
            "USD" -> "$$formatted"
            else -> "$formatted $currency"
        }
    }
}