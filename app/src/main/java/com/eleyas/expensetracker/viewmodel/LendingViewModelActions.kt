package com.eleyas.expensetracker

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.repository.syncAllLoanAndLendingData
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.reflect.Field

/**
 * Compatibility actions for the existing Lending UI.
 * The stable ViewModel already owns these state holders privately, so this bridge
 * updates their Compose MutableState delegates without changing the existing model.
 */
private fun <T> MainViewModel.setPrivateState(propertyName: String, value: T) {
    val delegateField: Field = MainViewModel::class.java.getDeclaredField("${propertyName}\$delegate").apply {
        isAccessible = true
    }
    @Suppress("UNCHECKED_CAST")
    val state = delegateField.get(this) as MutableState<T>
    state.value = value
}

fun MainViewModel.updateLending(
    context: Context,
    lending: LendingAccount,
    person: String,
    amount: Double,
    date: String,
    note: String,
    dueDate: String? = null
) {
    if (person.trim().isBlank() || amount <= 0.0) {
        Toast.makeText(context, "সঠিক নাম ও টাকার পরিমাণ দিন।", Toast.LENGTH_SHORT).show()
        return
    }

    val updated = lending.copy(
        person = person.trim(),
        amount = amount,
        date = date,
        note = note.trim(),
        dueDate = dueDate
    )
    val updatedLendings = lendings.map { if (it.id == lending.id) updated else it }
    setPrivateState("lendings", updatedLendings)
    saveAutoBackup(context)

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (!userId.isNullOrBlank()) {
        syncAllLoanAndLendingData(
            FirebaseFirestore.getInstance(),
            userId,
            loans,
            loanPayments,
            updatedLendings,
            lendingReturns
        )
    }
    Toast.makeText(context, "✅ ধারের তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
}

fun MainViewModel.deleteLending(context: Context, lending: LendingAccount) {
    val updatedLendings = lendings.filterNot { it.id == lending.id }
    val updatedReturns = lendingReturns.filterNot { it.lendingId == lending.id }

    setPrivateState("lendings", updatedLendings)
    setPrivateState("lendingReturns", updatedReturns)
    saveAutoBackup(context)

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (!userId.isNullOrBlank()) {
        syncAllLoanAndLendingData(
            FirebaseFirestore.getInstance(),
            userId,
            loans,
            loanPayments,
            updatedLendings,
            updatedReturns
        )
    }

    Toast.makeText(
        context,
        "🗑️ ধার ৳${formatMoney(lending.amount)} এবং এর ফেরত history মুছে ফেলা হয়েছে",
        Toast.LENGTH_SHORT
    ).show()
}
