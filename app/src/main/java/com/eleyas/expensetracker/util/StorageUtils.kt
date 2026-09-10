package com.eleyas.expensetracker.util

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.eleyas.expensetracker.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem

fun saveCategoryBudgets(
    prefs: SharedPreferences,
    budgets: List<CategoryBudget>
) {
    val array = JSONArray()

    budgets.forEach {
        array.put(
            JSONObject().apply {
                put("month", it.month)
                put("category", it.category)
                put("limit", it.limit)
            }
        )
    }

    prefs.edit()
        .putString("category_budgets", array.toString())
        .apply()
}

fun saveSavingsGoals(prefs: SharedPreferences, goals: List<SavingsGoal>) {
    val array = JSONArray()
    goals.forEach { goal ->
        array.put(JSONObject().apply {
            put("id", goal.id)
            put("name", goal.name)
            put("targetAmount", goal.targetAmount)
            put("savedAmount", goal.savedAmount)
            put("targetDate", goal.targetDate)
            put("frequency", goal.frequency)
        })
    }
    prefs.edit().putString("savings_goals", array.toString()).apply()
}

fun loadSavingsGoals(prefs: SharedPreferences): List<SavingsGoal> {
    return try {
        val array = JSONArray(prefs.getString("savings_goals", "[]") ?: "[]")
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            SavingsGoal(
                id = item.getLong("id"),
                name = item.getString("name"),
                targetAmount = item.getDouble("targetAmount"),
                savedAmount = item.optDouble("savedAmount", 0.0),
                targetDate = item.getString("targetDate"),
                frequency = item.optString("frequency", "daily")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadCategoryBudgets(
    prefs: SharedPreferences
): List<CategoryBudget> {
    return try {
        val array = JSONArray(
            prefs.getString("category_budgets", "[]") ?: "[]"
        )

        List(array.length()) {
            val o = array.getJSONObject(it)

            CategoryBudget(
                o.getString("month"),
                o.getString("category"),
                o.getDouble("limit")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveTransactionToFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    transaction: Transaction,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    firestore
        .collection("users")
        .document(userId)
        .collection("transactions")
        .document(transaction.id.toString())
        .set(
            mapOf(
                "id" to transaction.id,
                "type" to transaction.type,
                "amount" to transaction.amount,
                "currency" to transaction.currency,
                "category" to transaction.category,
                "reason" to transaction.reason,
                "date" to transaction.date,
                "receiptImage" to (transaction.receiptImage ?: ""),
                "walletId" to transaction.walletId,
                "addedByUid" to (transaction.addedByUid ?: ""),
                "addedByName" to (transaction.addedByName ?: "")
            )
        )
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener {
            onError(it.message ?: "Error")
        }
}

fun deleteTransactionFromFirestore(
    firestore: FirebaseFirestore,
    userId: String,
    transactionId: Long,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    firestore
        .collection("users")
        .document(userId)
        .collection("transactions")
        .document(transactionId.toString())
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener {
            onError(it.message ?: "Error")
        }
}

fun firestoreDocumentToTransaction(
    doc: DocumentSnapshot
): Transaction? {
    return try {
        Transaction(
            id = doc.getLong("id") ?: 0L,
            type = doc.getString("type") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            currency = doc.getString("currency") ?: "BDT",
            category = doc.getString("category") ?: "Other",
            reason = doc.getString("reason") ?: "",
            date = doc.getString("date") ?: "",
            receiptImage = doc.getString("receiptImage")
                ?.ifBlank { null },
            walletId = doc.getString("walletId")
                ?: "default_cash",
            addedByUid = doc.getString("addedByUid")
                ?.ifBlank { null },
            addedByName = doc.getString("addedByName")
                ?.ifBlank { null }
        )
    } catch (_: Exception) {
        null
    }
}

suspend fun getLiveRates(): Pair<Double, Double>? =
    withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(
                URL("https://open.er-api.com/v6/latest/USD").readText()
            )

            if (
                json.getString("result") == "success"
            ) {
                Pair(
                    json.getJSONObject("rates").getDouble("BDT"),
                    json.getJSONObject("rates").getDouble("MVR")
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

fun saveTransactions(
    prefs: SharedPreferences,
    list: List<Transaction>
) {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("type", it.type)
                put("amount", it.amount)
                put("currency", it.currency)
                put("category", it.category)
                put("reason", it.reason)
                put("date", it.date)
                put("receiptImage", it.receiptImage ?: "")
                put("walletId", it.walletId)
                put("addedByUid", it.addedByUid ?: "")
                put("addedByName", it.addedByName ?: "")
            }
        )
    }

    prefs.edit()
        .putString("transactions", array.toString())
        .apply()
}

fun loadTransactions(
    prefs: SharedPreferences
): List<Transaction> {
    return try {
        val array = JSONArray(
            prefs.getString("transactions", null) ?: "[]"
        )

        List(array.length()) { i ->
            val o = array.getJSONObject(i)

            Transaction(
                id = o.getLong("id"),
                type = o.getString("type"),
                amount = o.getDouble("amount"),
                currency = o.getString("currency"),
                category = o.getString("category"),
                reason = o.getString("reason"),
                date = o.getString("date"),
                receiptImage = o.optString("receiptImage")
                    .takeIf { it.isNotBlank() },
                walletId = o.optString(
                    "walletId",
                    "default_cash"
                ),
                addedByUid = o.optString("addedByUid")
                    .takeIf { it.isNotBlank() },
                addedByName = o.optString("addedByName")
                    .takeIf { it.isNotBlank() }
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveLoans(
    prefs: SharedPreferences,
    list: List<LoanAccount>
) {
    val array = JSONArray()

    list.forEach { loan ->
        array.put(
            JSONObject().apply {
                put("id", loan.id)
                put("name", loan.name)
                put("sourceType", loan.sourceType)
                put("principal", loan.principal)
                put("monthlyInstallment", loan.monthlyInstallment)
                put("startDate", loan.startDate)
                put("note", loan.note)
                put("lastEditedDate", loan.lastEditedDate)
                put(
                    "editHistory",
                    JSONArray(loan.editHistory)
                )
                put(
                    "dueDate",
                    loan.dueDate ?: ""
                )

                val borrowingArray = JSONArray()

                loan.borrowings.forEach { borrowing ->
                    borrowingArray.put(
                        JSONObject().apply {
                            put("id", borrowing.id)
                            put("loanId", borrowing.loanId)
                            put("amount", borrowing.amount)
                            put("date", borrowing.date)
                            put("note", borrowing.note)
                        }
                    )
                }

                put(
                    "borrowings",
                    borrowingArray
                )
            }
        )
    }

    prefs.edit()
        .putString("loans", array.toString())
        .apply()
}

fun loadLoans(
    prefs: SharedPreferences
): List<LoanAccount> {
    return try {
        val array = JSONArray(
            prefs.getString("loans", null) ?: "[]"
        )

        List(array.length()) { i ->
            val o = array.getJSONObject(i)

            val historyArray =
                o.optJSONArray("editHistory")

            val history = mutableListOf<String>()

            if (historyArray != null) {
                for (j in 0 until historyArray.length()) {
                    history.add(
                        historyArray.getString(j)
                    )
                }
            }

            val borrowingArray =
                o.optJSONArray("borrowings")

            val borrowings =
                mutableListOf<LoanBorrowing>()

            if (borrowingArray != null) {
                for (j in 0 until borrowingArray.length()) {
                    val b =
                        borrowingArray.getJSONObject(j)

                    borrowings.add(
                        LoanBorrowing(
                            id = b.getLong("id"),
                            loanId = b.getLong("loanId"),
                            amount = b.getDouble("amount"),
                            date = b.getString("date"),
                            note = b.getString("note")
                        )
                    )
                }
            }

            LoanAccount(
                id = o.getLong("id"),
                name = o.getString("name"),
                sourceType = o.getString("sourceType"),
                principal = o.getDouble("principal"),
                monthlyInstallment =
                    o.getDouble("monthlyInstallment"),
                startDate = o.getString("startDate"),
                note = o.getString("note"),
                lastEditedDate =
                    o.optString(
                        "lastEditedDate",
                        ""
                    ),
                editHistory = history,
                borrowings = borrowings,
                dueDate =
                    o.optString("dueDate")
                        .takeIf { it.isNotBlank() }
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveLoanPayments(
    prefs: SharedPreferences,
    list: List<LoanPayment>
) {
    val array = JSONArray()

    list.forEach { payment ->
        array.put(
            JSONObject().apply {
                put("id", payment.id)
                put("loanId", payment.loanId)
                put("amount", payment.amount)
                put("date", payment.date)
                put("note", payment.note)

                // New accounting field.
                // Old records remain compatible.
                put(
                    "fundSource",
                    payment.fundSource
                )

                put(
                    "sourceTransactionId",
                    payment.sourceTransactionId ?: 0L
                )
            }
        )
    }

    prefs.edit()
        .putString("loanPayments", array.toString())
        .apply()
}

fun loadLoanPayments(
    prefs: SharedPreferences
): List<LoanPayment> {
    return try {
        val array = JSONArray(
            prefs.getString(
                "loanPayments",
                null
            ) ?: "[]"
        )

        List(array.length()) { i ->
            val o = array.getJSONObject(i)

            LoanPayment(
                id = o.getLong("id"),
                loanId = o.getLong("loanId"),
                amount = o.getDouble("amount"),
                date = o.getString("date"),
                note = o.getString("note"),

                // Missing in old data = personal.
                fundSource = o.optString(
                    "fundSource",
                    "personal"
                ),

                        sourceTransactionId =
                        o.optLong(
                        "sourceTransactionId",
                0L
            ).takeIf { it != 0L }
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveLendings(
    prefs: SharedPreferences,
    list: List<LendingAccount>
) {
    val array = JSONArray()

    list.forEach { lending ->
        array.put(
            JSONObject().apply {
                put("id", lending.id)
                put("person", lending.person)
                put("amount", lending.amount)
                put("date", lending.date)
                put("note", lending.note)
                put(
                    "dueDate",
                    lending.dueDate ?: ""
                )

                // New accounting field.
                put(
                    "fundSource",
                    lending.fundSource
                )
            }
        )
    }

    prefs.edit()
        .putString("lendings", array.toString())
        .apply()
}

fun loadLendings(
    prefs: SharedPreferences
): List<LendingAccount> {
    return try {
        val array = JSONArray(
            prefs.getString(
                "lendings",
                null
            ) ?: "[]"
        )

        List(array.length()) { i ->
            val o = array.getJSONObject(i)

            LendingAccount(
                id = o.getLong("id"),
                person = o.getString("person"),
                amount = o.getDouble("amount"),
                date = o.getString("date"),
                note = o.getString("note"),
                dueDate = o.optString("dueDate")
                    .takeIf { it.isNotBlank() },
                fundSource = o.optString(
                    "fundSource",
                    "personal"
                )
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveLendingReturns(
    prefs: SharedPreferences,
    list: List<LendingReturn>
) {
    val array = JSONArray()

    list.forEach { item ->
        array.put(
            JSONObject().apply {
                put("id", item.id)
                put("lendingId", item.lendingId)
                put("amount", item.amount)
                put("date", item.date)
                put("note", item.note)

                // New accounting field.
                put(
                    "fundSource",
                    item.fundSource
                )
            }
        )
    }

    prefs.edit()
        .putString(
            "lendingReturns",
            array.toString()
        )
        .apply()
}

fun loadLendingReturns(
    prefs: SharedPreferences
): List<LendingReturn> {
    return try {
        val array = JSONArray(
            prefs.getString(
                "lendingReturns",
                null
            ) ?: "[]"
        )

        List(array.length()) { i ->
            val o = array.getJSONObject(i)

            LendingReturn(
                id = o.getLong("id"),
                lendingId = o.getLong("lendingId"),
                amount = o.getDouble("amount"),
                date = o.getString("date"),
                note = o.getString("note"),
                fundSource = o.optString(
                    "fundSource",
                    "personal"
                )
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveWallets(
    prefs: SharedPreferences,
    list: List<Wallet>
) {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("type", it.type)
                put(
                    "initialBalance",
                    it.initialBalance
                )
                put("currency", it.currency)
                put("color", it.color)
            }
        )
    }

    prefs.edit()
        .putString("wallets", array.toString())
        .apply()
}

fun loadWallets(
    prefs: SharedPreferences
): List<Wallet> {
    return try {
        val raw =
            prefs.getString("wallets", null)

        if (raw == null) {
            listOf(
                Wallet(
                    "default_cash",
                    "ক্যাশ (নগদ)",
                    "Cash",
                    0.0,
                    "BDT",
                    0xFF4CAF50.toInt()
                )
            )
        } else {
            val array = JSONArray(raw)

            List(array.length()) { i ->
                val o =
                    array.getJSONObject(i)

                Wallet(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    type = o.getString("type"),
                    initialBalance =
                        o.optDouble(
                            "initialBalance",
                            0.0
                        ),
                    currency = o.optString(
                        "currency",
                        "BDT"
                    ),
                    color = o.optInt(
                        "color",
                        0xFF4CAF50.toInt()
                    )
                )
            }
        }
    } catch (_: Exception) {
        listOf(
            Wallet(
                "default_cash",
                "ক্যাশ (নগদ)",
                "Cash",
                0.0,
                "BDT",
                0xFF4CAF50.toInt()
            )
        )
    }
}

fun saveCustomCategories(
    prefs: SharedPreferences,
    categories: List<String>
) {
    val normalized =
        categories
            .asSequence()
            .map {
                it.trim()
                    .replace(
                        Regex("\\s+"),
                        " "
                    )
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sortedBy {
                it.lowercase(
                    Locale.getDefault()
                )
            }
            .toList()

    prefs.edit()
        .putString(
            "custom_categories",
            JSONArray(normalized).toString()
        )
        .apply()
}

fun loadCustomCategories(
    prefs: SharedPreferences
): List<String> {
    return try {
        val raw =
            prefs.getString(
                "custom_categories",
                null
            ) ?: return emptyList()

        val array = JSONArray(raw)

        List(array.length()) { index ->
            array
                .getString(index)
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )
        }.filter {
            it.isNotBlank()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

/**
 * Complete backup JSON.
 *
 * IMPORTANT:
 * This now includes:
 * - Transactions
 * - Loans
 * - Loan payments
 * - Lendings
 * - Lending returns
 * - Wallets
 *
 * Old backup files remain readable because
 * parser uses optJSONArray() for newly added sections.
 */
fun buildBackupJson(
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    wallets: List<Wallet>
): String {

    val root = JSONObject()

    root.put(
        "backupVersion",
        5
    )

    root.put(
        "usdToBdt",
        usdToBdt
    )

    root.put(
        "usdToMvr",
        usdToMvr
    )

    // -------------------------------------------------
    // TRANSACTIONS
    // -------------------------------------------------

    val transactionArray = JSONArray()

    transactions.forEach { transaction ->
        transactionArray.put(
            JSONObject().apply {
                put("id", transaction.id)
                put("type", transaction.type)
                put("amount", transaction.amount)
                put("currency", transaction.currency)
                put("category", transaction.category)
                put("reason", transaction.reason)
                put("date", transaction.date)
                put(
                    "receiptImage",
                    transaction.receiptImage ?: ""
                )
                put(
                    "walletId",
                    transaction.walletId
                )
                put(
                    "addedByUid",
                    transaction.addedByUid ?: ""
                )
                put(
                    "addedByName",
                    transaction.addedByName ?: ""
                )
            }
        )
    }

    root.put(
        "transactions",
        transactionArray
    )

    // -------------------------------------------------
    // LOANS
    // -------------------------------------------------

    val loanArray = JSONArray()

    loans.forEach { loan ->
        loanArray.put(
            JSONObject().apply {
                put("id", loan.id)
                put("name", loan.name)
                put(
                    "sourceType",
                    loan.sourceType
                )
                put(
                    "principal",
                    loan.principal
                )
                put(
                    "monthlyInstallment",
                    loan.monthlyInstallment
                )
                put(
                    "startDate",
                    loan.startDate
                )
                put(
                    "note",
                    loan.note
                )
                put(
                    "lastEditedDate",
                    loan.lastEditedDate
                )
                put(
                    "editHistory",
                    JSONArray(loan.editHistory)
                )
                put(
                    "dueDate",
                    loan.dueDate ?: ""
                )

                val borrowingArray =
                    JSONArray()

                loan.borrowings.forEach {
                    borrowingArray.put(
                        JSONObject().apply {
                            put("id", it.id)
                            put(
                                "loanId",
                                it.loanId
                            )
                            put(
                                "amount",
                                it.amount
                            )
                            put(
                                "date",
                                it.date
                            )
                            put(
                                "note",
                                it.note
                            )
                        }
                    )
                }

                put(
                    "borrowings",
                    borrowingArray
                )
            }
        )
    }

    root.put(
        "loans",
        loanArray
    )

    // -------------------------------------------------
    // LOAN PAYMENTS
    // -------------------------------------------------

    val loanPaymentArray =
        JSONArray()

    loanPayments.forEach { payment ->
        loanPaymentArray.put(
            JSONObject().apply {
                put("id", payment.id)
                put(
                    "loanId",
                    payment.loanId
                )
                put(
                    "amount",
                    payment.amount
                )
                put(
                    "date",
                    payment.date
                )
                put(
                    "note",
                    payment.note
                )
                put(
                    "fundSource",
                    payment.fundSource
                )

                put(
                    "sourceTransactionId",
                    payment.sourceTransactionId ?: 0L
                )
            }
        )
    }

    root.put(
        "loanPayments",
        loanPaymentArray
    )

    // -------------------------------------------------
    // LENDINGS
    // -------------------------------------------------

    val lendingArray =
        JSONArray()

    lendings.forEach { lending ->
        lendingArray.put(
            JSONObject().apply {
                put("id", lending.id)
                put(
                    "person",
                    lending.person
                )
                put(
                    "amount",
                    lending.amount
                )
                put(
                    "date",
                    lending.date
                )
                put(
                    "note",
                    lending.note
                )
                put(
                    "dueDate",
                    lending.dueDate ?: ""
                )
                put(
                    "fundSource",
                    lending.fundSource
                )
            }
        )
    }

    root.put(
        "lendings",
        lendingArray
    )

    // -------------------------------------------------
    // LENDING RETURNS
    // -------------------------------------------------

    val lendingReturnArray =
        JSONArray()

    lendingReturns.forEach { item ->
        lendingReturnArray.put(
            JSONObject().apply {
                put("id", item.id)
                put(
                    "lendingId",
                    item.lendingId
                )
                put(
                    "amount",
                    item.amount
                )
                put(
                    "date",
                    item.date
                )
                put(
                    "note",
                    item.note
                )
                put(
                    "fundSource",
                    item.fundSource
                )
            }
        )
    }

    root.put(
        "lendingReturns",
        lendingReturnArray
    )

    // -------------------------------------------------
    // WALLETS
    // -------------------------------------------------

    val walletArray =
        JSONArray()

    wallets.forEach { wallet ->
        walletArray.put(
            JSONObject().apply {
                put("id", wallet.id)
                put(
                    "name",
                    wallet.name
                )
                put(
                    "type",
                    wallet.type
                )
                put(
                    "initialBalance",
                    wallet.initialBalance
                )
                put(
                    "currency",
                    wallet.currency
                )
                put(
                    "color",
                    wallet.color
                )
            }
        )
    }

    root.put(
        "wallets",
        walletArray
    )

    return root.toString(2)
}

/**
 * Parses both old and new backup files.
 *
 * New sections are optional so an old backup
 * will NOT crash the app.
 */
fun parseBackupJson(
    json: String
): BackupData? {
    return try {

        val root =
            JSONObject(json)

        // -------------------------------------------------
        // TRANSACTIONS
        // -------------------------------------------------

        val transactionArray =
            root.optJSONArray(
                "transactions"
            )

        val transactions =
            mutableListOf<Transaction>()

        if (transactionArray != null) {
            for (
            i in 0 until transactionArray.length()
            ) {
                val o =
                    transactionArray
                        .getJSONObject(i)

                transactions.add(
                    Transaction(
                        id = o.getLong("id"),
                        type = o.getString(
                            "type"
                        ),
                        amount = o.getDouble(
                            "amount"
                        ),
                        currency = o.getString(
                            "currency"
                        ),
                        category = o.getString(
                            "category"
                        ),
                        reason = o.getString(
                            "reason"
                        ),
                        date = o.getString(
                            "date"
                        ),
                        receiptImage =
                            o.optString(
                                "receiptImage"
                            ).takeIf {
                                it.isNotBlank()
                            },
                        walletId =
                            o.optString(
                                "walletId",
                                "default_cash"
                            ),
                        addedByUid =
                            o.optString(
                                "addedByUid"
                            ).takeIf {
                                it.isNotBlank()
                            },
                        addedByName =
                            o.optString(
                                "addedByName"
                            ).takeIf {
                                it.isNotBlank()
                            }
                    )
                )
            }
        }

        // -------------------------------------------------
        // LOANS
        // -------------------------------------------------

        val loanArray =
            root.optJSONArray("loans")

        val loans =
            mutableListOf<LoanAccount>()

        if (loanArray != null) {
            for (
            i in 0 until loanArray.length()
            ) {
                val o =
                    loanArray
                        .getJSONObject(i)

                val historyArray =
                    o.optJSONArray(
                        "editHistory"
                    )

                val history =
                    mutableListOf<String>()

                if (historyArray != null) {
                    for (
                    j in 0 until historyArray.length()
                    ) {
                        history.add(
                            historyArray
                                .getString(j)
                        )
                    }
                }

                val borrowingArray =
                    o.optJSONArray(
                        "borrowings"
                    )

                val borrowings =
                    mutableListOf<LoanBorrowing>()

                if (borrowingArray != null) {
                    for (
                    j in 0 until borrowingArray.length()
                    ) {
                        val b =
                            borrowingArray
                                .getJSONObject(j)

                        borrowings.add(
                            LoanBorrowing(
                                id = b.getLong(
                                    "id"
                                ),
                                loanId = b.getLong(
                                    "loanId"
                                ),
                                amount = b.getDouble(
                                    "amount"
                                ),
                                date = b.getString(
                                    "date"
                                ),
                                note = b.optString(
                                    "note",
                                    ""
                                )
                            )
                        )
                    }
                }

                loans.add(
                    LoanAccount(
                        id = o.getLong(
                            "id"
                        ),
                        name = o.getString(
                            "name"
                        ),
                        sourceType =
                            o.getString(
                                "sourceType"
                            ),
                        principal =
                            o.getDouble(
                                "principal"
                            ),
                        monthlyInstallment =
                            o.getDouble(
                                "monthlyInstallment"
                            ),
                        startDate =
                            o.getString(
                                "startDate"
                            ),
                        note =
                            o.getString(
                                "note"
                            ),
                        lastEditedDate =
                            o.optString(
                                "lastEditedDate",
                                ""
                            ),
                        editHistory =
                            history,
                        borrowings =
                            borrowings,
                        dueDate =
                            o.optString(
                                "dueDate"
                            ).takeIf {
                                it.isNotBlank()
                            }
                    )
                )
            }
        }

        // -------------------------------------------------
        // LOAN PAYMENTS
        // -------------------------------------------------

        val loanPaymentArray =
            root.optJSONArray(
                "loanPayments"
            )

        val loanPayments =
            mutableListOf<LoanPayment>()

        if (loanPaymentArray != null) {
            for (
            i in 0 until loanPaymentArray.length()
            ) {
                val o =
                    loanPaymentArray
                        .getJSONObject(i)

                loanPayments.add(
                    LoanPayment(
                        id = o.getLong(
                            "id"
                        ),
                        loanId =
                            o.getLong(
                                "loanId"
                            ),
                        amount =
                            o.getDouble(
                                "amount"
                            ),
                        date =
                            o.getString(
                                "date"
                            ),
                        note =
                            o.getString(
                                "note"
                            ),
                        fundSource =
                            o.optString(
                                "fundSource",
                                "personal"
                            ),

                        sourceTransactionId =
                            o.optLong(
                                "sourceTransactionId",
                                0L
                            ).takeIf {
                                it != 0L
                            }
                    )
                )
            }
        }

        // -------------------------------------------------
        // LENDINGS
        // -------------------------------------------------

        val lendingArray =
            root.optJSONArray(
                "lendings"
            )

        val lendings =
            mutableListOf<LendingAccount>()

        if (lendingArray != null) {
            for (
            i in 0 until lendingArray.length()
            ) {
                val o =
                    lendingArray
                        .getJSONObject(i)

                lendings.add(
                    LendingAccount(
                        id = o.getLong(
                            "id"
                        ),
                        person =
                            o.getString(
                                "person"
                            ),
                        amount =
                            o.getDouble(
                                "amount"
                            ),
                        date =
                            o.getString(
                                "date"
                            ),
                        note =
                            o.getString(
                                "note"
                            ),
                        dueDate =
                            o.optString(
                                "dueDate"
                            ).takeIf {
                                it.isNotBlank()
                            },
                        fundSource =
                            o.optString(
                                "fundSource",
                                "personal"
                            )
                    )
                )
            }
        }

        // -------------------------------------------------
        // LENDING RETURNS
        // -------------------------------------------------

        val lendingReturnArray =
            root.optJSONArray(
                "lendingReturns"
            )

        val lendingReturns =
            mutableListOf<LendingReturn>()

        if (lendingReturnArray != null) {
            for (
            i in 0 until lendingReturnArray.length()
            ) {
                val o =
                    lendingReturnArray
                        .getJSONObject(i)

                lendingReturns.add(
                    LendingReturn(
                        id = o.getLong(
                            "id"
                        ),
                        lendingId =
                            o.getLong(
                                "lendingId"
                            ),
                        amount =
                            o.getDouble(
                                "amount"
                            ),
                        date =
                            o.getString(
                                "date"
                            ),
                        note =
                            o.getString(
                                "note"
                            ),
                        fundSource =
                            o.optString(
                                "fundSource",
                                "personal"
                            )
                    )
                )
            }
        }

        // -------------------------------------------------
        // WALLETS
        // -------------------------------------------------

        val walletArray =
            root.optJSONArray(
                "wallets"
            )

        val wallets =
            mutableListOf<Wallet>()

        if (walletArray != null) {
            for (
            i in 0 until walletArray.length()
            ) {
                val o =
                    walletArray
                        .getJSONObject(i)

                wallets.add(
                    Wallet(
                        id = o.getString(
                            "id"
                        ),
                        name = o.getString(
                            "name"
                        ),
                        type = o.getString(
                            "type"
                        ),
                        initialBalance =
                            o.optDouble(
                                "initialBalance",
                                0.0
                            ),
                        currency =
                            o.optString(
                                "currency",
                                "BDT"
                            ),
                        color =
                            o.optInt(
                                "color",
                                0xFF4CAF50.toInt()
                            )
                    )
                )
            }
        }

        BackupData(
            transactions =
                transactions,
            usdToBdt =
                root.optDouble(
                    "usdToBdt",
                    0.0
                ),
            usdToMvr =
                root.optDouble(
                    "usdToMvr",
                    0.0
                ),
            loans =
                loans,
            loanPayments =
                loanPayments,
            lendings =
                lendings,
            lendingReturns =
                lendingReturns,
            wallets =
                wallets
        )

    } catch (_: Exception) {
        null
    }
}

fun saveAutoBackup(
    context: Context,
    userId: String,
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    wallets: List<Wallet>
) {
    try {
        context
            .openFileOutput(
                "AmarHisab_AutoBackup_$userId.json",
                Context.MODE_PRIVATE
            )
            .use { output ->
                output.write(
                    buildBackupJson(
                        transactions =
                            transactions,
                        usdToBdt =
                            usdToBdt,
                        usdToMvr =
                            usdToMvr,
                        loans =
                            loans,
                        loanPayments =
                            loanPayments,
                        lendings =
                            lendings,
                        lendingReturns =
                            lendingReturns,
                        wallets =
                            wallets
                    ).toByteArray()
                )
            }
    } catch (_: Exception) {
    }
}

fun loadAutoBackup(
    context: Context,
    userId: String
): BackupData? {
    return try {
        parseBackupJson(
            context
                .openFileInput(
                    "AmarHisab_AutoBackup_$userId.json"
                )
                .bufferedReader()
                .use {
                    it.readText()
                }
        )
    } catch (_: Exception) {
        null
    }
}

fun exportBackupToUri(
    context: Context,
    uri: Uri,
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    wallets: List<Wallet>
): Boolean {
    return try {
        context.contentResolver
            .openOutputStream(uri)
            ?.use { output ->
                output.write(
                    buildBackupJson(
                        transactions =
                            transactions,
                        usdToBdt =
                            usdToBdt,
                        usdToMvr =
                            usdToMvr,
                        loans =
                            loans,
                        loanPayments =
                            loanPayments,
                        lendings =
                            lendings,
                        lendingReturns =
                            lendingReturns,
                        wallets =
                            wallets
                    ).toByteArray()
                )
            }

        true
    } catch (_: Exception) {
        false
    }
}

fun importBackupFromUri(
    context: Context,
    uri: Uri
): BackupData? {
    return try {
        parseBackupJson(
            context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""
        )
    } catch (_: Exception) {
        null
    }
}

// =====================================================
// APP UPDATE CHECKER
// =====================================================

data class AppUpdateInfo(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val updateUrl: String,
    val updateMessage: String,
    val forceUpdate: Boolean
)

object AppUpdateChecker {

    suspend fun checkForUpdate(): AppUpdateInfo? {
        return try {

            val document = FirebaseFirestore
                .getInstance()
                .collection("config")
                .document("app_version")
                .get()
                .await()

            if (!document.exists()) {
                return null
            }

            val latestVersionCode =
                document.getLong("latestVersionCode") ?: return null

            val latestVersionName =
                document.getString("latestVersionName") ?: ""

            val updateUrl =
                document.getString("updateUrl") ?: ""

            val updateMessage =
                document.getString("updateMessage")
                    ?: "অ্যাপটির নতুন ভার্সন এসেছে।"

            val forceUpdate =
                document.getBoolean("forceUpdate") ?: false

            AppUpdateInfo(
                latestVersionCode = latestVersionCode,
                latestVersionName = latestVersionName,
                updateUrl = updateUrl,
                updateMessage = updateMessage,
                forceUpdate = forceUpdate
            )

        } catch (e: Exception) {

            Log.e(
                "AppUpdateChecker",
                "Update check failed",
                e
            )

            null
        }
    }

    fun getCurrentVersionCode(context: Context): Long {

        return try {

            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                packageInfo.longVersionCode

            } else {

                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

        } catch (e: Exception) {

            0L
        }
    }

    fun downloadAndInstall(
        context: Context,
        updateUrl: String,
        versionName: String
    ) {

        try {

            var downloadUrl = updateUrl

            // Google Drive share link থেকে direct download link তৈরি
            if (downloadUrl.contains("drive.google.com/file/d/")) {

                val regex =
                    Regex("drive\\.google\\.com/file/d/([^/]+)")

                val match = regex.find(downloadUrl)

                if (match != null) {

                    val fileId = match.groupValues[1]

                    downloadUrl =
                        "https://drive.google.com/uc?export=download&id=$fileId"
                }
            }

            Log.d(
                "AppUpdateChecker",
                "Download URL = $downloadUrl"
            )

            val fileName =
                "AmarHisab-$versionName.apk"

            val request =
                DownloadManager.Request(Uri.parse(downloadUrl))

            request.setTitle("Amar Hisab Update")

            request.setDescription(
                "Amar Hisab $versionName download হচ্ছে..."
            )

            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )

            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

            request.setMimeType("application/vnd.android.package-archive")

            val downloadManager =
                context.getSystemService(
                    Context.DOWNLOAD_SERVICE
                ) as DownloadManager

            val downloadId =
                downloadManager.enqueue(request)

            Log.d(
                "AppUpdateChecker",
                "Download started: $downloadId"
            )

            Handler(
                Looper.getMainLooper()
            ).postDelayed({

                try {

                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )

                    val apkFile =
                        File(downloadsDir, fileName)

                    if (!apkFile.exists()) {

                        Log.e(
                            "AppUpdateChecker",
                            "APK file not found"
                        )

                        return@postDelayed
                    }

                    val apkUri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            apkFile
                        )

                    val installIntent =
                        Intent(
                            Intent.ACTION_VIEW
                        ).apply {

                            setDataAndType(
                                apkUri,
                                "application/vnd.android.package-archive"
                            )

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }

                    context.startActivity(installIntent)

                } catch (e: Exception) {

                    Log.e(
                        "AppUpdateChecker",
                        "Install failed",
                        e
                    )
                }

            }, 8000)

        } catch (e: Exception) {

            Log.e(
                "AppUpdateChecker",
                "Download failed",
                e
            )
        }
    }
}

@Composable
fun AppUpdateDialog(
    context: Context
) {

    var updateInfo by remember {
        mutableStateOf<AppUpdateInfo?>(null)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        val info =
            AppUpdateChecker.checkForUpdate()

        Log.d(
            "AppUpdateChecker",
            "Firestore info = $info"
        )

        if (info != null) {

            val currentVersionCode =
                AppUpdateChecker.getCurrentVersionCode(
                    context
                )

            Log.d(
                "AppUpdateChecker",
                "Current versionCode = $currentVersionCode, Latest versionCode = ${info.latestVersionCode}"
            )

            if (info.latestVersionCode > currentVersionCode) {

                updateInfo = info

                showDialog = true
            }
        }
    }

    if (showDialog && updateInfo != null) {

        val info = updateInfo!!

        AlertDialog(

            onDismissRequest = {

                if (!info.forceUpdate) {
                    showDialog = false
                }
            },

            title = {
                Text("নতুন আপডেট পাওয়া গেছে 🎉")
            },

            text = {

                Text(
                    "Amar Hisab-এর নতুন ভার্সন ${info.latestVersionName} এসেছে।\n\n" +
                            info.updateMessage
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        AppUpdateChecker.downloadAndInstall(
                            context = context,
                            updateUrl = info.updateUrl,
                            versionName = info.latestVersionName
                        )

                        showDialog = false
                    }

                ) {

                    Text("Update")
                }
            },

            dismissButton = {

                if (!info.forceUpdate) {

                    TextButton(

                        onClick = {
                            showDialog = false
                        }

                    ) {

                        Text("পরে করব")
                    }
                }
            }
        )
    }
}

// =====================================================
// LOAN INTEREST TERMS
// =====================================================

private const val LOAN_INTEREST_TERMS_KEY =
    "loan_interest_terms_v1"

fun saveLoanInterestTerms(
    prefs: SharedPreferences,
    terms: List<LoanInterestTerms>
) {
    val array = JSONArray()

    terms.forEach { item ->
        array.put(
            JSONObject().apply {
                put("loanId", item.loanId)
                put("interestRate", item.interestRate)
                put("totalInterest", item.totalInterest)
                put("interestType", item.interestType)
            }
        )
    }

    prefs.edit()
        .putString(
            LOAN_INTEREST_TERMS_KEY,
            array.toString()
        )
        .apply()
}

fun loadLoanInterestTerms(
    prefs: SharedPreferences
): List<LoanInterestTerms> {

    val raw =
        prefs.getString(
            LOAN_INTEREST_TERMS_KEY,
            null
        ) ?: return emptyList()

    return try {

        val array = JSONArray(raw)

        List(array.length()) { index ->

            val item =
                array.getJSONObject(index)

            LoanInterestTerms(
                loanId =
                    item.optLong(
                        "loanId"
                    ),

                interestRate =
                    item.optDouble(
                        "interestRate",
                        0.0
                    ),

                totalInterest =
                    item.optDouble(
                        "totalInterest",
                        0.0
                    ),

                interestType =
                    item.optString(
                        "interestType",
                        "fixed"
                    )
            )
        }

    } catch (_: Exception) {
        emptyList()
    }
}


// =====================================================
// BIRTHDAY
// =====================================================

private const val BIRTHDAY_MONTH_KEY =
    "birthday_month"

private const val BIRTHDAY_DAY_KEY =
    "birthday_day"

fun getBirthday(
    prefs: SharedPreferences
): Pair<Int, Int>? {

    if (
        !prefs.contains(BIRTHDAY_MONTH_KEY) ||
        !prefs.contains(BIRTHDAY_DAY_KEY)
    ) {
        return null
    }

    val month =
        prefs.getInt(
            BIRTHDAY_MONTH_KEY,
            -1
        )

    val day =
        prefs.getInt(
            BIRTHDAY_DAY_KEY,
            -1
        )

    if (
        month < 0 ||
        day <= 0
    ) {
        return null
    }

    return Pair(
        month,
        day
    )
}

fun saveBirthday(
    prefs: SharedPreferences,
    month: Int,
    day: Int
) {
    prefs.edit()
        .putInt(
            BIRTHDAY_MONTH_KEY,
            month
        )
        .putInt(
            BIRTHDAY_DAY_KEY,
            day
        )
        .apply()
}

// =====================================================
// BIRTHDAY POPUP / CELEBRATION
// =====================================================

private fun isBirthdayToday(
    birthday: Pair<Int, Int>
): Boolean {

    val today = Calendar.getInstance()

    return today.get(Calendar.MONTH) ==
            birthday.first &&
            today.get(Calendar.DAY_OF_MONTH) ==
            birthday.second
}

@Composable
fun BirthdayPopupCheck(
    userId: String,
    birthday: Pair<Int, Int>?
) {
    val context = LocalContext.current
    val prefs = remember(userId) { AccountStorage.getPrefs(context, userId) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val lastShownYear = prefs.getInt("last_birthday_celebration_year", -1)

    var showBirthday by remember(userId, birthday) {
        mutableStateOf(
            birthday != null &&
            isBirthdayToday(birthday) &&
            lastShownYear < currentYear
        )
    }

    if (showBirthday) {
        BirthdayCelebrationModal(
            onDismiss = {
                prefs.edit().putInt("last_birthday_celebration_year", currentYear).apply()
                showBirthday = false
            }
        )
    }
}

@Composable
fun BirthdayCelebrationModal(
    onDismiss: () -> Unit
) {

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "birthday_animation"
        )

    val scale by
    infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 900,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "cake_scale"
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.80f
                    )
                ),
        contentAlignment =
            Alignment.Center
    ) {

        BirthdayConfetti(
            modifier =
                Modifier.fillMaxSize()
        )

        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .background(
                        color =
                            Color(0xFF123C3A),
                        shape =
                            RoundedCornerShape(28.dp)
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 30.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "🎂",
                fontSize = 60.sp,
                modifier =
                    Modifier.scale(scale)
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    "শুভ জন্মদিন! 🎉",
                color =
                    Color.White,
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "আজ তোমার বিশেষ দিন।\n" +
                            "Amar Hisab-এর পক্ষ থেকে রইল\n" +
                            "অনেক শুভকামনা! 💚",
                color =
                    Color.White.copy(
                        alpha = 0.9f
                    ),
                fontSize = 17.sp,
                lineHeight = 26.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Text(
                text =
                    "🎁  ✨  🎂  ✨  🎁",
                fontSize = 30.sp
            )

            Spacer(
                modifier =
                    Modifier.height(25.dp)
            )

            Button(
                onClick =
                    onDismiss,
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF00E676)
                    )
            ) {

                Text(
                    text =
                        "ধন্যবাদ ❤️",
                    color =
                        Color.Black,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BirthdayConfetti(
    modifier: Modifier
) {

    val pieces = remember {

        List(45) {

            ConfettiPiece(
                x =
                    Random.nextFloat(),
                y =
                    Random.nextFloat(),
                size =
                    Random.nextInt(
                        5,
                        12
                    ).toFloat(),
                rotation =
                    Random.nextFloat() * 360f,
                color =
                    listOf(
                        Color(0xFF00E676),
                        Color(0xFFFFD54F),
                        Color(0xFFFF4081),
                        Color(0xFF40C4FF),
                        Color(0xFFFF6D00)
                    ).random()
            )
        }
    }

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "confetti_animation"
        )

    val movement by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 3000
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "confetti_movement"
    )

    Canvas(
        modifier = modifier
    ) {

        pieces.forEach { piece ->

            val yPosition =
                (
                        (piece.y + movement) % 1f
                        ) * size.height

            rotate(
                degrees =
                    piece.rotation,
                pivot =
                    Offset(
                        x =
                            piece.x *
                                    size.width,
                        y =
                            yPosition
                    )
            ) {

                drawCircle(
                    color =
                        piece.color,
                    radius =
                        piece.size,
                    center =
                        Offset(
                            x =
                                piece.x *
                                        size.width,
                            y =
                                yPosition
                        )
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val size: Float,
    val rotation: Float,
    val color: Color
)