package com.eleyas.expensetracker

import com.eleyas.expensetracker.model.*
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
