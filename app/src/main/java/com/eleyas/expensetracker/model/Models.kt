package com.eleyas.expensetracker.model

data class Transaction(
    val id: Long,
    val type: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val reason: String,
    val date: String,
    val receiptImage: String? = null,
    val walletId: String = "default_cash",
    val addedByUid: String? = null,
    val addedByName: String? = null
)

data class Wallet(
    val id: String,
    val name: String,
    val type: String,
    val initialBalance: Double = 0.0,
    val currency: String = "BDT",
    val color: Int = 0xFF4CAF50.toInt()
)

data class BackupData(
    val transactions: List<Transaction>,
    val usdToBdt: Double,
    val usdToMvr: Double,
    val loans: List<LoanAccount> = emptyList(),
    val loanPayments: List<LoanPayment> = emptyList(),
    val lendings: List<LendingAccount> = emptyList(),
    val lendingReturns: List<LendingReturn> = emptyList(),
    val wallets: List<Wallet> = emptyList()
)

data class LoanBorrowing(
    val id: Long,
    val loanId: Long,
    val amount: Double,
    val date: String,
    val note: String = ""
)

data class CategoryBudget(
    val month: String,
    val category: String,
    val limit: Double
)

data class LoanAccount(
    val id: Long,
    val name: String,
    val sourceType: String,
    val principal: Double,
    val monthlyInstallment: Double,
    val startDate: String,
    val note: String,
    val lastEditedDate: String = "",
    val editHistory: List<String> = emptyList(),
    val borrowings: List<LoanBorrowing> = emptyList(),
    val dueDate: String? = null
)

data class LoanPayment(
    val id: Long,
    val loanId: Long,
    val amount: Double,
    val date: String,
    val note: String,
    val fundSource: String = "personal"
)

data class LendingAccount(
    val id: Long,
    val person: String,
    val amount: Double,
    val date: String,
    val note: String,
    val dueDate: String? = null,
    val fundSource: String = "personal"
)

data class LendingReturn(
    val id: Long,
    val lendingId: Long,
    val amount: Double,
    val date: String,
    val note: String,
    val fundSource: String = "personal"
)

data class LoanInterestTerms(
    val loanId: Long,
    val interestRate: Double = 0.0,
    val totalInterest: Double = 0.0,
    val interestType: String = "fixed"
)
