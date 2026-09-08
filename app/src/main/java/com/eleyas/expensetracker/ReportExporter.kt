package com.eleyas.expensetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import com.eleyas.expensetracker.model.Transaction
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 40f
    private const val RIGHT = 555f

    private fun writePdf(
        context: Context,
        uri: Uri,
        document: PdfDocument,
        successMessage: String
    ): Boolean {
        var success = false

        try {
            context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                document.writeTo(outputStream)
                outputStream.flush()
                success = true
            } ?: throw IllegalStateException("Could not open selected file for writing")

            if (success) {
                Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
            }

            return true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "❌ PDF Error: ${e.message ?: "Unable to write PDF"}",
                Toast.LENGTH_LONG
            ).show()
            return false
        } finally {
            document.close()
        }
    }

    private fun money(value: Double): String {
        return "%,.2f".format(Locale.getDefault(), value)
    }

    private fun drawReportHeader(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 40f, 40f, paint)
        canvas.drawText("Category", 110f, 40f, paint)
        canvas.drawText("Reason", 210f, 40f, paint)
        canvas.drawText("Type", 390f, 40f, paint)
        canvas.drawText("Amount", 480f, 40f, paint)
        canvas.drawLine(40f, 45f, RIGHT, 45f, paint)
        paint.isFakeBoldText = false
    }

    fun exportToPdf(
        context: Context,
        uri: Uri,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double
    ) {
        val pdfDocument = PdfDocument()

        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
            ).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateStr = SimpleDateFormat(
                "dd MMMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(Date())

            var y = 40f

            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("Amar Hisab - Financial Report", LEFT, y, paint)
            y += 25f

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Report Generated: $dateStr", LEFT, y, paint)
            y += 30f

            paint.color = Color.rgb(240, 240, 240)
            canvas.drawRect(LEFT, y, RIGHT, y + 60f, paint)

            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Summary", 55f, y + 20f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Total Income: BDT ${money(totalIncome)}", 55f, y + 40f, paint)
            canvas.drawText("Total Expense: BDT ${money(totalExpense)}", 250f, y + 40f, paint)
            canvas.drawText("Balance: BDT ${money(balance)}", 430f, y + 40f, paint)
            y += 90f

            paint.textSize = 10f
            drawReportHeader(canvas, paint)
            y = 65f

            transactions.forEach { trans ->
                if (y > PAGE_HEIGHT - 50f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        pageNumber
                    ).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawReportHeader(canvas, paint)
                    y = 65f
                }

                canvas.drawText(trans.date.take(14), 40f, y, paint)
                canvas.drawText(trans.category.take(15), 110f, y, paint)
                canvas.drawText(trans.reason.take(30), 210f, y, paint)
                canvas.drawText(trans.type.uppercase(Locale.getDefault()).take(10), 390f, y, paint)
                canvas.drawText("${money(trans.amount)}", 480f, y, paint)
                y += 18f
            }

            pdfDocument.finishPage(page)
            writePdf(context, uri, pdfDocument, "✅ PDF Report Saved")
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(
                context,
                "❌ PDF Error: ${e.message ?: "Unable to create PDF"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun exportToCsv(
        context: Context,
        uri: Uri,
        transactions: List<Transaction>
    ) {
        try {
            val builder = StringBuilder()
            builder.append("Date,Type,Category,Reason,Currency,Amount,Wallet\n")

            transactions.forEach { t ->
                val reason = t.reason.replace(",", " ")
                builder.append(
                    "${t.date},${t.type},${t.category},$reason,${t.currency},${t.amount},${t.walletId}\n"
                )
            }

            context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                outputStream.write(builder.toString().toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: throw IllegalStateException("Could not open selected file for writing")

            Toast.makeText(context, "✅ CSV Report Saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "❌ CSV Error: ${e.message ?: "Unable to write CSV"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun exportPersonStatement(
        context: Context,
        uri: Uri,
        personName: String,
        initialAmount: Double,
        transactions: List<Pair<String, Double>>,
        isLending: Boolean
    ) {
        val pdfDocument = PdfDocument()

        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
            ).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            var y = 50f

            fun drawStatementHeader() {
                paint.color = Color.BLACK
                paint.textSize = 22f
                paint.isFakeBoldText = true
                canvas.drawText("Transaction Statement", LEFT, 50f, paint)

                paint.textSize = 14f
                paint.isFakeBoldText = false
                canvas.drawText("Person: $personName", LEFT, 80f, paint)

                val typeStr = if (isLending) {
                    "Amount Given: BDT ${money(initialAmount)}"
                } else {
                    "Amount Borrowed: BDT ${money(initialAmount)}"
                }
                canvas.drawText(typeStr, LEFT, 105f, paint)

                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("Date", LEFT, 145f, paint)
                canvas.drawText("Amount Received/Paid", 200f, 145f, paint)
                canvas.drawLine(LEFT, 150f, 500f, 150f, paint)
                paint.isFakeBoldText = false
            }

            drawStatementHeader()
            y = 170f
            var totalHistory = 0.0

            transactions.forEach { (date, amount) ->
                if (y > PAGE_HEIGHT - 70f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        pageNumber
                    ).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawStatementHeader()
                    y = 170f
                }

                canvas.drawText(date.take(25), LEFT, y, paint)
                canvas.drawText("BDT ${money(amount)}", 200f, y, paint)
                totalHistory += amount
                y += 20f
            }

            if (y > PAGE_HEIGHT - 120f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH,
                    PAGE_HEIGHT,
                    pageNumber
                ).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            y += 10f
            canvas.drawLine(LEFT, y, 500f, y, paint)
            y += 25f

            paint.isFakeBoldText = true
            val remaining = initialAmount - totalHistory
            canvas.drawText("Total Returned: BDT ${money(totalHistory)}", LEFT, y, paint)
            y += 20f
            canvas.drawText("Net Balance: BDT ${money(remaining)}", LEFT, y, paint)

            pdfDocument.finishPage(page)
            writePdf(context, uri, pdfDocument, "✅ Statement Saved")
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(
                context,
                "❌ PDF Error: ${e.message ?: "Unable to create statement"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
