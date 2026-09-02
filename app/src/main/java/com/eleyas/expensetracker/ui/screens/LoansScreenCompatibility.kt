package com.eleyas.expensetracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanBorrowing
import com.eleyas.expensetracker.model.LoanInterestTerms
import com.eleyas.expensetracker.model.LoanPayment

/**
 * Compatibility overload for the existing MainActivity call.
 * Keeps MainActivity unchanged while the Loans screen uses the current API.
 */
@Composable
fun LoansScreen(
    modifier: Modifier,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    onAddLoan: () -> Unit,
    onAddLoanPayment: (LoanAccount) -> Unit,
    onEditLoan: (LoanAccount) -> Unit,
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onAddLending: () -> Unit,
    onAddLendingReturn: (LendingAccount) -> Unit,
    loanInterestTerms: List<LoanInterestTerms>,
    onEditLending: (LendingAccount) -> Unit,
    onDeleteLending: (LendingAccount) -> Unit,
    onShowCalculator: () -> Unit = {},
    onShareLoan: (LoanAccount, Boolean) -> Unit = { _, _ -> },
    onShareLending: (LendingAccount, Boolean) -> Unit = { _, _ -> },
    searchQuery: String = ""
) {
    // The callbacks are kept here for source compatibility with MainActivity.
    // Lending edit/delete UI remains owned by the existing LoansScreen implementation.
    LoansScreen(
        modifier = modifier,
        loans = loans,
        loanPayments = loanPayments,
        lendings = lendings,
        lendingReturns = lendingReturns,
        onAddLoan = onAddLoan,
        onAddLoanPayment = onAddLoanPayment,
        onEditLoan = onEditLoan,
        onEditBorrowing = onEditBorrowing,
        onDeleteBorrowing = onDeleteBorrowing,
        onAddLending = onAddLending,
        onAddLendingReturn = onAddLendingReturn,
        loanInterestTerms = loanInterestTerms,
        onShowCalculator = onShowCalculator,
        onShareLoan = onShareLoan,
        onShareLending = onShareLending,
        searchQuery = searchQuery
    )
}
