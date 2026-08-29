package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

object FinancialInsights {

    fun generateInsight(transactions: List<Transaction>): String {
        if (transactions.isEmpty()) {
            return "💡 টিপস: নিয়মিত আপনার দৈনিক আয় ও খরচ এন্ট্রি করুন, তাহলে সঠিক হিসাব ও চমৎকার ইনসাই্ট পাবেন!"
        }

        val cal = Calendar.getInstance()
        val currentMonthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val currentMonthStr = currentMonthFormat.format(cal.time)

        // Previous month
        cal.add(Calendar.MONTH, -1)
        val prevMonthStr = currentMonthFormat.format(cal.time)

        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val currentMonthT = transactions.filter { t ->
            try {
                val d = parser.parse(t.date)
                d != null && currentMonthFormat.format(d) == currentMonthStr
            } catch (e: Exception) {
                false
            }
        }

        val prevMonthT = transactions.filter { t ->
            try {
                val d = parser.parse(t.date)
                d != null && currentMonthFormat.format(d) == prevMonthStr
            } catch (e: Exception) {
                false
            }
        }

        val currentIncome = currentMonthT.filter { it.type == "income" }.sumOf { it.amount }
        val currentExpense = currentMonthT.filter { it.type != "income" }.sumOf { it.amount }

        val prevIncome = prevMonthT.filter { it.type == "income" }.sumOf { it.amount }
        val prevExpense = prevMonthT.filter { it.type != "income" }.sumOf { it.amount }

        // Savings rate
        if (currentIncome > 0) {
            val savingsRate = ((currentIncome - currentExpense) / currentIncome) * 100
            if (savingsRate >= 30) {
                return "🌟 দুর্দান্ত! আপনার আয়ের বিপরীতে সঞ্চয়ের হার খুব ভালো (${savingsRate.toInt()}%) আছে। এভাবেই সঞ্চয় চালিয়ে যান!"
            } else if (savingsRate < 0) {
                return "⚠️ সতর্কবার্তা: চলতি মাসে আপনার খরচ আয়ের তুলনায় বেশি হয়ে গেছে। খরচ কিছুটা নিয়ন্ত্রণ করুন।"
            }
        }

        // Compare with previous month expense
        if (prevExpense > 0 && currentExpense > 0) {
            val diff = currentExpense - prevExpense
            val percentage = (kotlin.math.abs(diff) / prevExpense) * 100
            if (diff > 0 && percentage >= 10) {
                return "📈 লক্ষ্য করুন: গত মাসের তুলনায় এই মাসে আপনার মোট খরচ প্রায় ${percentage.toInt()}% বেশি হয়েছে।"
            } else if (diff < 0 && percentage >= 10) {
                return "👏 দারুণ উন্নতি! গত মাসের তুলনায় এই মাসে আপনার খরচ ${percentage.toInt()}% কমেছে।"
            }
        }

        // Category specific check (e.g. Food / খাবার)
        val currentFoodExpense = currentMonthT.filter { it.category.contains("খাবার", true) || it.category.contains("Food", true) }.sumOf { it.amount }
        val prevFoodExpense = prevMonthT.filter { it.category.contains("খাবার", true) || it.category.contains("Food", true) }.sumOf { it.amount }
        if (prevFoodExpense > 0 && currentFoodExpense > prevFoodExpense) {
            val foodDiff = ((currentFoodExpense - prevFoodExpense) / prevFoodExpense) * 100
            if (foodDiff >= 15) {
                return "🍲 টিপস: গত মাসের তুলনায় এই মাসে আপনার খাবারের খরচ ${foodDiff.toInt()}% বেশি হয়েছে।"
            }
        }

        return "💡 স্মার্ট টিপস: নিয়মিত বাজেট মেনে চললে এবং ছোট ছোট খরচ ট্র্যাক করলে মাস শেষে আপনার পকেটে উদ্বৃত্ত টাকা থাকবে।"
    }
}
