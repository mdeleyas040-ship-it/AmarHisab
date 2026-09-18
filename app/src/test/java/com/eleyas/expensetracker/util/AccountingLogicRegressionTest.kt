package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.model.LendingReturn
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountingLogicRegressionTest {

    private fun tx(
        id: Long,
        type: String,
        amount: Double
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        currency = "BDT",
        category = "",
        reason = "",
        date = "2026-09-18"
    )

    private fun loan(
        id: Long,
        principal: Double
    ) = LoanAccount(
        id = id,
        name = "Test Loan",
        sourceType = "person",
        principal = principal,
        monthlyInstallment = principal,
        startDate = "2026-09-18",
        note = ""
    )

    private fun payment(
        id: Long,
        loanId: Long,
        amount: Double,
        fundSource: String
    ) = LoanPayment(
        id = id,
        loanId = loanId,
        amount = amount,
        date = "2026-09-18",
        note = "",
        fundSource = fundSource
    )

    private fun lending(
        id: Long,
        amount: Double,
        fundSource: String
    ) = LendingAccount(
        id = id,
        person = "Test Person",
        amount = amount,
        date = "2026-09-18",
        note = "",
        fundSource = fundSource
    )

    private fun lendingReturn(
        id: Long,
        lendingId: Long,
        amount: Double,
        fundSource: String
    ) = LendingReturn(
        id = id,
        lendingId = lendingId,
        amount = amount,
        date = "2026-09-18",
        note = "",
        fundSource = fundSource
    )

    private fun personalBalance(
        initial: Double = 0.0,
        transactions: List<Transaction> = emptyList(),
        loans: List<LoanAccount> = emptyList(),
        payments: List<LoanPayment> = emptyList(),
        lendings: List<LendingAccount> = emptyList(),
        returns: List<LendingReturn> = emptyList()
    ): Double = BalanceEngine.personalBalance(
        wallets = listOf(
            Wallet(
                id = "default_cash",
                name = "Cash",
                type = "cash",
                initialBalance = initial
            )
        ),
        transactions = transactions,
        loans = loans,
        loanPayments = payments,
        lendings = lendings,
        lendingReturns = returns
    )

    @Test
    fun loanProceedsEnterPersonalOnly() {
        assertEquals(
            10_000.0,
            personalBalance(loans = listOf(loan(1, 10_000.0))),
            0.001
        )
    }

    @Test
    fun personalToHomeTransferReducesPersonalOnly() {
        assertEquals(
            0.0,
            personalBalance(
                initial = 10_000.0,
                transactions = listOf(tx(1, "home", 10_000.0))
            ),
            0.001
        )
    }

    @Test
    fun homeExpenseDoesNotReducePersonal() {
        assertEquals(
            10_000.0,
            personalBalance(
                initial = 10_000.0,
                transactions = listOf(tx(1, "home_expense", 3_000.0))
            ),
            0.001
        )
    }

    @Test
    fun homeLoanPaymentDoesNotReducePersonal() {
        assertEquals(
            10_000.0,
            personalBalance(
                initial = 10_000.0,
                payments = listOf(payment(1, 1, 3_000.0, "home"))
            ),
            0.001
        )
    }

    @Test
    fun personalLoanPaymentReducesPersonal() {
        assertEquals(
            7_000.0,
            personalBalance(
                initial = 10_000.0,
                payments = listOf(payment(1, 1, 3_000.0, "personal"))
            ),
            0.001
        )
    }

    @Test
    fun homeLendingDoesNotReducePersonal() {
        assertEquals(
            10_000.0,
            personalBalance(
                initial = 10_000.0,
                lendings = listOf(lending(1, 3_000.0, "home"))
            ),
            0.001
        )
    }

    @Test
    fun personalLendingReducesPersonal() {
        assertEquals(
            7_000.0,
            personalBalance(
                initial = 10_000.0,
                lendings = listOf(lending(1, 3_000.0, "personal"))
            ),
            0.001
        )
    }

    @Test
    fun homeLendingReturnDoesNotIncreasePersonal() {
        val homeLending = lending(1, 3_000.0, "home")
        assertEquals(
            10_000.0,
            personalBalance(
                initial = 10_000.0,
                lendings = listOf(homeLending),
                returns = listOf(lendingReturn(1, 1, 3_000.0, "home"))
            ),
            0.001
        )
    }

    @Test
    fun personalLendingReturnIncreasesPersonal() {
        val personalLending = lending(1, 3_000.0, "personal")
        assertEquals(
            13_000.0,
            personalBalance(
                initial = 10_000.0,
                lendings = listOf(personalLending),
                returns = listOf(lendingReturn(1, 1, 3_000.0, "personal"))
            ),
            0.001
        )
    }

    @Test
    fun mixedHomeAndPersonalMovementsDoNotDoubleCount() {
        val result = personalBalance(
            initial = 10_000.0,
            transactions = listOf(
                tx(1, "home", 4_000.0),
                tx(2, "home_expense", 1_000.0),
                tx(3, "expense", 500.0)
            ),
            payments = listOf(
                payment(1, 1, 1_500.0, "home"),
                payment(2, 1, 1_000.0, "personal")
            ),
            lendings = listOf(
                lending(1, 1_000.0, "home"),
                lending(2, 500.0, "personal")
            ),
            returns = listOf(
                lendingReturn(1, 1, 1_000.0, "home"),
                lendingReturn(2, 2, 200.0, "personal")
            )
        )

        // Personal: 10,000 - 4,000 - 500 - 1,000 - 500 + 200 = 4,200.
        assertEquals(4_200.0, result, 0.001)
    }

    @Test
    fun homeLedgerContainsHomeMovementsButNotPersonalOnes() {
        val entries = HomeLedgerEngine.build(
            transactions = listOf(
                tx(1, "home", 4_000.0),
                tx(2, "home_expense", 1_000.0),
                tx(3, "expense", 500.0)
            ),
            loans = emptyList(),
            loanPayments = listOf(
                payment(1, 1, 1_500.0, "home"),
                payment(2, 1, 700.0, "personal")
            ),
            lendings = listOf(
                lending(1, 1_000.0, "home"),
                lending(2, 500.0, "personal")
            ),
            lendingReturns = listOf(
                lendingReturn(1, 1, 1_000.0, "home"),
                lendingReturn(2, 2, 500.0, "personal")
            )
        )

        assertEquals(5, entries.size)
        assertEquals(4_000.0 + 1_000.0, entries.filter { it.direction.name == "IN" }.sumOf { it.amount })
        assertEquals(1_000.0 + 1_500.0 + 1_000.0, entries.filter { it.direction.name == "OUT" }.sumOf { it.amount })
        assertEquals(
            setOf(
                "tx_home_1",
                "tx_home_expense_2",
                "loan_payment_home_1",
                "lending_home_1",
                "lending_return_home_1"
            ),
            entries.map { it.id }.toSet()
        )
    }
}
