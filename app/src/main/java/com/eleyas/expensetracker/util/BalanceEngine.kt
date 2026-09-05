package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.model.Wallet

/**
 * Source-aware balance calculations.
 *
 * Home-funded lending is identified by the existing [HOME] marker so that
 * old records remain untouched. Personal lending continues to affect the
 * personal balance; Home lending is excluded here because it is already
 * represented by the Home ledger.
 */
object BalanceEngine {

    fun personalBalance(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        loans: List<LoanAccount>,
        loanPayments: List<LoanPayment>,
        lendings: List<LendingAccount>,
        lendingReturns: List<LendingReturn>
    ): Double {
        val income = transactions
            .filter { it.type == "income" }
            .sumOf { convertToBdt(it.amount, it.currency) }

        val expense = transactions
            .filter { it.type == "expense" }
            .sumOf { convertToBdt(it.amount, it.currency) }

        val homeTransfer = transactions
            .filter { it.type == "home" }
            .sumOf { convertToBdt(it.amount, it.currency) }

        val loanReceived = loans.sumOf { it.principal }
        val loanPaid = loanPayments.sumOf { it.amount }

        // Only personal lending belongs in the personal balance.
        val personalLendings = lendings.filterNot { it.note.contains("[HOME]") }
        val personalReturns = lendingReturns.filter { ret ->
            personalLendings.any { it.id == ret.lendingId }
        }

        val moneyLent = personalLendings.sumOf { it.amount }
        val moneyReturned = personalReturns.sumOf { it.amount }

        return wallets.sumOf { it.initialBalance } +
                income +
                loanReceived +
                moneyReturned -
                expense -
                homeTransfer -
                loanPaid -
                moneyLent
    }

    /**
     * Converts a transaction amount to BDT without depending on MainViewModel.
     * BDT is unchanged; unknown currencies are kept as-is so historical data
     * is not silently changed.
     */
    private fun convertToBdt(amount: Double, currency: String): Double {
        return when (currency.trim().uppercase()) {
            "", "BDT", "৳", "TK", "TAKA" -> amount
            else -> amount
        }
    }
}
