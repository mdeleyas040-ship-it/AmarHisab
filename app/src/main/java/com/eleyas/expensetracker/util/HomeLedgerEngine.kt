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
 * Home fund ledger. Personal records are ignored unless they carry an
 * explicit Home marker, so family money is not mixed with personal cash.
 */
object HomeLedgerEngine {

    fun build(
        transactions: List<Transaction>,
        loans: List<LoanAccount>,
        loanPayments: List<LoanPayment>,
        lendings: List<LendingAccount>,
        lendingReturns: List<LendingReturn>,
        amountConverter: (Transaction) -> Double = { it.amount }
    ): List<HomeLedgerEntry> {
        val entries = mutableListOf<HomeLedgerEntry>()

        transactions.forEach { transaction ->
            when (transaction.type.lowercase()) {
                "home" -> entries += HomeLedgerEntry(
                    id = "tx_home_${transaction.id}",
                    date = transaction.date,
                    title = transaction.reason.ifBlank { "বাড়িতে পাঠানো" },
                    category = transaction.category.ifBlank { "বাড়িতে পাঠানো" },
                    amount = amountConverter(transaction),
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
                    amount = amountConverter(transaction),
                    direction = HomeLedgerDirection.OUT,
                    sourceType = HomeLedgerSourceType.HOME_EXPENSE,
                    sourceId = transaction.id.toString(),
                    note = transaction.reason
                )

                "home_adjustment" -> entries += HomeLedgerEntry(
                    id = "tx_home_adjustment_${transaction.id}",
                    date = transaction.date,
                    title = transaction.reason.ifBlank { "বাড়ির সমন্বয়" },
                    category = transaction.category.ifBlank { "Home Adjustment" },
                    amount = amountConverter(transaction),
                    direction = HomeLedgerDirection.IN,
                    sourceType = HomeLedgerSourceType.HOME_ADJUSTMENT,
                    sourceId = transaction.id.toString(),
                    note = transaction.reason
                )
            }
        }

        val loanNames = loans.associateBy { it.id }
        loanPayments.filter(FundSource::isHomeLoanPayment).forEach { payment ->
            val loan = loanNames[payment.loanId]
            entries += HomeLedgerEntry(
                id = "loan_payment_home_${payment.id}",
                date = payment.date,
                title = "ঋণ পরিশোধ${loan?.name?.let { " — $it" } ?: ""}",
                category = "ঋণ পরিশোধ",
                amount = payment.amount,
                direction = HomeLedgerDirection.OUT,
                sourceType = HomeLedgerSourceType.LOAN_REPAYMENT_RECEIVED,
                sourceId = payment.id.toString(),
                note = payment.note
            )
        }

        val lendingById = lendings.associateBy { it.id }
        lendings.filter(FundSource::isHomeLending).forEach { lending ->
            entries += HomeLedgerEntry(
                id = "lending_home_${lending.id}",
                date = lending.date,
                title = "ধার দেওয়া — ${lending.person}",
                category = "ধার দেওয়া",
                amount = lending.amount,
                direction = HomeLedgerDirection.OUT,
                sourceType = HomeLedgerSourceType.LENDING_GIVEN,
                sourceId = lending.id.toString(),
                note = lending.note
            )
        }

        lendingReturns.forEach { ret ->
            val lending = lendingById[ret.lendingId]
            if (!FundSource.isHomeLendingReturn(ret, lending)) return@forEach
            entries += HomeLedgerEntry(
                id = "lending_return_home_${ret.id}",
                date = ret.date,
                title = "ধার ফেরত${lending?.person?.let { " — $it" } ?: ""}",
                category = "ধার ফেরত",
                amount = ret.amount,
                direction = HomeLedgerDirection.IN,
                sourceType = HomeLedgerSourceType.LENDING_RETURN_RECEIVED,
                sourceId = ret.id.toString(),
                note = ret.note
            )
        }

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
