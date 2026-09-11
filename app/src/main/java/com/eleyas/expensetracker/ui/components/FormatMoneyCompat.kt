package com.eleyas.expensetracker.ui.components

/**
 * Compatibility bridge for component files that reference formatMoney without
 * importing the shared utility function.
 *
 * Number is intentionally used here so callers that also import the shared
 * Double overload resolve to the canonical utility function without ambiguity.
 */
fun formatMoney(amount: Number): String =
    com.eleyas.expensetracker.util.formatMoney(amount.toDouble())
