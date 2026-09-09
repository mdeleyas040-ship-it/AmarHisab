package com.eleyas.expensetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import com.eleyas.expensetracker.model.Transaction
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    // =========================================================
    // COMMON PDF WRITER
    // PDF IS FIRST CREATED COMPLETELY IN MEMORY
    // THEN WRITTEN TO THE SELECTED FILE
    // =========================================================

    private fun writePdf(
        context: Context,
        uri: Uri,
        document: PdfDocument,
        successMessage: String
    ): Boolean {

        return try {

            // -------------------------------------------------
            // Step 1: Convert complete PdfDocument to bytes
            // -------------------------------------------------

            val pdfBytes =
                ByteArrayOutputStream().use { memoryStream ->

                    document.writeTo(memoryStream)

                    memoryStream.flush()

                    memoryStream.toByteArray()
                }

            // PDF must contain actual data
            if (pdfBytes.isEmpty()) {
                throw Exception(
                    "PDF তৈরি হয়েছে কিন্তু কোনো data পাওয়া যায়নি"
                )
            }

            // -------------------------------------------------
            // Step 2: Open selected SAF file for writing
            // -------------------------------------------------

            val outputStream: OutputStream? =
                context.contentResolver.openOutputStream(
                    uri,
                    "w"
                )

            if (outputStream == null) {
                throw Exception(
                    "Selected PDF file খুলতে পারছে না"
                )
            }

            // -------------------------------------------------
            // Step 3: Write complete PDF bytes
            // -------------------------------------------------

            outputStream.use { stream ->

                stream.write(
                    pdfBytes
                )

                stream.flush()
            }

            // -------------------------------------------------
            // Step 4: Verify something was actually written
            // -------------------------------------------------

            Toast.makeText(
                context,
                "$successMessage (${pdfBytes.size / 1024} KB)",
                Toast.LENGTH_LONG
            ).show()

            true

        } catch (e: Exception) {

            Toast.makeText(
                context,
                "❌ PDF Error: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()

            false

        } finally {

            try {
                document.close()
            } catch (_: Exception) {
            }
        }
    }


    // =========================================================
    // MAIN FINANCIAL REPORT PDF
    // =========================================================

    fun exportToPdf(
        context: Context,
        uri: Uri,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double
    ) {

        val pdfDocument =
            PdfDocument()

        try {

            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1

            var pageInfo =
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()

            var page =
                pdfDocument.startPage(
                    pageInfo
                )

            var canvas: Canvas =
                page.canvas

            val paint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                )

            val dateStr =
                SimpleDateFormat(
                    "dd MMMM yyyy, HH:mm",
                    Locale.getDefault()
                ).format(Date())

            var y = 45f

            // =================================================
            // TITLE
            // =================================================

            paint.color =
                Color.BLACK

            paint.textSize =
                20f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                "Amar Hisab - Financial Report",
                40f,
                y,
                paint
            )

            y += 25f

            // =================================================
            // DATE
            // =================================================

            paint.textSize =
                10f

            paint.isFakeBoldText =
                false

            canvas.drawText(
                "Report Generated: $dateStr",
                40f,
                y,
                paint
            )

            y += 30f

            // =================================================
            // SUMMARY BOX
            // =================================================

            paint.color =
                Color.rgb(
                    240,
                    240,
                    240
                )

            canvas.drawRect(
                40f,
                y,
                555f,
                y + 65f,
                paint
            )

            paint.color =
                Color.BLACK

            paint.textSize =
                12f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                "Summary",
                55f,
                y + 20f,
                paint
            )

            paint.textSize =
                10f

            paint.isFakeBoldText =
                false

            canvas.drawText(
                "Total Income: BDT ${money(totalIncome)}",
                55f,
                y + 43f,
                paint
            )

            canvas.drawText(
                "Total Expense: BDT ${money(totalExpense)}",
                245f,
                y + 43f,
                paint
            )

            canvas.drawText(
                "Balance: BDT ${money(balance)}",
                430f,
                y + 43f,
                paint
            )

            y += 95f

            // =================================================
            // TABLE HEADER
            // =================================================

            paint.textSize =
                10f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                "Date",
                40f,
                y,
                paint
            )

            canvas.drawText(
                "Category",
                110f,
                y,
                paint
            )

            canvas.drawText(
                "Reason",
                210f,
                y,
                paint
            )

            canvas.drawText(
                "Type",
                390f,
                y,
                paint
            )

            canvas.drawText(
                "Amount",
                480f,
                y,
                paint
            )

            y += 6f

            canvas.drawLine(
                40f,
                y,
                555f,
                y,
                paint
            )

            y += 18f

            // =================================================
            // TRANSACTIONS
            // =================================================

            paint.isFakeBoldText =
                false

            transactions.forEach { trans ->

                if (y > pageHeight - 55f) {

                    pdfDocument.finishPage(
                        page
                    )

                    pageNumber++

                    pageInfo =
                        PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            pageNumber
                        ).create()

                    page =
                        pdfDocument.startPage(
                            pageInfo
                        )

                    canvas =
                        page.canvas

                    y = 45f

                    // Repeat header
                    paint.color =
                        Color.BLACK

                    paint.textSize =
                        10f

                    paint.isFakeBoldText =
                        true

                    canvas.drawText(
                        "Date",
                        40f,
                        y,
                        paint
                    )

                    canvas.drawText(
                        "Category",
                        110f,
                        y,
                        paint
                    )

                    canvas.drawText(
                        "Reason",
                        210f,
                        y,
                        paint
                    )

                    canvas.drawText(
                        "Type",
                        390f,
                        y,
                        paint
                    )

                    canvas.drawText(
                        "Amount",
                        480f,
                        y,
                        paint
                    )

                    y += 6f

                    canvas.drawLine(
                        40f,
                        y,
                        555f,
                        y,
                        paint
                    )

                    y += 18f

                    paint.isFakeBoldText =
                        false
                }

                canvas.drawText(
                    trans.date.take(12),
                    40f,
                    y,
                    paint
                )

                canvas.drawText(
                    trans.category.take(14),
                    110f,
                    y,
                    paint
                )

                canvas.drawText(
                    trans.reason
                        .replace("\n", " ")
                        .take(27),
                    210f,
                    y,
                    paint
                )

                canvas.drawText(
                    trans.type
                        .uppercase()
                        .take(8),
                    390f,
                    y,
                    paint
                )

                canvas.drawText(
                    money(trans.amount),
                    480f,
                    y,
                    paint
                )

                y += 18f
            }

            // Finish final page
            pdfDocument.finishPage(
                page
            )

            // Write complete PDF
            writePdf(
                context = context,
                uri = uri,
                document = pdfDocument,
                successMessage =
                    "✅ PDF Report Saved"
            )

        } catch (e: Exception) {

            try {
                pdfDocument.close()
            } catch (_: Exception) {
            }

            Toast.makeText(
                context,
                "❌ PDF Error: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // =========================================================
    // CSV EXPORT
    // =========================================================

    fun exportToCsv(
        context: Context,
        uri: Uri,
        transactions: List<Transaction>
    ) {

        try {

            val builder =
                StringBuilder()

            builder.append(
                "Date,Type,Category,Reason,Currency,Amount,Wallet\n"
            )

            transactions.forEach { t ->

                val reason =
                    t.reason
                        .replace(",", " ")
                        .replace("\n", " ")

                builder.append(
                    "${t.date}," +
                            "${t.type}," +
                            "${t.category}," +
                            "$reason," +
                            "${t.currency}," +
                            "${t.amount}," +
                            "${t.walletId}\n"
                )
            }

            val outputStream =
                context.contentResolver
                    .openOutputStream(
                        uri,
                        "w"
                    )

            if (outputStream == null) {
                throw Exception(
                    "CSV file খুলতে পারছে না"
                )
            }

            outputStream.use { stream ->

                stream.write(
                    builder
                        .toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )

                stream.flush()
            }

            Toast.makeText(
                context,
                "✅ CSV Report Saved",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                context,
                "❌ CSV Error: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // =========================================================
    // PERSON STATEMENT PDF
    // USED FOR:
    // LOAN + LENDING
    // =========================================================

    fun exportPersonStatement(
        context: Context,
        uri: Uri,
        personName: String,
        initialAmount: Double,
        transactions: List<Pair<String, Double>>,
        isLending: Boolean
    ) {

        val pdfDocument =
            PdfDocument()

        try {

            val pageWidth =
                595

            val pageHeight =
                842

            var pageNumber =
                1

            var pageInfo =
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()

            var page =
                pdfDocument.startPage(
                    pageInfo
                )

            var canvas =
                page.canvas

            val paint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                )

            var y =
                50f

            // =================================================
            // HEADER FUNCTION
            // =================================================

            fun drawHeader() {

                y = 50f

                paint.color =
                    Color.BLACK

                paint.textSize =
                    22f

                paint.isFakeBoldText =
                    true

                canvas.drawText(
                    "Amar Hisab",
                    40f,
                    y,
                    paint
                )

                y += 28f

                paint.textSize =
                    17f

                canvas.drawText(
                    if (isLending) {
                        "Lending Statement"
                    } else {
                        "Loan Statement"
                    },
                    40f,
                    y,
                    paint
                )

                y += 28f

                paint.textSize =
                    9f

                paint.isFakeBoldText =
                    false

                val generated =
                    SimpleDateFormat(
                        "dd MMMM yyyy, HH:mm",
                        Locale.getDefault()
                    ).format(Date())

                canvas.drawText(
                    "Generated: $generated",
                    40f,
                    y,
                    paint
                )

                y += 28f
            }

            // =================================================
            // FIRST PAGE HEADER
            // =================================================

            drawHeader()

            // =================================================
            // PERSON CARD
            // =================================================

            paint.color =
                Color.rgb(
                    240,
                    240,
                    240
                )

            canvas.drawRect(
                40f,
                y,
                555f,
                y + 75f,
                paint
            )

            paint.color =
                Color.BLACK

            paint.textSize =
                10f

            paint.isFakeBoldText =
                false

            canvas.drawText(
                "Person",
                55f,
                y + 22f,
                paint
            )

            paint.textSize =
                16f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                personName.take(35),
                55f,
                y + 45f,
                paint
            )

            paint.textSize =
                10f

            paint.isFakeBoldText =
                false

            canvas.drawText(
                if (isLending) {
                    "Amount Given"
                } else {
                    "Amount Borrowed"
                },
                365f,
                y + 22f,
                paint
            )

            paint.textSize =
                15f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                "BDT ${money(initialAmount)}",
                365f,
                y + 45f,
                paint
            )

            y += 105f

            // =================================================
            // HISTORY HEADER
            // =================================================

            fun drawHistoryHeader(
                continued: Boolean = false
            ) {

                paint.color =
                    Color.BLACK

                paint.textSize =
                    11f

                paint.isFakeBoldText =
                    true

                canvas.drawText(
                    if (continued) {
                        "Payment History (Continued)"
                    } else {
                        "Payment History"
                    },
                    40f,
                    y,
                    paint
                )

                y += 25f

                canvas.drawText(
                    "Date",
                    40f,
                    y,
                    paint
                )

                canvas.drawText(
                    if (isLending) {
                        "Amount Returned"
                    } else {
                        "Amount Paid"
                    },
                    240f,
                    y,
                    paint
                )

                y += 6f

                canvas.drawLine(
                    40f,
                    y,
                    555f,
                    y,
                    paint
                )

                y += 20f

                paint.textSize =
                    10f

                paint.isFakeBoldText =
                    false
            }

            drawHistoryHeader()

            // =================================================
            // HISTORY
            // =================================================

            var totalHistory =
                0.0

            transactions.forEach { item ->

                val date =
                    item.first

                val amount =
                    item.second

                if (y > pageHeight - 90f) {

                    pdfDocument.finishPage(
                        page
                    )

                    pageNumber++

                    pageInfo =
                        PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            pageNumber
                        ).create()

                    page =
                        pdfDocument.startPage(
                            pageInfo
                        )

                    canvas =
                        page.canvas

                    drawHeader()

                    drawHistoryHeader(
                        continued = true
                    )
                }

                canvas.drawText(
                    date.take(20),
                    40f,
                    y,
                    paint
                )

                canvas.drawText(
                    "BDT ${money(amount)}",
                    240f,
                    y,
                    paint
                )

                totalHistory +=
                    amount

                y += 21f
            }

            // =================================================
            // TOTAL SECTION
            // =================================================

            if (y > pageHeight - 150f) {

                pdfDocument.finishPage(
                    page
                )

                pageNumber++

                pageInfo =
                    PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageNumber
                    ).create()

                page =
                    pdfDocument.startPage(
                        pageInfo
                    )

                canvas =
                    page.canvas

                y =
                    60f
            } else {

                y += 10f
            }

            paint.color =
                Color.BLACK

            canvas.drawLine(
                40f,
                y,
                555f,
                y,
                paint
            )

            y += 25f

            paint.textSize =
                11f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                if (isLending) {
                    "Total Returned:"
                } else {
                    "Total Paid:"
                },
                40f,
                y,
                paint
            )

            canvas.drawText(
                "BDT ${money(totalHistory)}",
                240f,
                y,
                paint
            )

            y += 25f

            val remaining =
                (
                        initialAmount -
                                totalHistory
                        ).coerceAtLeast(
                        0.0
                    )

            paint.textSize =
                14f

            paint.isFakeBoldText =
                true

            canvas.drawText(
                if (isLending) {
                    "Remaining Receivable:"
                } else {
                    "Remaining Loan:"
                },
                40f,
                y,
                paint
            )

            canvas.drawText(
                "BDT ${money(remaining)}",
                300f,
                y,
                paint
            )

            y += 35f

            // =================================================
            // FOOTER
            // =================================================

            paint.textSize =
                8f

            paint.isFakeBoldText =
                false

            paint.color =
                Color.DKGRAY

            canvas.drawText(
                "Generated by Amar Hisab",
                40f,
                y,
                paint
            )

            // =================================================
            // FINISH FINAL PAGE
            // =================================================

            pdfDocument.finishPage(
                page
            )

            // =================================================
            // WRITE COMPLETE PDF
            // =================================================

            writePdf(
                context = context,
                uri = uri,
                document = pdfDocument,
                successMessage =
                    "✅ Statement PDF Saved"
            )

        } catch (e: Exception) {

            try {
                pdfDocument.close()
            } catch (_: Exception) {
            }

            Toast.makeText(
                context,
                "❌ Statement PDF Error: ${
                    e.message ?: "Unknown error"
                }",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // =========================================================
    // MONEY FORMAT
    // =========================================================

    private fun money(
        value: Double
    ): String {

        return String.format(
            Locale.US,
            "%,.2f",
            value
        )
    }
}