package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.Transaction

/**
 * Detects unusually large transactions relative to the user's own history.
 *
 * A transaction is flagged only when there is enough same-type/same-category
 * history to establish a meaningful baseline. The current transaction is
 * excluded from the baseline.
 */
object AnomalyDetector {
    private const val MIN_HISTORY = 5
    private const val MULTIPLIER = 3.0
    private const val MIN_ABSOLUTE_BDT = 1000.0

    fun isAnomalous(
        transaction: Transaction,
        transactions: List<Transaction>,
        usdToBdt: Double,
        usdToMvr: Double
    ): Boolean {
        val history = transactions
            .asSequence()
            .filter { it.id != transaction.id }
            .filter { it.type == transaction.type }
            .filter { it.category.equals(transaction.category, ignoreCase = true) }
            .map { toBdt(it, usdToBdt, usdToMvr) }
            .filter { it > 0.0 }
            .toList()

        if (history.size < MIN_HISTORY) return false

        val sorted = history.sorted()
        val median = if (sorted.size % 2 == 0) {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        } else {
            sorted[sorted.size / 2]
        }

        val currentBdt = toBdt(transaction, usdToBdt, usdToMvr)
        if (median <= 0.0 || currentBdt < MIN_ABSOLUTE_BDT) return false

        return currentBdt >= median * MULTIPLIER
    }

    fun toBdt(transaction: Transaction, usdToBdt: Double, usdToMvr: Double): Double =
        when (transaction.currency) {
            "BDT" -> transaction.amount
            "USD" -> transaction.amount * usdToBdt
            "MVR" -> if (usdToMvr > 0) transaction.amount * (usdToBdt / usdToMvr) else 0.0
            else -> 0.0
        }
}
