package com.eleyas.expensetracker.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Small in-app navigation bridge used when a loan is opened from SearchOverlay.
 * The Loans screen consumes the requested id and expands that loan.
 */
object LoanNavigationState {
    var requestedLoanId by mutableStateOf<Long?>(null)
        private set

    fun openLoan(loanId: Long) {
        requestedLoanId = loanId
    }

    fun clear() {
        requestedLoanId = null
    }
}
