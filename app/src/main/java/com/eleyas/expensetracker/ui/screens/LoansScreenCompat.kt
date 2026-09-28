package com.eleyas.expensetracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eleyas.expensetracker.model.*

/** Compatibility overload for existing MainActivity positional call sites. */
@Composable
fun LoansScreen(
    modifier: Modifier,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    people: List<PersonProfile> = emptyList(),
    lendingReturns: List<LendingReturn>,
    onAddLoan: () -> Unit,
    onAddLoanPayment: (LoanAccount) -> Unit,
    onEditLoan: (LoanAccount) -> Unit,
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onEditLoanPayment: (LoanPayment) -> Unit = {},
    onDeleteLoanPayment: (LoanPayment) -> Unit = {},
    onAddLending: () -> Unit,
    onAddLendingReturn: (LendingAccount) -> Unit,
    loanInterestTerms: List<LoanInterestTerms>,
    onEditLending: (LendingAccount) -> Unit = {},
    onDeleteLending: (LendingAccount) -> Unit = {},
    onShowCalculator: () -> Unit = {},
    onShareLoan: (LoanAccount, Boolean) -> Unit = { _, _ -> },
    onShareLending: (LendingAccount, Boolean) -> Unit = { _, _ -> },
    searchQuery: String = ""
) {
    LoansScreen(
        modifier = modifier,
        loans = loans,
        loanPayments = loanPayments,
        lendings = lendings,
        people = people,
        lendingReturns = lendingReturns,
        onAddLoan = onAddLoan,
        onAddLoanPayment = onAddLoanPayment,
        onEditLoan = onEditLoan,
        onDeleteLoan = {},
        onEditBorrowing = onEditBorrowing,
        onDeleteBorrowing = onDeleteBorrowing,
        onEditLoanPayment = onEditLoanPayment,
        onDeleteLoanPayment = onDeleteLoanPayment,
        onAddLending = onAddLending,
        onAddLendingReturn = onAddLendingReturn,
        onEditLending = onEditLending,
        onDeleteLending = onDeleteLending,
        loanInterestTerms = loanInterestTerms,
        onShowCalculator = onShowCalculator,
        onShareLoan = onShareLoan,
        onShareLending = onShareLending,
        searchQuery = searchQuery
    )
}
