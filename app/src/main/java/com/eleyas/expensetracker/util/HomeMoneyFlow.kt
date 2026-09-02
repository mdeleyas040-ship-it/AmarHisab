package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.HomeLedgerEntry
import com.eleyas.expensetracker.model.HomeLedgerSummary
import com.eleyas.expensetracker.viewmodel.MainViewModel

/**
 * Single read-only bridge for the Home money flow.
 *
 * This keeps Home calculations outside MainActivity/MainViewModel and does
 * not rewrite or migrate any existing saved data.
 */
object HomeMoneyFlow {

    fun entries(viewModel: MainViewModel): List<HomeLedgerEntry> =
        HomeLedgerEngine.build(
            transactions = viewModel.transactions,
            loans = viewModel.loans,
            loanPayments = viewModel.loanPayments,
            lendings = viewModel.lendings,
            lendingReturns = viewModel.lendingReturns
        )

    fun summary(viewModel: MainViewModel): HomeLedgerSummary =
        HomeLedgerEngine.summarize(entries(viewModel))

    fun balance(viewModel: MainViewModel): Double =
        summary(viewModel).balance

    /** Bengali labels for the Home money-flow source shown in the UI. */
    fun sourceLabel(entry: HomeLedgerEntry): String = when (entry.sourceType) {
        com.eleyas.expensetracker.model.HomeLedgerSourceType.HOME_TRANSFER -> "বাড়িতে পাঠানো"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.HOME_EXPENSE -> "বাড়ির খরচ"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.LOAN_GIVEN -> "ঋণ দেওয়া"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.LENDING_GIVEN -> "ধার দেওয়া"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.LOAN_REPAYMENT_RECEIVED -> "ঋণ পরিশোধ"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.LENDING_RETURN_RECEIVED -> "ধার ফেরত"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.OTHER_IN -> "টাকা এসেছে"
        com.eleyas.expensetracker.model.HomeLedgerSourceType.OTHER_OUT -> "টাকা গেছে"
    }
}
