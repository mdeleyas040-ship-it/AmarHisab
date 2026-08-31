package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class OnThisDayGroup(
    val day: Int,
    val month: Int,
    val transactions: List<Transaction>
)

object OnThisDayManager {

    /**
     * আজকের দিন ও মাসের সঙ্গে মিলে যায় এমন
     * আগের বছরের সব transaction খুঁজে বের করে।
     *
     * উদাহরণ:
     * আজ ১ সেপ্টেম্বর হলে:
     * 01/09/2025
     * 01/09/2024
     * 01/09/2023
     * সব পাওয়া যাবে।
     */
    fun getTodayMemories(
        transactions: List<Transaction>
    ): List<Transaction> {

        val dateFormat =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

        val today = Calendar.getInstance()

        val todayDay =
            today.get(Calendar.DAY_OF_MONTH)

        val todayMonth =
            today.get(Calendar.MONTH) + 1

        return transactions
            .mapNotNull { transaction ->

                try {

                    val date =
                        dateFormat.parse(transaction.date)
                            ?: return@mapNotNull null

                    val calendar =
                        Calendar.getInstance().apply {
                            time = date
                        }

                    val day =
                        calendar.get(Calendar.DAY_OF_MONTH)

                    val month =
                        calendar.get(Calendar.MONTH) + 1

                    val year =
                        calendar.get(Calendar.YEAR)

                    val currentYear =
                        today.get(Calendar.YEAR)

                    if (
                        day == todayDay &&
                        month == todayMonth &&
                        year < currentYear
                    ) {
                        transaction
                    } else {
                        null
                    }

                } catch (_: Exception) {
                    null
                }
            }
            .sortedByDescending {
                extractYear(it.date)
            }
    }

    fun groupByYear(
        transactions: List<Transaction>
    ): List<Pair<Int, List<Transaction>>> {

        return transactions
            .groupBy {
                extractYear(it.date)
            }
            .toList()
            .sortedByDescending {
                it.first
            }
    }

    private fun extractYear(
        date: String
    ): Int {

        return try {
            date.substringAfterLast("/")
                .toInt()
        } catch (_: Exception) {
            0
        }
    }

    fun getMonthName(
        month: Int
    ): String {

        return when (month) {

            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"

            else -> ""
        }
    }

    fun toBanglaNumber(
        number: Int
    ): String {

        val english =
            number.toString()

        val banglaDigits =
            charArrayOf(
                '০',
                '১',
                '২',
                '৩',
                '৪',
                '৫',
                '৬',
                '৭',
                '৮',
                '৯'
            )

        return english.map { char ->

            if (char.isDigit()) {
                banglaDigits[
                    char.digitToInt()
                ]
            } else {
                char
            }

        }.joinToString("")
    }
}