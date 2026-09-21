package com.eleyas.expensetracker

import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.util.MonthlySummary
import com.eleyas.expensetracker.util.MonthlySummaryUtils
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun exportToPdf(
        context: Context,
        uri: Uri,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas
            val paint = Paint()
            val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            var y = 40f

            // Title
            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("Amar Hisab - Financial Report", 40f, y, paint)
            y += 25f

            // Date
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Report Generated: $dateStr", 40f, y, paint)
            y += 30f

            // Summary Box
            paint.color = Color.rgb(240, 240, 240)
            canvas.drawRect(40f, y, 555f, y + 60f, paint)
            
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Summary", 55f, y + 20f, paint)
            
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Total Income: BDT ${"%,.2f".format(totalIncome)}", 55f, y + 40f, paint)
            canvas.drawText("Total Expense: BDT ${"%,.2f".format(totalExpense)}", 250f, y + 40f, paint)
            canvas.drawText("Balance: BDT ${"%,.2f".format(balance)}", 430f, y + 40f, paint)
            y += 90f

            // Table Header
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Date", 40f, y, paint)
            canvas.drawText("Category", 110f, y, paint)
            canvas.drawText("Reason", 210f, y, paint)
            canvas.drawText("Type", 390f, y, paint)
            canvas.drawText("Amount", 480f, y, paint)
            
            y += 5f
            canvas.drawLine(40f, y, 555f, y, paint)
            y += 15f

            // Table Content
            paint.isFakeBoldText = false
            
            transactions.forEach { trans ->
                if (y > pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                    
                    // Repeat Header on new page
                    paint.isFakeBoldText = true
                    canvas.drawText("Date", 40f, y, paint)
                    canvas.drawText("Category", 110f, y, paint)
                    canvas.drawText("Reason", 210f, y, paint)
                    canvas.drawText("Type", 390f, y, paint)
                    canvas.drawText("Amount", 480f, y, paint)
                    y += 5f
                    canvas.drawLine(40f, y, 555f, y, paint)
                    y += 15f
                    paint.isFakeBoldText = false
                }
                
                canvas.drawText(trans.date, 40f, y, paint)
                canvas.drawText(trans.category.take(15), 110f, y, paint)
                canvas.drawText(trans.reason.take(30), 210f, y, paint)
                canvas.drawText(trans.type.uppercase(), 390f, y, paint)
                canvas.drawText("${"%,.2f".format(trans.amount)}", 480f, y, paint)
                
                y += 18f
            }

            pdfDocument.finishPage(page)
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            Toast.makeText(context, "✅ PDF Report Saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    fun exportMonthlySummaryToPdf(
        context: Context,
        uri: Uri,
        summary: MonthlySummary,
        usdToBdt: Double,
        usdToMvr: Double
    ) {
        try {
            fun toBdt(transaction: Transaction): Double = when (transaction.currency) {
                "BDT" -> transaction.amount
                "USD" -> transaction.amount * usdToBdt
                "MVR" -> if (usdToMvr > 0) transaction.amount * (usdToBdt / usdToMvr) else 0.0
                else -> transaction.amount
            }

            val totalIncome = summary.incomeTransactions.sumOf(::toBdt)
            val totalExpense = summary.expenseTransactions.sumOf(::toBdt)
            val net = totalIncome - totalExpense
            val categoryTotals = summary.expenseTransactions
                .groupBy { it.category.ifBlank { "Other" } }
                .mapValues { (_, list) -> list.sumOf(::toBdt) }
                .toList()
                .sortedByDescending { it.second }

            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            var y = 46f

            fun text(value: String, x: Float, size: Float, bold: Boolean = false) {
                paint.color = Color.BLACK
                paint.textSize = size
                paint.isFakeBoldText = bold
                canvas.drawText(value, x, y, paint)
            }

            fun finishAndStartPage() {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 46f
                text("Amar Hisab - Monthly Summary", 40f, 16f, true)
                y += 22f
                text(MonthlySummaryUtils.displayPeriod(summary.start, summary.end), 40f, 10f)
                y += 28f
            }

            text("Amar Hisab - Monthly Summary", 40f, 22f, true)
            y += 28f
            text("Period: " + MonthlySummaryUtils.displayPeriod(summary.start, summary.end), 40f, 11f)
            y += 28f

            paint.color = Color.rgb(245, 245, 245)
            canvas.drawRoundRect(40f, y - 18f, 555f, y + 60f, 12f, 12f, paint)
            text("Total Income", 55f, 10f, true)
            text("BDT " + "%.2f".format(totalIncome), 55f, 30f, true)
            text("Total Expense", 235f, 10f, true)
            text("BDT " + "%.2f".format(totalExpense), 235f, 30f, true)
            text("Net Cash Flow", 410f, 10f, true)
            text("BDT " + "%.2f".format(net), 410f, 30f, true)
            y += 90f

            if (categoryTotals.isNotEmpty()) {
                text("Expense by Category", 40f, 14f, true)
                y += 22f
                categoryTotals.forEach { (category, amount) ->
                    if (y > pageHeight - 70) finishAndStartPage()
                    text(category.take(30), 45f, 10f)
                    text("BDT " + "%.2f".format(amount), 430f, 10f)
                    y += 18f
                }
                y += 12f
            }

            text("Transactions", 40f, 14f, true)
            y += 22f
            paint.color = Color.DKGRAY
            paint.textSize = 9f
            paint.isFakeBoldText = true
            canvas.drawText("Date", 40f, y, paint)
            canvas.drawText("Category", 105f, y, paint)
            canvas.drawText("Reason", 220f, y, paint)
            canvas.drawText("Type", 405f, y, paint)
            canvas.drawText("Amount", 480f, y, paint)
            y += 8f
            canvas.drawLine(40f, y, 555f, y, paint)
            y += 16f

            summary.transactions.forEach { transaction ->
                if (y > pageHeight - 45) {
                    finishAndStartPage()
                    paint.color = Color.DKGRAY
                    paint.textSize = 9f
                    paint.isFakeBoldText = true
                    canvas.drawText("Date", 40f, y, paint)
                    canvas.drawText("Category", 105f, y, paint)
                    canvas.drawText("Reason", 220f, y, paint)
                    canvas.drawText("Type", 405f, y, paint)
                    canvas.drawText("Amount", 480f, y, paint)
                    y += 8f
                    canvas.drawLine(40f, y, 555f, y, paint)
                    y += 16f
                }

                paint.color = Color.BLACK
                paint.textSize = 8.5f
                paint.isFakeBoldText = false
                canvas.drawText(transaction.date, 40f, y, paint)
                canvas.drawText(transaction.category.take(17), 105f, y, paint)
                canvas.drawText(transaction.reason.take(28), 220f, y, paint)
                canvas.drawText(transaction.type.uppercase().take(10), 405f, y, paint)
                canvas.drawText("%.2f".format(toBdt(transaction)), 480f, y, paint)
                y += 17f
            }

            if (summary.transactions.isEmpty()) {
                text("No transactions recorded in this period.", 40f, 10f)
                y += 20f
            }

            document.finishPage(page)
            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            document.close()
            Toast.makeText(context, "✅ Monthly PDF Saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Monthly PDF Error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun exportToCsv(context: Context, uri: Uri, transactions: List<Transaction>) {
        try {
            val builder = StringBuilder()
            builder.append("Date,Type,Category,Reason,Currency,Amount,Wallet\n")
            
            transactions.forEach { t ->
                val reason = t.reason.replace(",", " ")
                builder.append("${t.date},${t.type},${t.category},$reason,${t.currency},${t.amount},${t.walletId}\n")
            }
            
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.use { it.write(builder.toString().toByteArray()) }
            Toast.makeText(context, "✅ CSV Report Saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ CSV Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportPersonStatement(
        context: Context, 
        uri: Uri, 
        personName: String,
        initialAmount: Double,
        transactions: List<Pair<String, Double>>, // Date to Amount
        isLending: Boolean
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()
            var y = 50f

            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("Transaction Statement", 40f, y, paint)
            y += 30f
            
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Person: $personName", 40f, y, paint)
            y += 20f
            
            val typeStr = if (isLending) "Amount Given: BDT ${"%,.2f".format(initialAmount)}" 
                          else "Amount Borrowed: BDT ${"%,.2f".format(initialAmount)}"
            canvas.drawText(typeStr, 40f, y, paint)
            y += 40f

            // History Table
            paint.isFakeBoldText = true
            canvas.drawText("Date", 40f, y, paint)
            canvas.drawText("Amount Received/Paid", 200f, y, paint)
            y += 5f
            canvas.drawLine(40f, y, 500f, y, paint)
            y += 20f
            
            paint.isFakeBoldText = false
            var totalHistory = 0.0
            transactions.forEach { (date, amt) ->
                canvas.drawText(date, 40f, y, paint)
                canvas.drawText("BDT ${"%,.2f".format(amt)}", 200f, y, paint)
                totalHistory += amt
                y += 20f
            }
            
            y += 10f
            canvas.drawLine(40f, y, 500f, y, paint)
            y += 25f
            
            paint.isFakeBoldText = true
            val remaining = initialAmount - totalHistory
            canvas.drawText("Total Returned: BDT ${"%,.2f".format(totalHistory)}", 40f, y, paint)
            y += 20f
            canvas.drawText("Net Balance: BDT ${"%,.2f".format(remaining)}", 40f, y, paint)

            pdfDocument.finishPage(page)
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            Toast.makeText(context, "✅ Statement Saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
