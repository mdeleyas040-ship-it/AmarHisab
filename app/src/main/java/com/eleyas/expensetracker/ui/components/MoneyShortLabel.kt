package com.eleyas.expensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

@Composable
fun MoneyShortLabel(valueText: String, modifier: Modifier = Modifier) {
    val value = valueText.trim().replace(",", "")
        .map { c ->
            when (c) {
                '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                else -> c
            }
        }.joinToString("").toDoubleOrNull() ?: return

    if (value <= 0.0) return

    val absValue = abs(value)
    val (number, unit) = when {
        absValue >= 10_000_000 -> absValue / 10_000_000 to "কোটি"
        absValue >= 100_000 -> absValue / 100_000 to "লাখ"
        absValue >= 1_000 -> absValue / 1_000 to "হাজার"
        absValue >= 100 -> absValue / 100 to "শত"
        else -> absValue to ""
    }

    val formatted = if (number >= 100 || number % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", number)
    } else {
        String.format(Locale.US, "%.2f", number).trimEnd('0').trimEnd('.')
    }

    val bangla = formatted.map { c ->
        when (c) {
            '0' -> '০'; '1' -> '১'; '2' -> '২'; '3' -> '৩'; '4' -> '৪'
            '5' -> '৫'; '6' -> '৬'; '7' -> '৭'; '8' -> '৮'; '9' -> '৯'
            else -> c
        }
    }.joinToString("")

    Text(
        text = if (unit.isBlank()) bangla else "$bangla $unit",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier

    )
}
