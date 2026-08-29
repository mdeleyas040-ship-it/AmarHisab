package com.eleyas.expensetracker.util

import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

val ScreenHorizontalPadding = 16.dp
val SectionSpacing = 12.dp
val CardPadding = 16.dp
val CardRadius = 18.dp

val MoneyFormatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

fun formatMoney(amount: Double): String = MoneyFormatter.format(kotlin.math.round(amount))

fun displayTransactionDate(value: String): String = try {
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(value)?.let { 
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(it) 
    } ?: value
} catch (_: Exception) { value }

fun displayLoanDate(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val parsedDate = inputFormat.parse(date)
        if (parsedDate != null) outputFormat.format(parsedDate) else date
    } catch (e: Exception) {
        date
    }
}

fun sortTransactionsByDate(list: List<Transaction>): List<Transaction> = list.sortedWith(
    compareByDescending<Transaction> { 
        try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)?.time ?: 0L } 
        catch (_: Exception) { 0L } 
    }.thenByDescending { it.id }
)

fun groupTransactionsByDate(list: List<Transaction>): Map<String, List<Transaction>> {
    val sorted = sortTransactionsByDate(list)
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    val yesterday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)

    return sorted.groupBy { 
        when (it.date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> displayTransactionDate(it.date)
        }
    }
}
