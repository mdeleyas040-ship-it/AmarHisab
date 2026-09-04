package com.eleyas.expensetracker.model

/**
 * A read-only ledger view for money that belongs to the Home fund.
 *
 * IMPORTANT:
 * This model is intentionally NOT a replacement for existing Transaction,
 * LoanAccount, LoanPayment, LendingAccount or LendingReturn records.
 * It is a linked view so existing data remains untouched.
 */
data class HomeLedgerEntry(
    val id: String,
    val date: String,
    val title: String,
    val category: String,
    val amount: Double,
    val direction: HomeLedgerDirection,
    val sourceType: HomeLedgerSourceType,
    val sourceId: String? = null,
    val note: String = ""
)

enum class HomeLedgerDirection {
    IN,
    OUT
}

enum class HomeLedgerSourceType {
    HOME_TRANSFER,
    HOME_EXPENSE,
    LOAN_GIVEN,
    LENDING_GIVEN,
    LOAN_REPAYMENT_RECEIVED,
    LENDING_RETURN_RECEIVED,
    OTHER_IN,
    OTHER_OUT
}

data class HomeLedgerSummary(
    val totalIn: Double,
    val totalOut: Double,
    val balance: Double
)
