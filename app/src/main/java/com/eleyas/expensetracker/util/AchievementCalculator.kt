package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.AchievementBadge
import com.eleyas.expensetracker.model.CategoryBudget
import com.eleyas.expensetracker.model.Transaction
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object AchievementCalculator {
    private val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    fun evaluate(
        transactions: List<Transaction>,
        budgets: List<CategoryBudget>,
        usdToBdt: Double,
        usdToMvr: Double
    ): List<AchievementBadge> {
        val qualifyingMonths = budgets
            .groupBy { it.month }
            .mapNotNull { (month, monthBudgets) ->
                val budgetTotal = monthBudgets.sumOf { it.limit.coerceAtLeast(0.0) }
                if (budgetTotal <= 0.0) return@mapNotNull null

                val expenseTotal = transactions
                    .filter { it.type == "expense" && it.date.endsWith(month) }
                    .sumOf { convertToBdt(it.amount, it.currency, usdToBdt, usdToMvr) }

                month to (expenseTotal < budgetTotal)
            }
            .filter { it.second }
            .mapNotNull { (month, _) -> monthIndex(month)?.let { month to it } }
            .sortedBy { it.second }

        var longestStreak = 0
        var currentStreak = 0
        var previousMonth: Int? = null
        qualifyingMonths.forEach { (_, monthIndex) ->
            currentStreak = if (previousMonth != null && monthIndex == previousMonth!! + 1) {
                currentStreak + 1
            } else {
                1
            }
            longestStreak = maxOf(longestStreak, currentStreak)
            previousMonth = monthIndex
        }

        return listOf(
            badge("super_saver", "Super Saver", "এক মাস বাজেটের চেয়ে কম খরচ করেছেন", "🌟", 1, longestStreak),
            badge("budget_master", "Budget Master", "টানা ৩ মাস বাজেটের মধ্যে থেকেছেন", "🏆", 3, longestStreak),
            badge("savings_legend", "Savings Legend", "টানা ৬ মাস দুর্দান্ত সঞ্চয় করেছেন", "💎", 6, longestStreak)
        )
    }

    private fun badge(
        id: String,
        title: String,
        description: String,
        icon: String,
        requiredMonths: Int,
        longestStreak: Int
    ) = AchievementBadge(
        id = id,
        title = title,
        description = description,
        icon = icon,
        requiredMonths = requiredMonths,
        earned = longestStreak >= requiredMonths,
        progressMonths = minOf(longestStreak, requiredMonths)
    )

    private fun monthIndex(month: String): Int? = try {
        monthFormat.parse(month)?.let { date ->
            val calendar = java.util.Calendar.getInstance().apply { time = date }
            calendar.get(java.util.Calendar.YEAR) * 12 +
                calendar.get(java.util.Calendar.MONTH)
        }
    } catch (_: ParseException) {
        null
    }

    private fun convertToBdt(
        amount: Double,
        currency: String,
        usdToBdt: Double,
        usdToMvr: Double
    ): Double = when (currency) {
        "BDT" -> amount
        "USD" -> amount * usdToBdt
        "MVR" -> if (usdToMvr > 0.0) amount * (usdToBdt / usdToMvr) else 0.0
        else -> 0.0
    }
}
