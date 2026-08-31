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
                Text("নতুন আপডেট পাওয়া গেছে 🎉")
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

private const val BIRTHDAY_MONTH_KEY = "birthday_month"
private const val BIRTHDAY_DAY_KEY = "birthday_day"

fun getBirthday(
    prefs: SharedPreferences
): Pair<Int, Int>? {

    if (!prefs.contains(BIRTHDAY_MONTH_KEY) ||
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

    if (month !in 0..11 || day !in 1..31) {
        return null
    }

    return Pair(month, day)
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

private fun daysUntilBirthday(
    birthday: Pair<Int, Int>
): Int {

    val todayStart = Calendar.getInstance().apply {
        set(
            Calendar.HOUR_OF_DAY,
            0
        )
        set(
            Calendar.MINUTE,
            0
        )
        set(
            Calendar.SECOND,
            0
        )
        set(
            Calendar.MILLISECOND,
            0
        )
    }

    val birthdayDate =
        Calendar.getInstance().apply {
            set(
                Calendar.MONTH,
                birthday.first
            )
            set(
                Calendar.DAY_OF_MONTH,
                birthday.second
            )
            set(
                Calendar.HOUR_OF_DAY,
                0
            )
            set(
                Calendar.MINUTE,
                0
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    if (birthdayDate.before(todayStart)) {
        birthdayDate.add(
            Calendar.YEAR,
            1
        )
    }

    val difference =
        birthdayDate.timeInMillis -
                todayStart.timeInMillis

    return (
            difference /
                    (24L * 60L * 60L * 1000L)
            ).toInt()
}

private fun isBirthdayToday(
    birthday: Pair<Int, Int>
): Boolean {

    val today = Calendar.getInstance()

    return today.get(Calendar.MONTH) ==
            birthday.first &&
            today.get(Calendar.DAY_OF_MONTH) ==
            birthday.second
}

private fun formatBirthday(
    birthday: Pair<Int, Int>
): String {

    val calendar = Calendar.getInstance().apply {
        set(
            Calendar.MONTH,
            birthday.first
        )
        set(
            Calendar.DAY_OF_MONTH,
            birthday.second
        )
    }

    val month =
        calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            Locale.ENGLISH
        ) ?: ""

    return String.format(
        "%02d %s",
        birthday.second,
        month
    )
}

@Composable
fun BirthdayCountdownCard(
    userId: String,
    currentBirthday: Pair<Int, Int>?,
    onBirthdaySet: (Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier.Companion,
    isCompact: Boolean = false
) {
    val context = LocalContext.current
    val prefs = remember(userId) { AccountStorage.getPrefs(context, userId) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val today = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                saveBirthday(prefs, month, dayOfMonth)
                onBirthdaySet(Pair(month, dayOfMonth))
                showDatePicker = false
            },
            today.get(Calendar.YEAR),
            currentBirthday?.first ?: today.get(Calendar.MONTH),
            currentBirthday?.second ?: today.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

    if (currentBirthday == null) {
        BirthdaySetupCard(
            modifier = modifier,
            onSetBirthday = { showDatePicker = true },
            isCompact = isCompact
        )
        return
    }

    val days = daysUntilBirthday(currentBirthday)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isCompact) 12.dp else 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎂 Birthday",
                    color = Color.White,
                    fontSize = if (isCompact) 14.sp else 19.sp,
                    fontWeight = FontWeight.Bold
                )

                if (days == 0) {
                    Text(
                        text = "🎉 আজ তোমার জন্মদিন!",
                        color = Color(0xFF00E676),
                        fontSize = if (isCompact) 13.sp else 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$days দিন ",
                            color = Color(0xFF00E676),
                            fontSize = if (isCompact) 20.sp else 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বাকি",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = if (isCompact) 11.sp else 14.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(28.dp)) {
                Text(if (days == 0) "🎉" else "🎁", fontSize = if (isCompact) 24.sp else 40.sp)
            }
        }
    }
}

@Composable
private fun BirthdaySetupCard(
    modifier: Modifier = Modifier.Companion,
    onSetBirthday: () -> Unit,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(if (isCompact) 12.dp else 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎂 Birthday",
                    color = Color.White,
                    fontSize = if (isCompact) 14.sp else 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সেট করুন",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = if (isCompact) 11.sp else 14.sp
                )
            }
            Button(
                onClick = onSetBirthday,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Set", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
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
fun BirthdayCelebrationCheck(
    userId: String,
    birthday: Pair<Int, Int>?,
    onBirthdaySet: (Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    BirthdayCountdownCard(
        userId = userId,
        currentBirthday = birthday,
        onBirthdaySet = onBirthdaySet,
        modifier = modifier
    )

    BirthdayPopupCheck(
        userId = userId,
        birthday = birthday
    )
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
                            androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
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
                    androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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