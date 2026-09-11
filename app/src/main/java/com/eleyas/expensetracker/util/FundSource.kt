package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanPayment
import java.util.Locale

/**
 * Personal vs Home fund detection.
 *
 * New records use [fundSource]. Older records are still recognized from
 * existing notes so saved data does not need to be rewritten.
 */
object FundSource {

    fun isHomeLoanPayment(payment: LoanPayment): Boolean {
        return when (payment.fundSource.lowercase(Locale.getDefault())) {
            "home" -> true
            "personal" -> false
            else -> {
                val note = payment.note.lowercase(Locale.getDefault())
                note.contains("বাড়িতে পাঠানো") ||
                    note.contains("বাড়িতে পাঠানো") ||
                    note.contains("home loan payment") ||
                    note.contains("home-funded loan")
            }
        }
    }

    fun isHomeLending(lending: LendingAccount): Boolean =
        lending.fundSource.equals("home", ignoreCase = true) ||
            lending.note.contains("[HOME]", ignoreCase = true)

    fun isHomeLendingReturn(
        ret: LendingReturn,
        lending: LendingAccount?
    ): Boolean {
        if (ret.fundSource.equals("home", ignoreCase = true)) return true
        if (ret.note.contains("[HOME_RETURN]", ignoreCase = true)) return true
        return lending != null && isHomeLending(lending)
    }
}
