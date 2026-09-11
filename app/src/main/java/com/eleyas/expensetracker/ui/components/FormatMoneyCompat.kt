package com.eleyas.expensetracker.ui.components

/**
 * Compatibility bridge for component files that reference formatMoney without
 * importing the shared utility function.
 */
fun formatMoney(amount: Double): String =
    com.eleyas.expensetracker.util.formatMoney(amount)
