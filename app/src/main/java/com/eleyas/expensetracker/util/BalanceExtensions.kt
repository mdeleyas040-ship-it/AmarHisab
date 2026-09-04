package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.viewmodel.MainViewModel

/**
 * Source-aware personal balance without modifying MainViewModel.
 * Keeps the existing ViewModel structure and legacy data intact.
 */
val MainViewModel.sourceAwareBalance: Double
    get() = BalanceEngine.personalBalance(
        wallets = wallets,
        transactions = transactions,
        loans = loans,
        loanPayments = loanPayments,
        lendings = lendings,
        lendingReturns = lendingReturns
    )
