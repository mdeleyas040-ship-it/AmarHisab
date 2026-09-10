package com.eleyas.expensetracker.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.eleyas.expensetracker.model.Transaction

fun buildTransactionReceipt(transaction: Transaction, customNote: String): String {
    val title = transaction.reason.ifBlank { transaction.category }
    return buildString {
        appendLine("🧾 Amar Hisab রসিদ")
        appendLine("বিবরণ: $title")
        appendLine("ধরন: ${if (transaction.type == "home_expense") "বাড়ির খরচ" else "খরচ"}")
        appendLine("পরিমাণ: ${transaction.currency} ${formatMoney(transaction.amount)}")
        appendLine("তারিখ: ${displayTransactionDate(transaction.date)}")
        appendLine("ক্যাটাগরি: ${transaction.category}")
        if (customNote.isNotBlank()) {
            appendLine("নোট: ${customNote.trim()}")
        }
        appendLine()
        appendLine("এই রসিদটি Amar Hisab অ্যাপ থেকে তৈরি করা হয়েছে।")
    }
}

fun shareTextReceipt(
    context: Context,
    text: String,
    chooserTitle: String = "রসিদ শেয়ার করুন"
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    if (intent.resolveActivity(context.packageManager) == null) {
        Toast.makeText(context, "শেয়ার করার কোনো অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
        return
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
