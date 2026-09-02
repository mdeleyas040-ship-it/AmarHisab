package com.eleyas.expensetracker

import android.content.Context
import android.widget.Toast
import com.eleyas.expensetracker.model.LendingAccount
import com.eleyas.expensetracker.viewmodel.MainViewModel

/** Compatibility extensions kept in the app root package so MainActivity can resolve them without extra imports. */
fun MainViewModel.updateLending(
    context: Context,
    lending: LendingAccount,
    person: String,
    amount: Double,
    date: String,
    note: String,
    dueDate: String? = null
) {
    updateCloudData(
        transactions,
        loans,
        loanPayments,
        lendings.map { if (it.id == lending.id) lending.copy(person = person.trim(), amount = amount, date = date, note = note.trim(), dueDate = dueDate) else it },
        lendingReturns,
        wallets
    )
    Toast.makeText(context, "✅ ধারের তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
}

fun MainViewModel.deleteLending(
    context: Context,
    lending: LendingAccount
) {
    updateCloudData(
        transactions,
        loans,
        loanPayments,
        lendings.filterNot { it.id == lending.id },
        lendingReturns.filterNot { it.lendingId == lending.id },
        wallets
    )
    Toast.makeText(context, "🗑️ ধারের তথ্য মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
}
