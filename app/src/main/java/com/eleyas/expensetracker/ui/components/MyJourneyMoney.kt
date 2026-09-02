package com.eleyas.expensetracker.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Local money formatter for the My Journey card. */
private val MyJourneyMoneyFormatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

fun formatMoney(amount: Double): String =
    MyJourneyMoneyFormatter.format(kotlin.math.round(amount))
