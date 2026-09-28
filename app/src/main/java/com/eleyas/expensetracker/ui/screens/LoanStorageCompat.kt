package com.eleyas.expensetracker.ui.screens

import android.content.Context
import android.content.SharedPreferences
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanInterestTerms
import com.eleyas.expensetracker.model.LoanPayment

/** Compatibility bridge for legacy unqualified storage calls in LoansScreen. */
object AccountStorage {
    fun getPrefs(context: Context, userId: String): SharedPreferences =
        com.eleyas.expensetracker.util.AccountStorage.getPrefs(context, userId)
}

fun saveLoans(prefs: SharedPreferences, loans: List<LoanAccount>) =
    com.eleyas.expensetracker.util.saveLoans(prefs, loans)

fun saveLoanPayments(prefs: SharedPreferences, payments: List<LoanPayment>) =
    com.eleyas.expensetracker.util.saveLoanPayments(prefs, payments)

fun saveLoanInterestTerms(prefs: SharedPreferences, terms: List<LoanInterestTerms>) =
    com.eleyas.expensetracker.util.saveLoanInterestTerms(prefs, terms)
