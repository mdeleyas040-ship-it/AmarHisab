package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.HomeLedgerDirection
import com.eleyas.expensetracker.model.HomeLedgerEntry
import com.eleyas.expensetracker.model.HomeLedgerSourceType
import com.eleyas.expensetracker.model.HomeLedgerSummary
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.model.Transaction

/**
 * Builds a Home ledger from existing records.
 *
 * No existing record is copied or rewritten. This prevents migration from
 * creating duplicate money movements. Ambiguous historical loan/lending
 * records are deliberately excluded until they are explicitly linked to Home.
 */
object HomeLedgerEngine {

    fun build(
        transactions: List<Transaction>,
        loans: List<LoanAccount>,
        loanPayments: List<LoanPayment>,
        lendings: List<LendingAccount>,
        lendingReturns: List<LendingReturn>
    ): List<HomeLedgerEntry> {
        val entries = mutableListOf<HomeLedgerEntry>()

        transactions.forEach { transaction ->
            when (transaction.type.lowercase()) {
                "home" -> entries += HomeLedgerEntry(
                    id = "tx_home_${transaction.id}",
                    date = transaction.date,
                    title = transaction.reason.ifBlank { "বাড়িতে পাঠানো" },
                    category = transaction.category.ifBlank { "বাড়িতে পাঠানো" },
                    amount = transaction.amount,
                    direction = HomeLedgerDirection.IN,
                    sourceType = HomeLedgerSourceType.HOME_TRANSFER,
                    sourceId = transaction.id.toString(),
                    note = transaction.reason
                )

                "home_expense" -> entries += HomeLedgerEntry(
                    id = "tx_home_expense_${transaction.id}",
                    date = transaction.date,
                    title = transaction.reason.ifBlank { transaction.category },
                    category = transaction.category.ifBlank { "বাড়ির খরচ" },
                    amount = transaction.amount,
                    direction = HomeLedgerDirection.OUT,
                    sourceType = HomeLedgerSourceType.HOME_EXPENSE,
                    sourceId = transaction.id.toString(),
                    note = transaction.reason
                )
            }
        }

        // Existing loan/lending records are not guessed as Home transactions.
        // They can be linked explicitly in the next integration step.
        @Suppress("UNUSED_VARIABLE")
        val existingLoans = loans
        @Suppress("UNUSED_VARIABLE")
        val existingLoanPayments = loanPayments
        @Suppress("UNUSED_VARIABLE")
        val existingLendings = lendings
        @Suppress("UNUSED_VARIABLE")
        val existingLendingReturns = lendingReturns

        return entries.sortedByDescending { it.date }
    }

    fun summarize(entries: List<HomeLedgerEntry>): HomeLedgerSummary {
        val totalIn = entries
            .filter { it.direction == HomeLedgerDirection.IN }
            .sumOf { it.amount }

        val totalOut = entries
            .filter { it.direction == HomeLedgerDirection.OUT }
            .sumOf { it.amount }

        return HomeLedgerSummary(
            totalIn = totalIn,
            totalOut = totalOut,
            balance = totalIn - totalOut
        )
    }
}
