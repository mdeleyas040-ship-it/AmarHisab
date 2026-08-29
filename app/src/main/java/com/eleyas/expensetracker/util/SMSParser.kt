package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.SMSSuggestion
import java.text.SimpleDateFormat
import java.util.*

object SMSParser {

    fun parseSMS(sender: String, message: String): SMSSuggestion? {
        val lowerMsg = message.lowercase()
        
        return when {
            sender.contains("bkash", ignoreCase = true) || lowerMsg.contains("bkash") -> 
                parseBkash(sender, message)
            sender.contains("nagad", ignoreCase = true) || lowerMsg.contains("nagad") -> 
                parseNagad(sender, message)
            sender.contains("rocket", ignoreCase = true) || lowerMsg.contains("rocket") -> 
                parseRocket(sender, message)
            sender.contains("bank", ignoreCase = true) -> 
                parseBank(sender, message)
            else -> null
        }
    }

    private fun parseBkash(sender: String, message: String): SMSSuggestion? {
        val amountPattern = """(?:Sent|Received|Sent|Cash out|Paid)\s+[\w\s]+(?:BDT\s+)?([0-9,]+)""".toRegex(RegexOption.IGNORE_CASE)
        val amount = amountPattern.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val type = when {
            message.contains("Sent", ignoreCase = true) || message.contains("transferred", ignoreCase = true) -> "expense"
            message.contains("Received", ignoreCase = true) -> "income"
            message.contains("Cash out", ignoreCase = true) -> "expense"
            message.contains("Paid", ignoreCase = true) -> "expense"
            else -> "expense"
        }

        return SMSSuggestion(
            bankName = "bKash",
            senderName = sender,
            amount = amount,
            transactionType = type,
            description = message.split("\n").firstOrNull() ?: "bKash Transaction",
            category = if (type == "expense") "ডিজিটাল পেমেন্ট" else "ডিজিটাল আয়",
            rawSMS = message
        )
    }

    private fun parseNagad(sender: String, message: String): SMSSuggestion? {
        val amountPattern = """(?:টাকা|BDT)\s+([0-9,]+)""".toRegex(RegexOption.IGNORE_CASE)
        val amount = amountPattern.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val type = when {
            message.contains("পাঠিয়েছেন", ignoreCase = true) || message.contains("sent", ignoreCase = true) -> "expense"
            message.contains("পেয়েছেন", ignoreCase = true) || message.contains("received", ignoreCase = true) -> "income"
            else -> "expense"
        }

        return SMSSuggestion(
            bankName = "Nagad",
            senderName = sender,
            amount = amount,
            transactionType = type,
            description = "Nagad Transaction",
            category = if (type == "expense") "ডিজিটাল পেমেন্ট" else "ডিজিটাল আয়",
            rawSMS = message
        )
    }

    private fun parseRocket(sender: String, message: String): SMSSuggestion? {
        val amountPattern = """([0-9,]+)\s*BDT""".toRegex(RegexOption.IGNORE_CASE)
        val amount = amountPattern.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val type = when {
            message.contains("transferred", ignoreCase = true) || message.contains("sent", ignoreCase = true) -> "expense"
            message.contains("received", ignoreCase = true) -> "income"
            message.contains("withdraw", ignoreCase = true) -> "expense"
            else -> "expense"
        }

        return SMSSuggestion(
            bankName = "Rocket",
            senderName = sender,
            amount = amount,
            transactionType = type,
            description = "Rocket Transaction",
            category = if (type == "expense") "ডিজিটাল পেমেন্ট" else "ডিজিটাল আয়",
            rawSMS = message
        )
    }

    private fun parseBank(sender: String, message: String): SMSSuggestion? {
        val amountPattern = """(?:Taka|Amount|BDT|৳)\s*([0-9,]+)""".toRegex(RegexOption.IGNORE_CASE)
        val amount = amountPattern.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val type = when {
            message.contains("withdrawal", ignoreCase = true) || message.contains("debited", ignoreCase = true) -> "expense"
            message.contains("deposit", ignoreCase = true) || message.contains("credited", ignoreCase = true) -> "income"
            message.contains("transferred", ignoreCase = true) -> "expense"
            else -> "expense"
        }

        val bankName = extractBankName(sender)

        return SMSSuggestion(
            bankName = bankName,
            senderName = sender,
            amount = amount,
            transactionType = type,
            description = "Bank Transaction",
            category = if (type == "expense") "ব্যাংক" else "ব্যাংক জমা",
            rawSMS = message
        )
    }

    private fun extractBankName(sender: String): String {
        return when {
            sender.contains("dbbl", ignoreCase = true) -> "Dutch Bangla Bank"
            sender.contains("ebl", ignoreCase = true) -> "Eastern Bank"
            sender.contains("gtb", ignoreCase = true) -> "Guarantee Trust Bank"
            sender.contains("ib", ignoreCase = true) -> "IBBL"
            sender.contains("ucb", ignoreCase = true) -> "United Commercial Bank"
            sender.contains("scb", ignoreCase = true) -> "Standard Chartered Bank"
            else -> sender
        }
    }

    fun getDateFromSMS(message: String): String {
        val datePatterns = listOf(
            """(\d{2}/\d{2}/\d{4})""",
            """(\d{4}-\d{2}-\d{2})""",
            """(\d{2}-\d{2}-\d{4})"""
        )

        for (pattern in datePatterns) {
            val match = pattern.toRegex().find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
}
