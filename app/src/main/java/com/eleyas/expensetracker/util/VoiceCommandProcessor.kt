package com.eleyas.expensetracker.util

/**
 * Parses Bengali/English speech text into safe Income/Expense transaction hints.
 * This processor never saves a transaction; the normal Add Transaction dialog
 * remains the confirmation step.
 */
object VoiceCommandProcessor {

    data class VoiceResult(
        val amount: Double?,
        val type: String, // "income" or "expense"
        val category: String,
        val originalText: String
    )

    fun processCommand(text: String): VoiceResult {
        val cleanText = text.lowercase().trim()
        val amount = extractAmount(cleanText)

        val incomeWords = listOf(
            "পেলাম", "পেয়েছি", "পেয়েছি", "জমা", "আয়", "আয়",
            "বেতন", "মাইনে", "বোনাস", "received", "income", "salary", "bonus", "got"
        )
        val expenseWords = listOf(
            "খরচ", "কিনলাম", "কেনাকাটা", "দিলাম", "দিয়েছি", "দিয়েছি",
            "expense", "spent", "paid", "purchase", "bought"
        )

        val isIncome = incomeWords.any(cleanText::contains)
        val isExplicitExpense = expenseWords.any(cleanText::contains)
        val type = if (isIncome && !isExplicitExpense) "income" else "expense"
        val category = mapCategory(cleanText, type)

        return VoiceResult(
            amount = amount,
            type = type,
            category = category,
            originalText = text
        )
    }

    private fun extractAmount(text: String): Double? {
        val normalized = buildString(text.length) {
            for (char in text) {
                append(
                    when (char) {
                        '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                        '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                        '٫' -> '.'
                        else -> char
                    }
                )
            }
        }

        // Match grouped thousands before decimal values so 1,500 is not read as 1.50.
        val amountPattern = Regex("(?<![\d.])(?:\d{1,3}(?:,\d{3})+|\d+(?:[.]\d{1,2})?)")
        val candidates = amountPattern.findAll(normalized).map { it.value }

        return candidates
            .mapNotNull { candidate ->
                candidate.replace(",", "").toDoubleOrNull()
            }
            .firstOrNull { it > 0.0 }
    }

    private fun mapCategory(text: String, type: String): String {
        val mapping = linkedMapOf(
            "বাজার" to "Food",
            "খাওয়া" to "Food",
            "খাওয়া" to "Food",
            "নাস্তা" to "Food",
            "রেস্টুরেন্ট" to "Food",
            "food" to "Food",
            "বাস" to "Transport",
            "রিকশা" to "Transport",
            "যাতায়াত" to "Transport",
            "যাতায়াত" to "Transport",
            "ভাড়া" to "Transport",
            "ভাড়া" to "Transport",
            "transport" to "Transport",
            "মোবাইল" to "Mobile",
            "রিচার্জ" to "Mobile",
            "mobile" to "Mobile",
            "কারেন্ট" to "Bills",
            "বিদ্যুৎ" to "Bills",
            "বিল" to "Bills",
            "bill" to "Bills",
            "shopping" to "Shopping",
            "শপিং" to "Shopping",
            "জামা" to "Shopping",
            "কাপড়" to "Shopping",
            "কাপড়" to "Shopping",
            "বেতন" to "Salary",
            "salary" to "Salary",
            "বোনাস" to "Bonus",
            "bonus" to "Bonus"
        )

        for ((keyword, category) in mapping) {
            if (text.contains(keyword)) return category
        }

        return if (type == "income") "Other Income" else "Other Expense"
    }
}
