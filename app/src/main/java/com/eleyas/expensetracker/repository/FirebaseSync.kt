package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.*
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Amar Hisab - Firebase Cloud Sync
 *
 * Existing local data structure পরিবর্তন না করে
 * আলাদা file থেকে Firebase sync করার জন্য।
 */

fun syncLoansToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    loans: List<LoanAccount>
) {
    if (loans.isEmpty()) return

    val batch = firestore.batch()

    val collection = firestore
        .collection("users")
        .document(userId)
        .collection("loans")

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

        val document = collection
            .document(loan.id.toString())

        batch.set(document, loanData)
    }

    batch.commit()
}


fun syncLoanPaymentsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    payments: List<LoanPayment>
) {
    if (payments.isEmpty()) return

    val batch = firestore.batch()

    val collection = firestore
        .collection("users")
        .document(userId)
        .collection("loanPayments")

    payments.forEach { payment ->

        val paymentData = mapOf(
            "id" to payment.id,
            "loanId" to payment.loanId,
            "amount" to payment.amount,
            "date" to payment.date,
            "note" to payment.note
        )

        val document = collection
            .document(payment.id.toString())

        batch.set(document, paymentData)
    }

    batch.commit()
}


fun syncLendingsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    lendings: List<LendingAccount>
) {
    if (lendings.isEmpty()) return

    val batch = firestore.batch()

    val collection = firestore
        .collection("users")
        .document(userId)
        .collection("lendings")

    lendings.forEach { lending ->

        val lendingData = mapOf(
            "id" to lending.id,
            "person" to lending.person,
            "amount" to lending.amount,
            "date" to lending.date,
            "note" to lending.note
        )

        val document = collection
            .document(lending.id.toString())

        batch.set(document, lendingData)
    }

    batch.commit()
}


fun syncLendingReturnsToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    returns: List<LendingReturn>
) {
    if (returns.isEmpty()) return

    val batch = firestore.batch()

    val collection = firestore
        .collection("users")
        .document(userId)
        .collection("lendingReturns")

    returns.forEach { item ->

        val returnData = mapOf(
            "id" to item.id,
            "lendingId" to item.lendingId,
            "amount" to item.amount,
            "date" to item.date,
            "note" to item.note
        )

        val document = collection
            .document(item.id.toString())

        batch.set(document, returnData)
    }

    batch.commit()
}


/**
 * একবারে সব Loan/Lending data Cloud-এ পাঠানোর জন্য।
 */
fun syncAllLoanAndLendingData(
    firestore: FirebaseFirestore,
    userId: String,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>
) {
    syncLoansToFirestore(
        firestore = firestore,
        userId = userId,
        loans = loans
    )

    syncLoanPaymentsToFirestore(
        firestore = firestore,
        userId = userId,
        payments = loanPayments
    )

    syncLendingsToFirestore(
        firestore = firestore,
        userId = userId,
        lendings = lendings
    )

    syncLendingReturnsToFirestore(
        firestore = firestore,
        userId = userId,
        returns = lendingReturns
    )
}