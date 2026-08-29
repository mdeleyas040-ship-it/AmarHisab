package com.eleyas.expensetracker.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class ReceiptScanResult(
    val amount: Double,
    val date: String,
    val rawText: String
)

object ReceiptScanner {
    suspend fun scanFromUri(context: Context, uri: Uri): ReceiptScanResult? {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            scanFromImage(image)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun scanFromBitmap(bitmap: Bitmap): ReceiptScanResult? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            scanFromImage(image)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun scanFromImage(image: InputImage): ReceiptScanResult? {
        // Latin recognizer — English receipt ও latin সংখ্যা
        val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            parseReceiptResult(latinRecognizer.process(image).await().text)
        } catch (_: Exception) {
            null
        } finally {
            latinRecognizer.close()
        }
    }

    private fun parseReceiptResult(rawText: String?): ReceiptScanResult? {
        if (rawText.isNullOrBlank()) return null
        val amount = extractAmount(rawText)
        val date = extractDate(rawText) ?: currentDateString()
        return if (amount != null) ReceiptScanResult(amount = amount, date = date, rawText = rawText) else null
    }

    /** OCR-এ বাংলা সংখ্যা (০-৯) এলে Latin-এ রূপান্তর */
    private fun convertBengaliDigits(text: String): String {
        val bn = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return buildString {
            text.forEach { ch ->
                val i = bn.indexOf(ch)
                append(if (i >= 0) ('0' + i) else ch)
            }
        }
    }

    private fun extractAmount(rawText: String): Double? {
        val normalizedText = convertBengaliDigits(rawText.replace("\r", ""))
        val keywordScore: (String) -> Int = { text ->
            val lower = text.lowercase(Locale.getDefault())
            when {
                lower.contains("total") || lower.contains("মোট") || lower.contains("সর্বমোট") -> 50
                lower.contains("amount") || lower.contains("পরিমাণ") -> 45
                lower.contains("payable") || lower.contains("paid") || lower.contains("পরিশোধ") -> 40
                lower.contains("net") || lower.contains("balance") || lower.contains("বাকি") -> 30
                lower.contains("subtotal") || lower.contains("উপমোট") -> 20
                lower.contains("৳") || lower.contains("tk") || lower.contains("taka") || lower.contains("টাকা") || lower.contains("bdt") -> 15
                else -> 0
            }
        }

        val matches = mutableListOf<Pair<Double, Int>>()
        val regex = Regex("(?:\\d{1,3}(?:[\\s,]\\d{3})+|\\d+)(?:\\.\\d{1,2})?")

        normalizedText.lines().forEach { line ->
            val lineMatches = regex.findAll(line)
            for (match in lineMatches) {
                val value = match.value.replace(",", "").trim().toDoubleOrNull() ?: continue
                if (value <= 0.0 || value > 1000000.0) continue
                val score = keywordScore(line) + if (value > 1.0) 10 else 0
                matches.add(value to score)
            }
        }

        if (matches.isEmpty()) {
            val allMatches = regex.findAll(normalizedText).map { it.value.replace(",", "").trim().toDoubleOrNull() }.filterNotNull().toList()
            if (allMatches.isNotEmpty()) return allMatches.maxOrNull()
            return null
        }

        return matches.maxByOrNull { it.second }?.first
    }

    private fun extractDate(rawText: String): String? {
        val patterns = listOf(
            Regex("\\b(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[0-2])/\\d{4}\\b"),
            Regex("\\b(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[0-2])-\\d{4}\\b"),
            Regex("\\b\\d{4}-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01])\\b"),
            Regex("\\b(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[0-2])/\\d{2}\\b"),
            Regex("\\b(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[0-2])-\\d{2}\\b")
        )

        for (pattern in patterns) {
            val match = pattern.find(rawText) ?: continue
            val candidate = match.value
            val normalized = normalizeDate(candidate)
            if (normalized != null) return normalized
        }

        return null
    }

    private fun normalizeDate(raw: String): String? {
        val clean = raw.trim()
        return try {
            val formats = listOf(
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd",
                "dd/MM/yy",
                "dd-MM-yy"
            )

            for (format in formats) {
                try {
                    val parsed = SimpleDateFormat(format, Locale.getDefault()).parse(clean)
                    if (parsed != null) return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed)
                } catch (_: Exception) {
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun currentDateString(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date())
}
