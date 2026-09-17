package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.model.Wallet

/**
 * Personal cash only. Home-funded movements are excluded so the same
 * taka is never subtracted from both Personal and Home.
 */
object BalanceEngine {

    fun personalBalance(
        wallets: List<Wallet>,
        transactions: List<Transaction>,
        loans: List<LoanAccount>,
        loanPayments: List<LoanPayment>,
        lendings: List<LendingAccount>,
        lendingReturns: List<LendingReturn>,
        amountConverter: (Transaction) -> Double = { it.amount }
    ): Double {
        val income = transactions
            .filter { it.type.equals("income", ignoreCase = true) }
            .sumOf { amountConverter(it) }

        val expense = transactions
            .filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { amountConverter(it) }

        val homeTransfer = transactions
            .filter { it.type.equals("home", ignoreCase = true) }
            .sumOf { amountConverter(it) }

        val loanReceived = loans.sumOf { it.principal }

        val personalLoanPaid = loanPayments
            .filterNot { FundSource.isHomeLoanPayment(it) }
            .sumOf { it.amount }

        val lendingById = lendings.associateBy { it.id }
        val personalLendings = lendings.filterNot { FundSource.isHomeLending(it) }
        val personalReturns = lendingReturns.filter { ret ->
            !FundSource.isHomeLendingReturn(ret, lendingById[ret.lendingId])
        }

        val moneyLent = personalLendings.sumOf { it.amount }
        val moneyReturned = personalReturns.sumOf { it.amount }

        return wallets.sumOf { it.initialBalance } +
            income +
            loanReceived +
            moneyReturned -
            expense -
            homeTransfer -
            personalLoanPaid -
            moneyLent
    }
}
