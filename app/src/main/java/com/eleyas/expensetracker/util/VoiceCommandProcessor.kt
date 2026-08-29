package com.eleyas.expensetracker.util

/**
 * Parses Bengali/English text to extract Transaction information.
 */
object VoiceCommandProcessor {

    data class VoiceResult(
        val amount: Double?,
        val type: String, // "income" or "expense"
        val category: String,
        val originalText: String
    )

    fun processCommand(text: String): VoiceResult {
        val cleanText = text.lowercase()
        
        // 1. Extract Amount (Supports both English and Bengali digits)
        val amount = extractAmount(cleanText)

        // 2. Determine Type
        val isIncome = cleanText.contains("পেলাম") || cleanText.contains("জমা") || 
                       cleanText.contains("received") || cleanText.contains("income")
        val type = if (isIncome) "income" else "expense"

        // 3. Map Category
        val category = mapCategory(cleanText, type)

        return VoiceResult(
            amount = amount,
            type = type,
            category = category,
            originalText = text
        )
    }

    private fun extractAmount(text: String): Double? {
        // Convert Bengali digits to English
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        var processedText = text
        bengaliDigits.forEachIndexed { index, c ->
            processedText = processedText.replace(c, index.toString()[0])
        }

        val regex = Regex("(\\d+(\\.\\d+)?)")
        val match = regex.find(processedText)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun mapCategory(text: String, type: String): String {
        val mapping = mapOf(
            "বাজার" to "Food",
            "খাওয়া" to "Food",
            "নাস্তা" to "Food",
            "রেস্টুরেন্ট" to "Food",
            "বাস" to "Transport",
            "রিকশা" to "Transport",
            "যাতায়াত" to "Transport",
            "ভাড়া" to "Transport",
            "মোবাইল" to "Mobile",
            "রিচার্জ" to "Mobile",
            "কারেন্ট" to "Bills",
            "বিদ্যুৎ" to "Bills",
            "shopping" to "Shopping",
            "জামা" to "Shopping",
            "কাপড়" to "Shopping",
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
