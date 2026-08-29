package com.eleyas.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.eleyas.expensetracker.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

fun saveCategoryBudgets(prefs: SharedPreferences, budgets: List<CategoryBudget>) {
    val array = JSONArray(); budgets.forEach { array.put(JSONObject().apply { put("month", it.month); put("category", it.category); put("limit", it.limit) }) }
    prefs.edit().putString("category_budgets", array.toString()).apply()
}

fun loadCategoryBudgets(prefs: SharedPreferences): List<CategoryBudget> {
    return try {
        val array = JSONArray(prefs.getString("category_budgets", "[]") ?: "[]")
        List(array.length()) { CategoryBudget(array.getJSONObject(it).getString("month"), array.getJSONObject(it).getString("category"), array.getJSONObject(it).getDouble("limit")) }
    } catch (_: Exception) { emptyList() }
}

fun saveTransactionToFirestore(firestore: FirebaseFirestore, userId: String, transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
    firestore.collection("users").document(userId).collection("transactions").document(transaction.id.toString()).set(mapOf("id" to transaction.id, "type" to transaction.type, "amount" to transaction.amount, "currency" to transaction.currency, "category" to transaction.category, "reason" to transaction.reason, "date" to transaction.date, "receiptImage" to (transaction.receiptImage ?: ""), "walletId" to transaction.walletId)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it.message ?: "Error") }
}

fun deleteTransactionFromFirestore(firestore: FirebaseFirestore, userId: String, transactionId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
    firestore.collection("users").document(userId).collection("transactions").document(transactionId.toString()).delete().addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it.message ?: "Error") }
}

fun firestoreDocumentToTransaction(doc: DocumentSnapshot): Transaction? = try {
    Transaction(doc.getLong("id") ?: 0L, doc.getString("type") ?: "", doc.getDouble("amount") ?: 0.0, doc.getString("currency") ?: "BDT", doc.getString("category") ?: "Other", doc.getString("reason") ?: "", doc.getString("date") ?: "", doc.getString("receiptImage")?.ifBlank { null }, doc.getString("walletId") ?: "default_cash", doc.getString("addedByUid")?.ifBlank { null }, doc.getString("addedByName")?.ifBlank { null })
} catch (_: Exception) { null }

suspend fun getLiveRates(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    try {
        val json = JSONObject(URL("https://open.er-api.com/v6/latest/USD").readText())
        if (json.getString("result") == "success") Pair(json.getJSONObject("rates").getDouble("BDT"), json.getJSONObject("rates").getDouble("MVR")) else null
    } catch (_: Exception) { null }
}

fun saveTransactions(prefs: SharedPreferences, list: List<Transaction>) {
    val array = JSONArray(); list.forEach { array.put(JSONObject().apply { put("id", it.id); put("type", it.type); put("amount", it.amount); put("currency", it.currency); put("category", it.category); put("reason", it.reason); put("date", it.date); put("receiptImage", it.receiptImage ?: ""); put("walletId", it.walletId); put("addedByUid", it.addedByUid ?: ""); put("addedByName", it.addedByName ?: "") }) }
    prefs.edit().putString("transactions", array.toString()).apply()
}

fun loadTransactions(prefs: SharedPreferences): List<Transaction> = try {
    val array = JSONArray(prefs.getString("transactions", null) ?: "[]")
    List(array.length()) { i -> val o = array.getJSONObject(i); Transaction(o.getLong("id"), o.getString("type"), o.getDouble("amount"), o.getString("currency"), o.getString("category"), o.getString("reason"), o.getString("date"), o.optString("receiptImage").takeIf { it.isNotBlank() }, o.optString("walletId", "default_cash"), o.optString("addedByUid").takeIf { it.isNotBlank() }, o.optString("addedByName").takeIf { it.isNotBlank() }) }
} catch (_: Exception) { emptyList() }

fun saveLoans(prefs: SharedPreferences, list: List<LoanAccount>) {
    val array = JSONArray(); list.forEach { l -> array.put(JSONObject().apply { put("id", l.id); put("name", l.name); put("sourceType", l.sourceType); put("principal", l.principal); put("monthlyInstallment", l.monthlyInstallment); put("startDate", l.startDate); put("note", l.note); put("editHistory", JSONArray(l.editHistory)); put("dueDate", l.dueDate ?: ""); val bArr = JSONArray(); l.borrowings.forEach { b -> bArr.put(JSONObject().apply { put("id", b.id); put("loanId", b.loanId); put("amount", b.amount); put("date", b.date); put("note", b.note) }) }; put("borrowings", bArr) }) }
    prefs.edit().putString("loans", array.toString()).apply()
}

fun loadLoans(prefs: SharedPreferences): List<LoanAccount> = try {
    val array = JSONArray(prefs.getString("loans", null) ?: "[]")
    List(array.length()) { i ->
        val o = array.getJSONObject(i); val h = o.optJSONArray("editHistory"); val eH = mutableListOf<String>(); if (h != null) for (j in 0 until h.length()) eH.add(h.getString(j))
        val bA = o.optJSONArray("borrowings"); val bS = mutableListOf<LoanBorrowing>(); if (bA != null) for (j in 0 until bA.length()) { val bO = bA.getJSONObject(j); bS.add(LoanBorrowing(bO.getLong("id"), bO.getLong("loanId"), bO.getDouble("amount"), bO.getString("date"), bO.getString("note"))) }
        LoanAccount(o.getLong("id"), o.getString("name"), o.getString("sourceType"), o.getDouble("principal"), o.getDouble("monthlyInstallment"), o.getString("startDate"), o.getString("note"), o.optString("lastEditedDate", ""), eH, bS, o.optString("dueDate").takeIf { it.isNotBlank() })
    }
} catch (_: Exception) { emptyList() }

fun saveLoanPayments(prefs: SharedPreferences, list: List<LoanPayment>) {
    val array = JSONArray(); list.forEach { array.put(JSONObject().apply { put("id", it.id); put("loanId", it.loanId); put("amount", it.amount); put("date", it.date); put("note", it.note) }) }
    prefs.edit().putString("loanPayments", array.toString()).apply()
}

fun loadLoanPayments(prefs: SharedPreferences): List<LoanPayment> = try {
    val array = JSONArray(prefs.getString("loanPayments", null) ?: "[]")
    List(array.length()) { i -> val o = array.getJSONObject(i); LoanPayment(o.getLong("id"), o.getLong("loanId"), o.getDouble("amount"), o.getString("date"), o.getString("note")) }
} catch (_: Exception) { emptyList() }

fun saveLendings(prefs: SharedPreferences, list: List<LendingAccount>) {
    val array = JSONArray(); list.forEach { array.put(JSONObject().apply { put("id", it.id); put("person", it.person); put("amount", it.amount); put("date", it.date); put("note", it.note); put("dueDate", it.dueDate ?: "") }) }
    prefs.edit().putString("lendings", array.toString()).apply()
}

fun loadLendings(prefs: SharedPreferences): List<LendingAccount> = try {
    val array = JSONArray(prefs.getString("lendings", null) ?: "[]")
    List(array.length()) { i -> val o = array.getJSONObject(i); LendingAccount(o.getLong("id"), o.getString("person"), o.getDouble("amount"), o.getString("date"), o.getString("note"), o.optString("dueDate").takeIf { it.isNotBlank() }) }
} catch (_: Exception) { emptyList() }

fun saveLendingReturns(prefs: SharedPreferences, list: List<LendingReturn>) {
    val array = JSONArray(); list.forEach { array.put(JSONObject().apply { put("id", it.id); put("lendingId", it.lendingId); put("amount", it.amount); put("date", it.date); put("note", it.note) }) }
    prefs.edit().putString("lendingReturns", array.toString()).apply()
}

fun loadLendingReturns(prefs: SharedPreferences): List<LendingReturn> = try {
    val array = JSONArray(prefs.getString("lendingReturns", null) ?: "[]")
    List(array.length()) { i -> val o = array.getJSONObject(i); LendingReturn(o.getLong("id"), o.getLong("lendingId"), o.getDouble("amount"), o.getString("date"), o.getString("note")) }
} catch (_: Exception) { emptyList() }

fun saveWallets(prefs: SharedPreferences, list: List<Wallet>) {
    val array = JSONArray(); list.forEach { array.put(JSONObject().apply { put("id", it.id); put("name", it.name); put("type", it.type); put("initialBalance", it.initialBalance); put("currency", it.currency); put("color", it.color) }) }
    prefs.edit().putString("wallets", array.toString()).apply()
}

fun loadWallets(prefs: SharedPreferences): List<Wallet> = try {
    val raw = prefs.getString("wallets", null)
    if (raw == null) {
        listOf(Wallet("default_cash", "ক্যাশ (নগদ)", "Cash", 0.0, "BDT", 0xFF4CAF50.toInt()))
    } else {
        val array = JSONArray(raw)
        List(array.length()) { i -> val o = array.getJSONObject(i); Wallet(o.getString("id"), o.getString("name"), o.getString("type"), o.optDouble("initialBalance", 0.0), o.optString("currency", "BDT"), o.optInt("color", 0xFF4CAF50.toInt())) }
    }
} catch (_: Exception) { listOf(Wallet("default_cash", "ক্যাশ (নগদ)", "Cash", 0.0, "BDT", 0xFF4CAF50.toInt())) }

fun saveCustomCategories(prefs: SharedPreferences, categories: List<String>) {
    val normalized = categories.asSequence()
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
        .distinct()
        .sortedBy { it.lowercase(Locale.getDefault()) }
        .toList()
    prefs.edit().putString("custom_categories", JSONArray(normalized).toString()).apply()
}

fun loadCustomCategories(prefs: SharedPreferences): List<String> = try {
    val raw = prefs.getString("custom_categories", null) ?: return emptyList()
    val array = JSONArray(raw)
    List(array.length()) { index ->
        val value = array.getString(index)
        value.trim().replace(Regex("\\s+"), " ")
    }.filter { it.isNotBlank() }
} catch (_: Exception) { emptyList() }

fun buildBackupJson(transactions: List<Transaction>, usdToBdt: Double, usdToMvr: Double, loans: List<LoanAccount>, loanPayments: List<LoanPayment>, lendings: List<LendingAccount>, lendingReturns: List<LendingReturn>, wallets: List<Wallet>): String {
    val root = JSONObject(); root.put("backupVersion", 4); root.put("usdToBdt", usdToBdt); root.put("usdToMvr", usdToMvr)
    val tA = JSONArray(); transactions.forEach { tA.put(JSONObject().apply { put("id", it.id); put("type", it.type); put("amount", it.amount); put("currency", it.currency); put("category", it.category); put("reason", it.reason); put("date", it.date); put("receiptImage", it.receiptImage ?: ""); put("walletId", it.walletId) }) }; root.put("transactions", tA)
    val lA = JSONArray(); loans.forEach { lA.put(JSONObject().apply { put("id", it.id); put("name", it.name); put("sourceType", it.sourceType); put("principal", it.principal); put("monthlyInstallment", it.monthlyInstallment); put("startDate", it.startDate); put("note", it.note) }) }; root.put("loans", lA)
    val wA = JSONArray(); wallets.forEach { wA.put(JSONObject().apply { put("id", it.id); put("name", it.name); put("type", it.type); put("initialBalance", it.initialBalance); put("currency", it.currency); put("color", it.color) }) }; root.put("wallets", wA)
    return root.toString(2)
}

fun parseBackupJson(json: String): BackupData? = try {
    val root = JSONObject(json); val tA = root.getJSONArray("transactions"); val tL = mutableListOf<Transaction>()
    for (i in 0 until tA.length()) { val o = tA.getJSONObject(i); tL.add(Transaction(o.getLong("id"), o.getString("type"), o.getDouble("amount"), o.getString("currency"), o.getString("category"), o.getString("reason"), o.getString("date"), o.optString("receiptImage").takeIf { it.isNotBlank() }, o.optString("walletId", "default_cash"))) }
    val wA = root.optJSONArray("wallets"); val wL = mutableListOf<Wallet>()
    if (wA != null) for (i in 0 until wA.length()) { val o = wA.getJSONObject(i); wL.add(Wallet(o.getString("id"), o.getString("name"), o.getString("type"), o.optDouble("initialBalance", 0.0), o.optString("currency", "BDT"), o.optInt("color", 0xFF4CAF50.toInt()))) }
    BackupData(tL, root.optDouble("usdToBdt", 0.0), root.optDouble("usdToMvr", 0.0), wallets = wL)
} catch (_: Exception) { null }

fun saveAutoBackup(context: Context, userId: String, transactions: List<Transaction>, usdToBdt: Double, usdToMvr: Double, loans: List<LoanAccount>, loanPayments: List<LoanPayment>, lendings: List<LendingAccount>, lendingReturns: List<LendingReturn>, wallets: List<Wallet>) {
    try { context.openFileOutput("AmarHisab_AutoBackup_$userId.json", Context.MODE_PRIVATE).use { it.write(buildBackupJson(transactions, usdToBdt, usdToMvr, loans, loanPayments, lendings, lendingReturns, wallets).toByteArray()) } } catch (_: Exception) {}
}

fun loadAutoBackup(context: Context, userId: String): BackupData? = try {
    parseBackupJson(context.openFileInput("AmarHisab_AutoBackup_$userId.json").bufferedReader().use { it.readText() })
} catch (_: Exception) { null }

fun exportBackupToUri(context: Context, uri: Uri, transactions: List<Transaction>, usdToBdt: Double, usdToMvr: Double, loans: List<LoanAccount>, loanPayments: List<LoanPayment>, lendings: List<LendingAccount>, lendingReturns: List<LendingReturn>, wallets: List<Wallet>): Boolean = try {
    context.contentResolver.openOutputStream(uri)?.use { it.write(buildBackupJson(transactions, usdToBdt, usdToMvr, loans, loanPayments, lendings, lendingReturns, wallets).toByteArray()) }; true
} catch (_: Exception) { false }

fun importBackupFromUri(context: Context, uri: Uri): BackupData? = try {
    parseBackupJson(context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: "")
} catch (_: Exception) { null }

private const val LOAN_INTEREST_TERMS_KEY = "loan_interest_terms_v1"

fun saveLoanInterestTerms(prefs: SharedPreferences, terms: List<LoanInterestTerms>) {
    val array = JSONArray()
    terms.forEach { item ->
        array.put(JSONObject().apply {
            put("loanId", item.loanId)
            put("interestRate", item.interestRate)
            put("totalInterest", item.totalInterest)
            put("interestType", item.interestType)
        })
    }
    prefs.edit().putString(LOAN_INTEREST_TERMS_KEY, array.toString()).apply()
}

fun loadLoanInterestTerms(prefs: SharedPreferences): List<LoanInterestTerms> {
    val raw = prefs.getString(LOAN_INTEREST_TERMS_KEY, null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            LoanInterestTerms(
                loanId = item.optLong("loanId"),
                interestRate = item.optDouble("interestRate", 0.0),
                totalInterest = item.optDouble("totalInterest", 0.0),
                interestType = item.optString("interestType", "fixed")
            )
        }
    } catch (_: Exception) { emptyList() }
}
