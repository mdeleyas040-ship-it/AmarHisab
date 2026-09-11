package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.*
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Amar Hisab - Firebase Cloud Sync
 *
 * Local data structure unchanged রেখে
 * Loan / Lending data Firebase Cloud-এ sync করার জন্য।
 *
 * Backward compatible:
 * - পুরোনো documents-এ নতুন field না থাকলেও listener-side
 *   default value ব্যবহার করা যাবে।
 * - নতুন data-তে complete accounting information যাবে।
 */

/* =========================================================
   LOANS
   ========================================================= */

fun syncLoansToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    loans: List<LoanAccount>
) {
    if (loans.isEmpty()) return

    val batch = firestore.batch()
    val collection = firestore.collection("users").document(userId).collection("loans")

    loans.forEach { loan ->
        val loanData = mapOf(
            "id" to loan.id,
            "name" to loan.name,
            "sourceType" to loan.sourceType,
            "principal" to loan.principal,
            "monthlyInstallment" to loan.monthlyInstallment,
            "startDate" to loan.startDate,
            "note" to loan.note,
            "lastEditedDate" to loan.lastEditedDate,
            "editHistory" to loan.editHistory,
            "dueDate" to (loan.dueDate ?: ""),
            "borrowings" to loan.borrowings.map { borrowing ->
                mapOf(
                    "id" to borrowing.id,
                    "loanId" to borrowing.loanId,
                    "amount" to borrowing.amount,
                    "date" to borrowing.date,
                    "note" to borrowing.note
                )
            }
        )
        batch.set(collection.document(loan.id.toString()), loanData)
    }

    batch.commit()
}

/* =========================================================
   LOAN PAYMENTS
   ========================================================= */

fun syncLoanPaymentsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    payments: List<LoanPayment>
) {
    if (payments.isEmpty()) return

    val batch = firestore.batch()
    val collection = firestore.collection("users").document(userId).collection("loanPayments")

    payments.forEach { payment ->
        val paymentData = mapOf(
            "id" to payment.id,
            "loanId" to payment.loanId,
            "amount" to payment.amount,
            "date" to payment.date,
            "note" to payment.note,
            "fundSource" to payment.fundSource,
            "sourceTransactionId" to (payment.sourceTransactionId ?: 0L)
        )
        batch.set(collection.document(payment.id.toString()), paymentData)
    }

    batch.commit()
}

/* =========================================================
   LENDINGS
   ========================================================= */

fun syncLendingsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    lendings: List<LendingAccount>
) {
    if (lendings.isEmpty()) return

    val batch = firestore.batch()
    val collection = firestore.collection("users").document(userId).collection("lendings")

    lendings.forEach { lending ->
        val lendingData = mapOf(
            "id" to lending.id,
            "person" to lending.person,
            "amount" to lending.amount,
            "date" to lending.date,
            "note" to lending.note,
            "dueDate" to (lending.dueDate ?: ""),
            "fundSource" to lending.fundSource,
            // Accounting identity: the wallet and currency from which the lending was made.
            "walletId" to lending.walletId,
            "currency" to lending.currency
        )
        batch.set(collection.document(lending.id.toString()), lendingData)
    }

    batch.commit()
}

/* =========================================================
   LENDING RETURNS
   ========================================================= */

fun syncLendingReturnsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    returns: List<LendingReturn>
) {
    if (returns.isEmpty()) return

    val batch = firestore.batch()
    val collection = firestore.collection("users").document(userId).collection("lendingReturns")

    returns.forEach { item ->
        val returnData = mapOf(
            "id" to item.id,
            "lendingId" to item.lendingId,
            "amount" to item.amount,
            "date" to item.date,
            "note" to item.note,
            "fundSource" to item.fundSource,
            // Accounting identity: the wallet/currency receiving the returned money.
            "walletId" to item.walletId,
            "currency" to item.currency
        )
        batch.set(collection.document(item.id.toString()), returnData)
    }

    batch.commit()
}

/* =========================================================
   LOAN INTEREST TERMS
   ========================================================= */

fun syncLoanInterestTermsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    terms: List<LoanInterestTerms>
) {
    if (terms.isEmpty()) return

    val batch = firestore.batch()
    val collection = firestore.collection("users").document(userId).collection("loanInterestTerms")

    terms.forEach { term ->
        val termData = mapOf(
            "loanId" to term.loanId,
            "interestRate" to term.interestRate,
            "totalInterest" to term.totalInterest,
            "interestType" to term.interestType
        )
        batch.set(collection.document(term.loanId.toString()), termData)
    }

    batch.commit()
}

/* =========================================================
   ALL LOAN / LENDING DATA
   ========================================================= */

fun syncAllLoanAndLendingData(
    firestore: FirebaseFirestore,
    userId: String,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    loanInterestTerms: List<LoanInterestTerms> = emptyList()
) {
    syncLoansToFirestore(firestore, userId, loans)
    syncLoanPaymentsToFirestore(firestore, userId, loanPayments)
    syncLoanInterestTermsToFirestore(firestore, userId, loanInterestTerms)
    syncLendingsToFirestore(firestore, userId, lendings)
    syncLendingReturnsToFirestore(firestore, userId, lendingReturns)
}
