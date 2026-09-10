
package com.eleyas.expensetracker.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleyas.expensetracker.model.NotificationItem
import com.eleyas.expensetracker.NotificationStorage
import com.eleyas.expensetracker.*
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.repository.HouseholdRepository
import com.eleyas.expensetracker.repository.syncAllLoanAndLendingData
import com.eleyas.expensetracker.ui.components.WarningPopupManager
import com.eleyas.expensetracker.util.*
import com.eleyas.expensetracker.util.HouseholdStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Personal (নিজের) transactions — user collection-এর data।
    private var personalTransactions by mutableStateOf<List<Transaction>>(emptyList())

    // Household (পরিবার) থেকে আসা shared home transactions।
    private var sharedHomeTransactions by mutableStateOf<List<Transaction>>(emptyList())

    // UI-তে যে combined list যায় (personal + family shared, id দিয়ে dedupe)।
    val transactions: List<Transaction>
        get() = (personalTransactions + sharedHomeTransactions).distinctBy { it.id }

    var household by mutableStateOf<Household?>(null)
        private set
    var householdBusy by mutableStateOf(false)
        private set
    var loans by mutableStateOf<List<LoanAccount>>(emptyList())
        private set
    var categoryBudgets by mutableStateOf<List<CategoryBudget>>(emptyList())
        private set
    var loanPayments by mutableStateOf<List<LoanPayment>>(emptyList())
        private set
    var loanInterestTerms by mutableStateOf<List<LoanInterestTerms>>(emptyList())
        private set
    var lendings by mutableStateOf<List<LendingAccount>>(emptyList())
        private set
    var lendingReturns by mutableStateOf<List<LendingReturn>>(emptyList())
        private set
    var wallets by mutableStateOf<List<Wallet>>(emptyList())
        private set
    var savingsGoals by mutableStateOf<List<SavingsGoal>>(emptyList())
        private set
    var shoppingItems by mutableStateOf<List<ShoppingItem>>(emptyList())
        private set
    var wishlistItems by mutableStateOf<List<WishlistItem>>(emptyList())
        private set
    var usdToBdt by mutableDoubleStateOf(0.0)
        private set
    var usdToMvr by mutableDoubleStateOf(0.0)
        private set
    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set
    var birthday by mutableStateOf<Pair<Int, Int>?>(null)
        private set

    val totalIncome by derivedStateOf { transactions.filter { it.type == "income" }.sumOf { convertToBdt(it.amount, it.currency) } }
    val totalExpense by derivedStateOf { transactions.filter { it.type == "expense" }.sumOf { convertToBdt(it.amount, it.currency) } }
    val totalHome by derivedStateOf { transactions.filter { it.type == "home" }.sumOf { convertToBdt(it.amount, it.currency) } }
    val totalHomeExpense by derivedStateOf { transactions.filter { it.type == "home_expense" }.sumOf { convertToBdt(it.amount, it.currency) } }

    // Home-only adjustment. এটি Personal balance-এ যাবে না।
    val totalHomeAdjustment by derivedStateOf {
        transactions
            .filter { it.type == "home_adjustment" }
            .sumOf { convertToBdt(it.amount, it.currency) }
    }

    // যে Loan payment Home-এর টাকা থেকে করা হয়েছে, শুধু সেগুলো Home balance কমাবে।
    val totalHomeLoanPaid by derivedStateOf {
        loanPayments
            .filter { isHomeLoanPayment(it) }
            .sumOf { it.amount }
    }

    val totalPersonalLoanPaid by derivedStateOf {
        loanPayments
            .filterNot { isHomeLoanPayment(it) }
            .sumOf { it.amount }
    }

    val totalLoanReceived by derivedStateOf { loans.sumOf { it.principal } }
    val totalLoanInterest by derivedStateOf { loans.sumOf { loan -> loanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0 } }
    val totalLoanPaid by derivedStateOf { loanPayments.sumOf { it.amount } }
    val totalLoanRemaining by derivedStateOf { (totalLoanReceived + totalLoanInterest - totalLoanPaid).coerceAtLeast(0.0) }
    val totalMoneyLent by derivedStateOf { lendings.sumOf { it.amount } }
    val totalMoneyReturned by derivedStateOf { lendingReturns.sumOf { it.amount } }
    val totalMoneyToReceive by derivedStateOf { (totalMoneyLent - totalMoneyReturned).coerceAtLeast(0.0) }

    // Home balance = Home transfer + Home adjustment - Home expense - Home-funded Loan payment.
    val homeBalance by derivedStateOf {
        (
                totalHome +
                        totalHomeAdjustment -
                        totalHomeExpense -
                        totalHomeLoanPaid
                ).coerceAtLeast(0.0)
    }

    // Home-funded Loan payment Personal balance-এ আরেকবার subtract হবে না।
    val balance by derivedStateOf {
        wallets.sumOf { it.initialBalance } +
                totalIncome +
                totalLoanReceived +
                totalMoneyReturned -
                totalExpense -
                totalHome -
                totalPersonalLoanPaid -
                totalMoneyLent
    }

    var rateLoading by mutableStateOf(false)
        private set
    var rateError by mutableStateOf("")
        private set
    var cloudLoading by mutableStateOf(false)
        private set
    var cloudError by mutableStateOf("")
        private set

    private var currentUserId: String = "guest"
    private lateinit var prefs: SharedPreferences

    private val registrations = mutableListOf<ListenerRegistration>()

    private var householdRegistration: ListenerRegistration? = null
    private var sharedHomeRegistration: ListenerRegistration? = null

    fun init(context: Context, userId: String, sharedPrefs: SharedPreferences) {
        if (currentUserId == userId && ::prefs.isInitialized) return

        currentUserId = userId
        prefs = sharedPrefs

        // Clear old registrations
        registrations.forEach { it.remove() }
        registrations.clear()
        detachHouseholdListeners()

        // Load local data
        personalTransactions = loadTransactions(prefs)
        loans = loadLoans(prefs)
        categoryBudgets = loadCategoryBudgets(prefs)
        loanPayments = loadLoanPayments(prefs)
        loanInterestTerms = loadLoanInterestTerms(prefs)
        lendings = loadLendings(prefs)
        lendingReturns = loadLendingReturns(prefs)
        wallets = loadWallets(prefs)
        savingsGoals = loadSavingsGoals(prefs)
        shoppingItems = ShoppingListStorage.load(context, userId)
        wishlistItems = WishlistStorage.load(context, userId)
        notifications = NotificationStorage.load(context, userId)
        birthday = getBirthday(prefs)
        usdToBdt = prefs.getFloat("usd_to_bdt", 0f).toDouble()
        usdToMvr = prefs.getFloat("usd_to_mvr", 0f).toDouble()

        refreshRate()

        if (userId != "guest") {
            setupFirestoreListeners(userId)

            // আগের সেশনে join করা household থাকলে লোড করে listener attach করা হয়
            household = HouseholdStorage.loadHouseholdCache(context, userId)
            HouseholdStorage.loadHouseholdId(context, userId)?.let { attachHouseholdListeners(context, it) }
        }

        checkDueReminders(context)
    }

    private fun checkDueReminders(context: Context) {
        val today = Calendar.getInstance()
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Check Loans
        loans.filter { it.dueDate != null }.forEach { loan ->
            try {
                val due = Calendar.getInstance().apply { time = format.parse(loan.dueDate!!)!! }
                val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                val interest =
                    loanInterestTerms
                        .firstOrNull { it.loanId == loan.id }
                        ?.totalInterest
                        ?: 0.0
                val remaining = loan.principal + interest - paid

                if (remaining > 0) {
                    val diffDays = (due.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)
                    if (diffDays in 0..2) {
                        sendNotification(context, "⏰ ঋণ পরিশোধের সময় হয়েছে", "${loan.name} ঋণের পরিশোধের তারিখ: ${loan.dueDate}")
                    } else if (diffDays < 0) {
                        sendNotification(context, "🚨 ঋণ ওভারডিউ!", "${loan.name} ঋণের পরিশোধের তারিখ পার হয়ে গেছে (${loan.dueDate})")
                    }
                }
            } catch (_: Exception) {}
        }

        // Check Lendings
        lendings.filter { it.dueDate != null }.forEach { lending ->
            try {
                val due = Calendar.getInstance().apply { time = format.parse(lending.dueDate!!)!! }
                val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
                val remaining = lending.amount - returned

                if (remaining > 0) {
                    val diffDays = (due.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)
                    if (diffDays in 0..2) {
                        sendNotification(context, "⏳ টাকা আদায়ের সময় হয়েছে", "${lending.person}-এর কাছ থেকে টাকা আদায়ের তারিখ: ${lending.dueDate}")
                    } else if (diffDays < 0) {
                        sendNotification(context, "⚠️ পাওনা টাকা ওভারডিউ!", "${lending.person}-এর কাছ থেকে টাকা আদায়ের তারিখ পার হয়ে গেছে (${lending.dueDate})")
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        if (notifications.any { it.title == title && it.message == message }) return
        val newNotification = NotificationItem(title = title, message = message, type = "loan")
        notifications = notifications + newNotification
        NotificationStorage.save(context, notifications, currentUserId)
    }

    private fun setupFirestoreListeners(userId: String) {
        cloudLoading = true

        // =====================================================
        // TRANSACTIONS
        // =====================================================

        val transReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("transactions")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Cloud data load হয়নি: ${error.message}"
                        cloudLoading = false
                        return@addSnapshotListener
                    }

                    val cloudTransactions =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                firestoreDocumentToTransaction(it)
                            }
                            ?: emptyList()

                    val deletedIds =
                        prefs.getStringSet(
                            "deleted_transaction_ids",
                            emptySet()
                        )
                            ?.mapNotNull {
                                it.toLongOrNull()
                            }
                            ?.toSet()
                            ?: emptySet()

                    val activeCloudTransactions =
                        cloudTransactions.filter {
                            it.id !in deletedIds
                        }

                    personalTransactions =
                        activeCloudTransactions

                    saveTransactions(
                        prefs,
                        activeCloudTransactions
                    )

                    cloudLoading = false
                    cloudError = ""
                }

        registrations.add(transReg)


        // =====================================================
        // LOANS
        // =====================================================

        val loansReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("loans")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Loan Cloud data load হয়নি: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val cloudLoans =
                        snapshot.documents.mapNotNull { doc ->

                            try {

                                val borrowingArray =
                                    doc.get(
                                        "borrowings"
                                    ) as? List<Map<String, Any>>

                                val borrowings =
                                    borrowingArray
                                        ?.map { b ->

                                            LoanBorrowing(
                                                id =
                                                    (
                                                            b["id"]
                                                                    as? Number
                                                            )
                                                        ?.toLong()
                                                        ?: 0L,

                                                loanId =
                                                    (
                                                            b["loanId"]
                                                                    as? Number
                                                            )
                                                        ?.toLong()
                                                        ?: 0L,

                                                amount =
                                                    (
                                                            b["amount"]
                                                                    as? Number
                                                            )
                                                        ?.toDouble()
                                                        ?: 0.0,

                                                date =
                                                    b["date"]
                                                            as? String
                                                        ?: "",

                                                note =
                                                    b["note"]
                                                            as? String
                                                        ?: ""
                                            )
                                        }
                                        ?: emptyList()

                                LoanAccount(
                                    id =
                                        doc.getLong("id")
                                            ?: 0L,

                                    name =
                                        doc.getString("name")
                                            ?: "",

                                    sourceType =
                                        doc.getString(
                                            "sourceType"
                                        )
                                            ?: "bank",

                                    principal =
                                        doc.getDouble(
                                            "principal"
                                        )
                                            ?: 0.0,

                                    monthlyInstallment =
                                        doc.getDouble(
                                            "monthlyInstallment"
                                        )
                                            ?: 0.0,

                                    startDate =
                                        doc.getString(
                                            "startDate"
                                        )
                                            ?: "",

                                    note =
                                        doc.getString("note")
                                            ?: "",

                                    lastEditedDate =
                                        doc.getString(
                                            "lastEditedDate"
                                        )
                                            ?: "",

                                    editHistory =
                                        doc.get(
                                            "editHistory"
                                        ) as? List<String>
                                            ?: emptyList(),

                                    borrowings =
                                        borrowings,

                                    // NEW:
                                    // Cloud থেকে dueDate restore হবে
                                    dueDate =
                                        doc.getString(
                                            "dueDate"
                                        )
                                            ?.ifBlank {
                                                null
                                            }
                                )

                            } catch (_: Exception) {
                                null
                            }
                        }

                    // IMPORTANT:
                    // Cloud empty হলে local data clear হবে।
                    loans = cloudLoans
                    saveLoans(
                        prefs,
                        cloudLoans
                    )
                }

        registrations.add(loansReg)


        // =====================================================
        // LOAN PAYMENTS
        // =====================================================

        val paymentsReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("loanPayments")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Loan payment Cloud data load হয়নি: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val cloudPayments =
                        snapshot.documents.mapNotNull { doc ->

                            try {

                                LoanPayment(
                                    id =
                                        doc.getLong("id")
                                            ?: 0L,

                                    loanId =
                                        doc.getLong(
                                            "loanId"
                                        )
                                            ?: 0L,

                                    amount =
                                        doc.getDouble(
                                            "amount"
                                        )
                                            ?: 0.0,

                                    date =
                                        doc.getString("date")
                                            ?: "",

                                    note =
                                        doc.getString("note")
                                            ?: "",

                                    // NEW:
                                    // Home / Personal source
                                    fundSource =
                                        doc.getString(
                                            "fundSource"
                                        )
                                            ?.ifBlank {
                                                "personal"
                                            }
                                            ?: "personal",

                                    sourceTransactionId =
                                        doc.getLong(
                                            "sourceTransactionId"
                                        )
                                )

                            } catch (_: Exception) {
                                null
                            }
                        }

                    // IMPORTANT:
                    // Cloud empty হলে local data clear হবে।
                    loanPayments =
                        cloudPayments

                    saveLoanPayments(
                        prefs,
                        cloudPayments
                    )
                }

        registrations.add(paymentsReg)


        // =====================================================
        // LOAN INTEREST TERMS
        // =====================================================

        val interestReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("loanInterestTerms")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Loan interest Cloud data load হয়নি: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val cloudInterestTerms =
                        snapshot.documents.mapNotNull { doc ->

                            try {

                                LoanInterestTerms(
                                    loanId =
                                        doc.getLong(
                                            "loanId"
                                        )
                                            ?: 0L,

                                    interestRate =
                                        doc.getDouble(
                                            "interestRate"
                                        )
                                            ?: 0.0,

                                    totalInterest =
                                        doc.getDouble(
                                            "totalInterest"
                                        )
                                            ?: 0.0,

                                    interestType =
                                        doc.getString(
                                            "interestType"
                                        )
                                            ?: "fixed"
                                )

                            } catch (_: Exception) {
                                null
                            }
                        }

                    // IMPORTANT:
                    // Cloud empty হলে local data clear হবে।
                    loanInterestTerms =
                        cloudInterestTerms

                    saveLoanInterestTerms(
                        prefs,
                        cloudInterestTerms
                    )
                }

        registrations.add(interestReg)


        // =====================================================
        // LENDINGS
        // =====================================================

        val lendingsReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("lendings")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Lending Cloud data load হয়নি: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val cloudLendings =
                        snapshot.documents.mapNotNull { doc ->

                            try {

                                LendingAccount(
                                    id =
                                        doc.getLong("id")
                                            ?: 0L,

                                    person =
                                        doc.getString(
                                            "person"
                                        )
                                            ?: "",

                                    amount =
                                        doc.getDouble(
                                            "amount"
                                        )
                                            ?: 0.0,

                                    date =
                                        doc.getString("date")
                                            ?: "",

                                    note =
                                        doc.getString("note")
                                            ?: "",

                                    // NEW:
                                    dueDate =
                                        doc.getString(
                                            "dueDate"
                                        )
                                            ?.ifBlank {
                                                null
                                            },

                                    // NEW:
                                    fundSource =
                                        doc.getString(
                                            "fundSource"
                                        )
                                            ?.ifBlank {
                                                "personal"
                                            }
                                            ?: "personal"
                                )

                            } catch (_: Exception) {
                                null
                            }
                        }

                    // IMPORTANT:
                    // Cloud empty হলে local data clear হবে।
                    lendings =
                        cloudLendings

                    saveLendings(
                        prefs,
                        cloudLendings
                    )
                }

        registrations.add(lendingsReg)


        // =====================================================
        // LENDING RETURNS
        // =====================================================

        val returnsReg =
            firestore
                .collection("users")
                .document(userId)
                .collection("lendingReturns")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        cloudError =
                            "Lending return Cloud data load হয়নি: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val cloudReturns =
                        snapshot.documents.mapNotNull { doc ->

                            try {

                                LendingReturn(
                                    id =
                                        doc.getLong("id")
                                            ?: 0L,

                                    lendingId =
                                        doc.getLong(
                                            "lendingId"
                                        )
                                            ?: 0L,

                                    amount =
                                        doc.getDouble(
                                            "amount"
                                        )
                                            ?: 0.0,

                                    date =
                                        doc.getString("date")
                                            ?: "",

                                    note =
                                        doc.getString("note")
                                            ?: "",

                                    // NEW:
                                    fundSource =
                                        doc.getString(
                                            "fundSource"
                                        )
                                            ?.ifBlank {
                                                "personal"
                                            }
                                            ?: "personal"
                                )

                            } catch (_: Exception) {
                                null
                            }
                        }

                    // IMPORTANT:
                    // Cloud empty হলে local data clear হবে।
                    lendingReturns =
                        cloudReturns

                    saveLendingReturns(
                        prefs,
                        cloudReturns
                    )
                }

        registrations.add(returnsReg)
    }

    // --------------------------------------------------
    // ফ্যামিলি / Shared Household
    // --------------------------------------------------

    fun createHousehold(context: Context, name: String, onResult: (Boolean, String) -> Unit) {
        if (currentUserId == "guest") {
            onResult(false, "Family feature-এর জন্য লগইন দরকার")
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        val member = HouseholdMember(
            uid = currentUserId,
            name = user?.displayName ?: "আমি",
            email = user?.email ?: "",
            photoUrl = user?.photoUrl?.toString() ?: ""
        )
        val householdId = firestore.collection("households").document().id
        val newHousehold = Household(
            id = householdId,
            name = name.trim(),
            code = HouseholdRepository.generateCode(),
            createdBy = currentUserId,
            members = listOf(member)
        )
        householdBusy = true
        HouseholdRepository.createHousehold(firestore, newHousehold, {
            household = newHousehold
            HouseholdStorage.saveHouseholdId(context, currentUserId, householdId)
            HouseholdStorage.saveHouseholdCache(context, currentUserId, newHousehold)
            attachHouseholdListeners(context, householdId)
            markAndUploadHomeTransactions(householdId)
            householdBusy = false
            onResult(true, "✅ \"${newHousehold.name}\" তৈরি হয়েছে")
        }, { e ->
            householdBusy = false
            onResult(false, e)
        })
    }

    fun joinHousehold(context: Context, code: String, onResult: (Boolean, String) -> Unit) {
        if (currentUserId == "guest") {
            onResult(false, "Family feature-এর জন্য লগইন দরকার")
            return
        }
        householdBusy = true
        HouseholdRepository.findHouseholdByCode(firestore, code) { found ->
            if (found == null) {
                householdBusy = false
                onResult(false, "❌ এই Code-এ কোনো Household পাওয়া যায়নি")
                return@findHouseholdByCode
            }
            val user = FirebaseAuth.getInstance().currentUser
            val member = HouseholdMember(
                uid = currentUserId,
                name = user?.displayName ?: "",
                email = user?.email ?: "",
                photoUrl = user?.photoUrl?.toString() ?: ""
            )
            val alreadyMember = found.members.any { it.uid == currentUserId }
            val proceed: () -> Unit = {
                household = found
                HouseholdStorage.saveHouseholdId(context, currentUserId, found.id)
                HouseholdStorage.saveHouseholdCache(context, currentUserId, found)
                attachHouseholdListeners(context, found.id)
                markAndUploadHomeTransactions(found.id)
                householdBusy = false
                onResult(true, "✅ \"${found.name}\"-এ join করেছেন")
            }
            if (alreadyMember) {
                proceed()
            } else {
                HouseholdRepository.addMember(firestore, found.id, member, proceed, { e ->
                    householdBusy = false
                    onResult(false, e)
                })
            }
        }
    }

    fun leaveHousehold(context: Context, onResult: (Boolean, String) -> Unit) {
        val current = household ?: run {
            onResult(false, "কোনো Household নেই")
            return
        }
        householdBusy = true
        HouseholdRepository.removeMember(firestore, current.id, currentUserId, {
            detachHouseholdListeners()
            household = null
            sharedHomeTransactions = emptyList()

            // পরিবারের shared mark করা home লেনদেন লোকাল থেকেও সরিয়ে ফেলা হয়
            personalTransactions = personalTransactions.filter {
                !(
                        (
                                it.type == "home" ||
                                        it.type == "home_expense" ||
                                        it.type == "home_adjustment"
                                ) && it.addedByUid != null
                        )
            }
            saveTransactions(prefs, personalTransactions)
            saveAutoBackup(context)
            HouseholdStorage.clearHousehold(context, currentUserId)

            householdBusy = false
            onResult(true, "পরিবার ছেড়ে দিয়েছেন")
        }, { e ->
            householdBusy = false
            onResult(false, e)
        })
    }

    private fun attachHouseholdListeners(context: Context, householdId: String) {
        householdRegistration?.remove()
        sharedHomeRegistration?.remove()

        householdRegistration = firestore.collection("households").document(householdId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val h = HouseholdRepository.docToHousehold(doc)
                    household = h
                    if (h != null) {
                        HouseholdStorage.saveHouseholdCache(context, currentUserId, h)
                    }
                }
            }

        sharedHomeRegistration = firestore.collection("households").document(householdId)
            .collection("homeTransactions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val deletedIds = prefs.getStringSet("deleted_transaction_ids", emptySet())?.toSet() ?: emptySet()
                sharedHomeTransactions = snapshot.documents
                    .mapNotNull { HouseholdRepository.homeDocToTransaction(it) }
                    .filter { it.id.toString() !in deletedIds }
            }
    }

    private fun detachHouseholdListeners() {
        householdRegistration?.remove()
        householdRegistration = null
        sharedHomeRegistration?.remove()
        sharedHomeRegistration = null
    }

    /** Join/Create করার পর নিজের পুরনো home লেনদেনগুলো shared collection-এ পাঠানো হয়। */
    private fun markAndUploadHomeTransactions(householdId: String) {
        val myName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        val homeTxns = personalTransactions.filter {
            it.type == "home" ||
                    it.type == "home_expense" ||
                    it.type == "home_adjustment"
        }
        if (homeTxns.isEmpty()) return

        personalTransactions = personalTransactions.map { t ->
            if (
                t.type == "home" ||
                t.type == "home_expense" ||
                t.type == "home_adjustment"
            ) {
                t.copy(addedByUid = currentUserId, addedByName = myName ?: "")
            } else t
        }
        saveTransactions(prefs, personalTransactions)

        homeTxns.forEach { t ->
            HouseholdRepository.saveSharedHomeTransaction(
                firestore, householdId,
                t.copy(addedByUid = currentUserId, addedByName = myName ?: "")
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        detachHouseholdListeners()
        registrations.forEach { it.remove() }
        registrations.clear()
    }

    fun refreshRate() {
        viewModelScope.launch {
            rateLoading = true
            rateError = ""
            val result = getLiveRates()
            if (result != null) {
                usdToBdt = result.first
                usdToMvr = result.second
                prefs.edit()
                    .putFloat("usd_to_bdt", usdToBdt.toFloat())
                    .putFloat("usd_to_mvr", usdToMvr.toFloat())
                    .apply()
            } else {
                rateError = "Internet connection পাওয়া যায়নি।"
            }
            rateLoading = false
        }
    }

    fun saveTransaction(
        context: Context,
        amount: Double,
        currency: String,
        category: String,
        reason: String,
        date: String,
        type: String,
        walletId: String,
        receiptImage: String? = null,
        transactionId: Long? = null,
        onComplete: () -> Unit
    ) {
        val transaction = Transaction(
            id = transactionId ?: System.currentTimeMillis(),
            type = type,
            amount = amount,
            currency = currency,
            category = category,
            reason = reason,
            date = date,
            receiptImage = receiptImage,
            walletId = walletId
        )

        // পরিবারের shared home লেনদেন হলে addedBy mark করা হয়
        val isSharedHome = household != null &&
                (type == "home" || type == "home_expense" || type == "home_adjustment")
        val marked = if (isSharedHome) transaction.copy(
            addedByUid = currentUserId,
            addedByName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        ) else transaction

        personalTransactions = (personalTransactions + marked).distinctBy { it.id }
        saveTransactions(prefs, personalTransactions)
        val backupCreated = saveAutoBackup(context)
        LocalBackupReminderManager.showAfterEntry(context, currentUserId, backupCreated)

        // নতুন লেনদেন সফলভাবে সেভ হলে sound + মৃদু vibration feedback
        SoundHapticHelper.playTransactionSavedFeedback(context)

        if (currentUserId != "guest") {
            saveTransactionToFirestore(firestore, currentUserId, marked, {
                makeText(context, "☁️ Cloud-এ save হয়েছে", Toast.LENGTH_SHORT).show()
            }, onError = { message ->
                WarningPopupManager.show(
                    title = "Cloud Save Failed",
                    message = "Cloud-এ হিসাবটি save করা যায়নি।\n\n$message"
                )
            })
        }

        // পরিবারের shared collection-এ পাঠানো হয় — সবাই সাথে সাথে দেখবে
        if (isSharedHome) {
            household?.let {
                HouseholdRepository.saveSharedHomeTransaction(firestore, it.id, marked)
            }
        }

        checkBudget(context, marked, type, category)
        onComplete()
    }

    private fun checkBudget(context: Context, transaction: Transaction, type: String, category: String) {
        if (type == "expense") {
            val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
            val budget = categoryBudgets.firstOrNull { it.month == currentMonth && it.category == category }
            if (budget != null && budget.limit > 0) {
                val spent = transactions.filter { it.type == "expense" && it.category == category && it.date.endsWith(currentMonth) }
                    .sumOf { convertToBdt(it.amount, it.currency) }
                val percentage = spent / budget.limit
                if (spent >= budget.limit) {
                    WarningPopupManager.show(
                        title = "$category Budget Limit Crossed",
                        message = "আপনার $category Budget Limit অতিক্রম করেছে।\n\n" +
                                "খরচ: ৳${formatMoney(spent)}\n" +
                                "Limit: ৳${formatMoney(budget.limit)}"
                    )
                    val newNotification = NotificationItem(
                        title = "🚨 $category Budget Limit Crossed",
                        message = "Budget limit cross হয়েছে।\nখরচ: ৳${formatMoney(spent)} / Limit: ৳${formatMoney(budget.limit)}"
                    )
                    notifications = notifications + newNotification
                    NotificationStorage.save(context, notifications, currentUserId)
                } else if (percentage >= 0.80) {
                    WarningPopupManager.show(
                        title = "⚠️ $category Budget Warning",
                        message = "Budget-এর 80% ব্যবহার হয়েছে।\n\n" +
                                "খরচ: ৳${formatMoney(spent)}\n" +
                                "Limit: ৳${formatMoney(budget.limit)}"
                    )

                    val newNotification = NotificationItem(
                        title = "⚠️ $category Budget Warning",
                        message = "Budget-এর 80% ব্যবহার হয়েছে।\n" +
                                "খরচ: ৳${formatMoney(spent)} / Limit: ৳${formatMoney(budget.limit)}"
                    )

                    notifications = notifications + newNotification
                    NotificationStorage.save(context, notifications, currentUserId)
                }
            }
        }
    }

    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val inPersonal = personalTransactions.any { it.id == updatedTransaction.id }
        val inShared = sharedHomeTransactions.any { it.id == updatedTransaction.id }

        if (inPersonal) {
            personalTransactions = personalTransactions.map { if (it.id == updatedTransaction.id) updatedTransaction else it }
            saveTransactions(prefs, personalTransactions)
        }
        if (inShared) {
            sharedHomeTransactions = sharedHomeTransactions.map { if (it.id == updatedTransaction.id) updatedTransaction else it }
            household?.let {
                HouseholdRepository.saveSharedHomeTransaction(firestore, it.id, updatedTransaction)
            }
        }
        val backupCreated = saveAutoBackup(context)
        LocalBackupReminderManager.showAfterEntry(context, currentUserId, backupCreated)

        // লেনদেন সফলভাবে এডিট হলে হালকা feedback
        SoundHapticHelper.playTransactionUpdatedFeedback(context)

        if (currentUserId != "guest") {
            saveTransactionToFirestore(firestore, currentUserId, updatedTransaction, {
                makeText(context, "✏️ Cloud data update হয়েছে", Toast.LENGTH_SHORT).show()
            }, { message ->
                WarningPopupManager.show(
                    title = "Cloud Update Failed",
                    message = "Cloud data update করা যায়নি।\n\n$message"
                )
            })
        }
    }

    fun deleteTransaction(context: Context, transaction: Transaction) {
        val deletedIds = prefs.getStringSet("deleted_transaction_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        deletedIds.add(transaction.id.toString())
        prefs.edit().putStringSet("deleted_transaction_ids", deletedIds).apply()

        personalTransactions = personalTransactions.filter { it.id != transaction.id }
        sharedHomeTransactions = sharedHomeTransactions.filter { it.id != transaction.id }
        saveTransactions(prefs, personalTransactions)
        saveAutoBackup(context)

        // লেনদেন ডিলিট হলে কড়া feedback
        SoundHapticHelper.playTransactionDeletedFeedback(context)

        // পরিবারের shared collection থেকেও মুছে ফেলা হয়
        household?.let {
            HouseholdRepository.deleteSharedHomeTransaction(firestore, it.id, transaction.id)
        }

        if (currentUserId != "guest") {
            deleteTransactionFromFirestore(firestore, currentUserId, transaction.id, {
                makeText(context, "🗑️ Cloud data delete হয়েছে", Toast.LENGTH_SHORT).show()
            }, { message ->
                WarningPopupManager.show(
                    title = "Cloud Delete Failed",
                    message = "Cloud data delete করা যায়নি।\n\n$message"
                )
            })
        }
    }

    fun saveCategoryBudgetAction(context: Context, budget: CategoryBudget) {
        categoryBudgets = categoryBudgets.filterNot { it.month == budget.month && it.category == budget.category } + budget
        saveCategoryBudgets(prefs, categoryBudgets)
        makeText(context, "✅ ${budget.category} Budget Save হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addSavingsGoal(
        context: Context,
        name: String,
        targetAmount: Double,
        targetDate: String,
        frequency: String
    ) {
        val goal = SavingsGoal(
            id = System.currentTimeMillis(),
            name = name.trim(),
            targetAmount = targetAmount,
            savedAmount = 0.0,
            targetDate = targetDate,
            frequency = frequency
        )
        savingsGoals = savingsGoals + goal
        saveSavingsGoals(prefs, savingsGoals)
        saveAutoBackup(context)
    }

    fun addSavingsContribution(context: Context, goalId: Long, amount: Double) {
        savingsGoals = savingsGoals.map { goal ->
            if (goal.id == goalId) goal.copy(savedAmount = goal.savedAmount + amount) else goal
        }
        saveSavingsGoals(prefs, savingsGoals)
        saveAutoBackup(context)
    }

    fun deleteSavingsGoal(context: Context, goalId: Long) {
        savingsGoals = savingsGoals.filterNot { it.id == goalId }
        saveSavingsGoals(prefs, savingsGoals)
        saveAutoBackup(context)
    }

    fun addLoan(context: Context, name: String, type: String, amount: Double, monthly: Double, date: String, note: String, dueDate: String? = null) {
        val newLoan = LoanAccount(System.currentTimeMillis(), name, type, amount, monthly, date, note, dueDate = dueDate)
        loans = loans + newLoan
        persistLoanData(context)
        makeText(context, "✅ নতুন ঋণ যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun updateLoan(context: Context, loan: LoanAccount, name: String, type: String, amount: Double, monthly: Double, date: String, note: String, dueDate: String? = null) {
        if (amount <= 0.0) {
            WarningPopupManager.show(
                title = "ঋণের পরিমাণ সঠিক নয়",
                message = "ঋণের পরিমাণ ০-এর বেশি দিন।"
            )
            return
        }

        val existingInterest =
            loanInterestTerms
                .firstOrNull { it.loanId == loan.id }
                ?.totalInterest
                ?: 0.0

        val alreadyPaid =
            loanPayments
                .filter { it.loanId == loan.id }
                .sumOf { it.amount }

        val newTotalPayable =
            amount + existingInterest

        if (newTotalPayable + 0.000001 < alreadyPaid) {
            WarningPopupManager.show(
                title = "ঋণের পরিমাণ কমানো যাবে না",
                message =
                    "এই Loan-এ ইতিমধ্যে ৳${formatMoney(alreadyPaid)} পরিশোধ হয়েছে।\n\n" +
                            "বর্তমান Interest সহ মোট payable ৳${formatMoney(newTotalPayable)}।\n\n" +
                            "তাই পরিশোধিত টাকার চেয়ে কম Loan amount দেওয়া যাবে না।"
            )
            return
        }

        val updated = loan.copy(
            name = name,
            sourceType = type,
            principal = amount,
            monthlyInstallment = monthly,
            startDate = date,
            note = note,
            lastEditedDate =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date()),
            editHistory =
                loan.editHistory +
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date()),
            dueDate = dueDate
        )

        loans =
            loans.map {
                if (it.id == loan.id) updated else it
            }

        persistLoanData(context)

        makeText(
            context,
            "✅ ঋণের তথ্য আপডেট হয়েছে",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Note-based compatibility helper.
     * পুরোনো LoanPayment data-তে আলাদা source field নেই, তাই existing note দেখে
     * Home-funded payment চিহ্নিত করা হচ্ছে।
     */
    private fun isHomeLoanPayment(payment: LoanPayment): Boolean {
        return when (payment.fundSource.lowercase(Locale.getDefault())) {
            "home" -> true
            "personal" -> false
            else -> {
                val note = payment.note.lowercase(Locale.getDefault())
                note.contains("বাড়িতে পাঠানো") ||
                        note.contains("বাড়িতে পাঠানো") ||
                        note.contains("home loan payment") ||
                        note.contains("home-funded loan")
            }
        }
    }

    /**
     * Home → Loan payment-এর জন্য available Home balance যাচাই।
     * Extra ছাড়া shortage থাকলে payment save হবে না।
     */
    fun getHomeLoanShortage(amount: Double): Double {
        if (amount <= 0.0) return 0.0
        return (amount - homeBalance).coerceAtLeast(0.0)
    }

    /**
     * Home-only Extra/Adjustment history-তে রাখা হয়।
     * এটি Personal balance বা Personal expense নয়।
     */
    private fun addHomeAdjustment(
        context: Context,
        amount: Double,
        date: String,
        reason: String
    ) {
        if (amount <= 0.0) return

        val adjustment = Transaction(
            id = System.currentTimeMillis(),
            type = "home_adjustment",
            amount = amount,
            currency = "BDT",
            category = "Home Adjustment",
            reason = reason,
            date = date,
            walletId = "default_cash"
        )

        personalTransactions = (personalTransactions + adjustment).distinctBy { it.id }
        saveTransactions(prefs, personalTransactions)
        saveAutoBackup(context)

        if (currentUserId != "guest") {
            saveTransactionToFirestore(
                firestore,
                currentUserId,
                adjustment,
                {},
                {}
            )
        }

        // Household থাকলে adjustment-টিও Home ledger-এ shared করা হয়।
        household?.let {
            HouseholdRepository.saveSharedHomeTransaction(
                firestore,
                it.id,
                adjustment
            )
        }
    }

    /**
     * Home → Loan payment save করার নতুন নিরাপদ path।
     *
     * extraHomeAmount:
     * - shortage না থাকলে 0 রাখা হবে; Extra UI দেখানোর প্রয়োজন নেই।
     * - shortage থাকলে অন্তত shortage পরিমাণ হতে হবে।
     * - Extra shortage-এর চেয়ে বেশি হলেও allowed; অতিরিক্তটাও Home adjustment হিসেবে থাকবে।
     *
     * return:
     * - true = payment save হয়েছে
     * - false = validation fail, কিছু save হয়নি
     */
    fun addHomeLoanPayment(
        context: Context,
        loan: LoanAccount,
        amount: Double,
        date: String,
        note: String,
        extraHomeAmount: Double,
        sourceTransactionId: Long? = null
    ): Boolean {
        if (amount <= 0.0) {
            WarningPopupManager.show(
                title = "পরিশোধের পরিমাণ সঠিক নয়",
                message = "Loan payment-এর পরিমাণ ০-এর বেশি দিন।"
            )
            return false
        }

        val interest =
            loanInterestTerms
                .firstOrNull { it.loanId == loan.id }
                ?.totalInterest
                ?: 0.0

        val alreadyPaid =
            loanPayments
                .filter { it.loanId == loan.id }
                .sumOf { it.amount }

        val totalPayable =
            loan.principal + interest

        val remaining =
            (totalPayable - alreadyPaid)
                .coerceAtLeast(0.0)

        if (amount > remaining + 0.000001) {
            WarningPopupManager.show(
                title = "Loan amount-এর বেশি",
                message = "এই Loan-এর বাকি পরিমাণ ৳${formatMoney(remaining)}।\n\n" +
                        "আপনি ৳${formatMoney(amount)} দিতে চাচ্ছেন।"
            )
            return false
        }

        val shortage = getHomeLoanShortage(amount)
        val extra = extraHomeAmount.coerceAtLeast(0.0)

        if (shortage > 0.000001 && extra + 0.000001 < shortage) {
            WarningPopupManager.show(
                title = "Home balance যথেষ্ট নয়",
                message = "এই payment-এর জন্য আরও ৳${formatMoney(shortage)} দরকার।\n\n" +
                        "Extra/Adjustment কমপক্ষে ৳${formatMoney(shortage)} হতে হবে।"
            )
            return false
        }

        // Extra থাকলে আগে Home adjustment হিসেবে history-তে যোগ হবে।
        if (extra > 0.000001) {
            addHomeAdjustment(
                context = context,
                amount = extra,
                date = date,
                reason = "Home Loan Payment Extra/Adjustment"
            )
        }

        val payment = LoanPayment(
            id = System.currentTimeMillis(),
            loanId = loan.id,
            amount = amount,
            date = date,
            note = if (
                note.isBlank()
            ) {
                "বাড়িতে পাঠানো টাকা থেকে Loan payment"
            } else {
                note
            },
            fundSource = "home",
            sourceTransactionId = sourceTransactionId
        )

        loanPayments = loanPayments + payment
        persistLoanData(context)

        makeText(
            context,
            "✅ Home থেকে Loan payment সেভ হয়েছে",
            Toast.LENGTH_SHORT
        ).show()

        return true
    }

    fun addLoanPayment(context: Context, loan: LoanAccount, amount: Double, date: String, note: String, isHome: Boolean) {
        if (isHome) {
            // পুরোনো callback compatibility: Extra দেওয়া না হলে shortage-এ save বন্ধ থাকবে।
            addHomeLoanPayment(
                context = context,
                loan = loan,
                amount = amount,
                date = date,
                note = note,
                extraHomeAmount = 0.0
            )
            return
        }

        val interest =
            loanInterestTerms
                .firstOrNull { it.loanId == loan.id }
                ?.totalInterest
                ?: 0.0

        val alreadyPaid =
            loanPayments
                .filter { it.loanId == loan.id }
                .sumOf { it.amount }

        val totalPayable =
            loan.principal + interest

        val remaining =
            (totalPayable - alreadyPaid)
                .coerceAtLeast(0.0)

        if (amount <= 0.0 || amount > remaining + 0.000001) {
            WarningPopupManager.show(
                title = "Loan payment সঠিক নয়",
                message = "এই Loan-এর সর্বোচ্চ বাকি পরিমাণ ৳${formatMoney(remaining)}।"
            )
            return
        }

        val payment = LoanPayment(
            id = System.currentTimeMillis(),
            loanId = loan.id,
            amount = amount,
            date = date,
            note = note,
            fundSource = "personal"
        )
        loanPayments = loanPayments + payment
        persistLoanData(context)
        makeText(context, "✅ পরিশোধের তথ্য সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }


    fun updateLoanPayment(
        context: Context,
        payment: LoanPayment,
        amount: Double,
        date: String,
        note: String
    ): Boolean {

        if (amount <= 0.0) {
            WarningPopupManager.show(
                title = "পরিশোধের পরিমাণ সঠিক নয়",
                message = "Loan payment-এর পরিমাণ ০-এর বেশি দিন।"
            )
            return false
        }

        val loan = loans.firstOrNull {
            it.id == payment.loanId
        }

        if (loan == null) {
            WarningPopupManager.show(
                title = "Loan পাওয়া যায়নি",
                message = "এই payment-এর সাথে যুক্ত Loan খুঁজে পাওয়া যায়নি।"
            )
            return false
        }

        val interest =
            loanInterestTerms
                .firstOrNull { it.loanId == loan.id }
                ?.totalInterest
                ?: 0.0

        val totalPayable =
            loan.principal + interest

        val paidWithoutCurrent =
            loanPayments
                .filter {
                    it.loanId == payment.loanId &&
                            it.id != payment.id
                }
                .sumOf { it.amount }

        val remainingForThisPayment =
            (totalPayable - paidWithoutCurrent)
                .coerceAtLeast(0.0)

        if (amount > remainingForThisPayment + 0.000001) {
            WarningPopupManager.show(
                title = "Loan amount-এর বেশি",
                message =
                    "এই Loan-এ সর্বোচ্চ ৳${formatMoney(remainingForThisPayment)} দেওয়া যাবে।"
            )
            return false
        }

        val updatedPayment =
            payment.copy(
                amount = amount,
                date = date,
                note = note
            )

        loanPayments =
            loanPayments.map {
                if (it.id == payment.id) {
                    updatedPayment
                } else {
                    it
                }
            }

        persistLoanData(context)

        if (currentUserId != "guest") {
            firestore
                .collection("users")
                .document(currentUserId)
                .collection("loanPayments")
                .document(payment.id.toString())
                .set(
                    mapOf(
                        "id" to updatedPayment.id,
                        "loanId" to updatedPayment.loanId,
                        "amount" to updatedPayment.amount,
                        "date" to updatedPayment.date,
                        "note" to updatedPayment.note,
                        "fundSource" to updatedPayment.fundSource,
                        "sourceTransactionId" to updatedPayment.sourceTransactionId
                    )
                )
        }

        SoundHapticHelper.playTransactionUpdatedFeedback(
            context
        )

        makeText(
            context,
            "✏️ Loan payment আপডেট হয়েছে",
            Toast.LENGTH_SHORT
        ).show()

        return true
    }

    fun deleteLoanPayment(
        context: Context,
        payment: LoanPayment
    ): Boolean {

        if (
            loanPayments.none {
                it.id == payment.id
            }
        ) {
            WarningPopupManager.show(
                title = "Payment পাওয়া যায়নি",
                message = "এই Loan payment আর পাওয়া যাচ্ছে না।"
            )
            return false
        }

        loanPayments =
            loanPayments.filter {
                it.id != payment.id
            }

        persistLoanData(context)

        if (currentUserId != "guest") {
            firestore
                .collection("users")
                .document(currentUserId)
                .collection("loanPayments")
                .document(payment.id.toString())
                .delete()
        }

        SoundHapticHelper.playTransactionDeletedFeedback(
            context
        )

        makeText(
            context,
            "🗑️ Loan payment মুছে ফেলা হয়েছে",
            Toast.LENGTH_SHORT
        ).show()

        return true
    }

    fun addLending(context: Context, person: String, amount: Double, date: String, note: String, dueDate: String? = null) {
        val lending = LendingAccount(System.currentTimeMillis(), person, amount, date, note, dueDate = dueDate)
        lendings = lendings + lending
        persistLoanData(context)
        makeText(context, "✅ ধারের তথ্য সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addLendingReturn(context: Context, lending: LendingAccount, amount: Double, date: String, note: String) {
        if (amount <= 0.0) {
            WarningPopupManager.show(
                title = "ফেরতের পরিমাণ সঠিক নয়",
                message = "ধার ফেরতের পরিমাণ ০-এর বেশি দিন।"
            )
            return
        }

        val alreadyReturned =
            lendingReturns
                .filter { it.lendingId == lending.id }
                .sumOf { it.amount }

        val outstanding =
            (lending.amount - alreadyReturned)
                .coerceAtLeast(0.0)

        if (amount > outstanding + 0.000001) {
            WarningPopupManager.show(
                title = "ফেরতের পরিমাণ বেশি",
                message =
                    "এই ধার থেকে সর্বোচ্চ ৳${formatMoney(outstanding)} ফেরত নেওয়া যাবে।\n\n" +
                            "আপনি ৳${formatMoney(amount)} দিতে চাচ্ছেন।"
            )
            return
        }

        val ret =
            LendingReturn(
                System.currentTimeMillis(),
                lending.id,
                amount,
                date,
                note
            )

        lendingReturns =
            lendingReturns + ret

        persistLoanData(context)

        makeText(
            context,
            "✅ ধার ফেরতের তথ্য সেভ হয়েছে",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun updateBorrowing(context: Context, loan: LoanAccount, borrowing: LoanBorrowing, amount: Double, date: String, note: String) {
        val updatedBorrowing = borrowing.copy(amount = amount, date = date, note = note)
        val updatedLoan = loan.copy(borrowings = loan.borrowings.map { if (it.id == borrowing.id) updatedBorrowing else it })
        loans = loans.map { if (it.id == loan.id) updatedLoan else it }
        persistLoanData(context)
        makeText(context, "✅ ঋণের এন্ট্রি আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun deleteBorrowing(context: Context, loan: LoanAccount, borrowing: LoanBorrowing) {
        val updatedLoan = loan.copy(borrowings = loan.borrowings.filter { it.id != borrowing.id })
        loans = loans.map { if (it.id == loan.id) updatedLoan else it }
        persistLoanData(context)
        makeText(context, "🗑️ এন্ট্রিটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    private fun persistLoanData(context: Context) {
        saveLoans(prefs, loans)
        saveLoanPayments(prefs, loanPayments)
        saveLendings(prefs, lendings)
        saveLendingReturns(prefs, lendingReturns)
        saveLoanInterestTerms(prefs, loanInterestTerms)
        saveWallets(prefs, wallets)
        saveAutoBackup(context)

        if (currentUserId != "guest") {
            syncAllLoanAndLendingData(
                firestore = firestore,
                userId = currentUserId,
                loans = loans,
                loanPayments = loanPayments,
                lendings = lendings,
                lendingReturns = lendingReturns,
                loanInterestTerms = loanInterestTerms
            )
        }
    }

    fun addWallet(context: Context, name: String, type: String, initialBalance: Double, currency: String, color: Int) {
        val newWallet = Wallet(id = "wallet_${System.currentTimeMillis()}", name = name, type = type, initialBalance = initialBalance, currency = currency, color = color)
        wallets = wallets + newWallet
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
        makeText(context, "✅ নতুন অ্যাকাউন্ট যোগ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun updateWallet(context: Context, wallet: Wallet) {
        wallets = wallets.map { if (it.id == wallet.id) wallet else it }
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
    }

    fun deleteWallet(context: Context, walletId: String) {
        if (walletId == "default_cash") {
            WarningPopupManager.show(
                title = "অ্যাকাউন্ট মুছে ফেলা যাবে না",
                message = "ডিফল্ট অ্যাকাউন্টটি মুছে ফেলা সম্ভব নয়।"
            )
            return
        }
        wallets = wallets.filter { it.id != walletId }
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
        makeText(context, "🗑️ অ্যাকাউন্ট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun getWalletBalance(walletId: String): Double {
        val wallet = wallets.firstOrNull { it.id == walletId } ?: return 0.0
        val walletTransactions = transactions.filter { it.walletId == walletId }
        val income = walletTransactions.filter { it.type == "income" }.sumOf { convertToBdt(it.amount, it.currency) }
        val expense = walletTransactions.filter { it.type == "expense" }.sumOf { convertToBdt(it.amount, it.currency) }
        val home = walletTransactions.filter { it.type == "home" }.sumOf { convertToBdt(it.amount, it.currency) }

        // Simplified: assuming loans/lendings are from default wallet for now
        // If we want per-wallet loans, we'd need to update those models too.
        return wallet.initialBalance + income - expense - home
    }

    fun resetCurrentAccountData(context: Context, onComplete: () -> Unit) {
        personalTransactions = emptyList()
        sharedHomeTransactions = emptyList()
        loans = emptyList()
        loanPayments = emptyList()
        lendings = emptyList()
        lendingReturns = emptyList()
        categoryBudgets = emptyList()
        notifications = emptyList()

        prefs.edit().clear().apply()
        NotificationStorage.save(context, emptyList(), currentUserId)

        if (currentUserId != "guest") {
            // Delete from Firestore logic can be added here
        }
        onComplete()
    }

    fun updateBirthday(context: Context, newBirthday: Pair<Int, Int>?) {
        birthday = newBirthday
        if (newBirthday != null) {
            saveBirthday(prefs, newBirthday.first, newBirthday.second)
        } else {
            prefs.edit().remove("birthday_month").remove("birthday_day").apply()
        }
    }

    fun saveAutoBackup(context: Context): Boolean {
        return saveAutoBackup(
            context,
            currentUserId,
            transactions,
            usdToBdt,
            usdToMvr,
            loans,
            loanPayments,
            lendings,
            lendingReturns,
            wallets
        )
    }

    // --------------------------------------------------
    // বাজারের ফর্দ / Shopping List
    // --------------------------------------------------

    private fun persistShoppingList(context: Context) {
        ShoppingListStorage.save(context, currentUserId, shoppingItems)
    }

    private fun persistWishlist(context: Context) {
        WishlistStorage.save(context, currentUserId, wishlistItems)
    }

    fun addWishlistItem(context: Context, name: String, price: Double) {
        wishlistItems = wishlistItems + WishlistItem(name = name.trim(), price = price)
        persistWishlist(context)
    }

    fun deleteWishlistItem(context: Context, id: Long) {
        wishlistItems = wishlistItems.filterNot { it.id == id }
        persistWishlist(context)
    }

    fun notifyAffordableWishlistItems(context: Context) {
        val affordableItems = wishlistItems.filter { !it.notifiedAffordable && balance >= it.price }
        if (affordableItems.isEmpty()) return

        val notifiedIds = affordableItems
            .filter { WishlistNotificationManager.notifyAffordable(context, it) }
            .map { it.id }
        wishlistItems = wishlistItems.map { item ->
            if (item.id in notifiedIds) item.copy(notifiedAffordable = true) else item
        }
        persistWishlist(context)
    }

    fun addShoppingItem(context: Context, name: String, amount: Double, currency: String, category: String, note: String) {
        val item = ShoppingItem(
            name = name.trim(),
            amount = amount,
            currency = currency.ifBlank { "BDT" },
            category = category.ifBlank { "অন্যান্য" },
            note = note.trim()
        )
        shoppingItems = shoppingItems + item
        persistShoppingList(context)
    }

    fun updateShoppingItem(context: Context, updated: ShoppingItem) {
        shoppingItems = shoppingItems.map { if (it.id == updated.id) updated.copy(name = updated.name.trim(), currency = updated.currency.ifBlank { "BDT" }, category = updated.category.ifBlank { "অন্যান্য" }) else it }
        persistShoppingList(context)
    }

    fun toggleShoppingItem(context: Context, id: Long) {
        shoppingItems = shoppingItems.map { if (it.id == id) it.copy(checked = !it.checked) else it }
        persistShoppingList(context)
    }

    fun removeShoppingItem(context: Context, item: ShoppingItem) {
        shoppingItems = shoppingItems.filter { it.id != item.id }
        persistShoppingList(context)
    }

    fun clearAddedShoppingItems(context: Context) {
        shoppingItems = shoppingItems.filterNot { it.addedToExpense }
        persistShoppingList(context)
    }

    /** টিক করা আইটেমগুলো এক ক্লিকে খরচ (Expense) খাতায় এন্ট্রি করে। */
    fun convertCheckedToExpenses(context: Context, onComplete: (Int) -> Unit) {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val toConvert = shoppingItems.filter { it.checked && !it.addedToExpense }
        if (toConvert.isEmpty()) {
            onComplete(0)
            return
        }

        val newTransactions = toConvert.map { item ->
            Transaction(
                id = System.nanoTime() + (0..999L).random(),
                type = "expense",
                amount = item.amount,
                currency = item.currency.ifBlank { "BDT" },
                category = item.category.ifBlank { "অন্যান্য" },
                reason = item.name,
                date = today,
                walletId = "default_cash"
            )
        }

        personalTransactions = (personalTransactions + newTransactions).distinctBy { it.id }
        saveTransactions(prefs, personalTransactions)
        saveAutoBackup(context)

        shoppingItems = shoppingItems.map { if (it.checked && !it.addedToExpense) it.copy(addedToExpense = true) else it }
        persistShoppingList(context)

        // টিক করা আইটেম একসাথে খরচে গেলে একবার feedback
        SoundHapticHelper.playTransactionSavedFeedback(context)

        if (currentUserId != "guest") {
            newTransactions.forEach { txn ->
                saveTransactionToFirestore(firestore, currentUserId, txn, {}, {})
            }
        }

        onComplete(newTransactions.size)
    }

    fun convertToBdt(amount: Double, currency: String): Double {
        return when (currency) {
            "BDT" -> amount
            "USD" -> amount * usdToBdt
            "MVR" -> if (usdToMvr > 0) amount * (usdToBdt / usdToMvr) else 0.0
            else -> 0.0
        }
    }

    fun updateCloudData(cloudTransactions: List<Transaction>, cloudLoans: List<LoanAccount>, cloudPayments: List<LoanPayment>, cloudLendings: List<LendingAccount>, cloudReturns: List<LendingReturn>, cloudWallets: List<Wallet> = emptyList()) {
        personalTransactions = cloudTransactions
        loans = cloudLoans
        loanPayments = cloudPayments
        lendings = cloudLendings
        lendingReturns = cloudReturns
        if (cloudWallets.isNotEmpty()) wallets = cloudWallets

        saveTransactions(prefs, transactions)
        saveLoans(prefs, loans)
        saveLoanPayments(prefs, loanPayments)
        saveLendings(prefs, lendings)
        saveLendingReturns(prefs, lendingReturns)
        if (cloudWallets.isNotEmpty()) saveWallets(prefs, wallets)
    }

    fun getLoanStatement(loan: LoanAccount): String {
        val payments = loanPayments.filter { it.loanId == loan.id }
        val totalPaid = payments.sumOf { it.amount }
        val remaining = loan.principal - totalPaid

        val sb = StringBuilder()
        sb.append("📋 ঋণ স্টেটমেন্ট\n")
        sb.append("ব্যাংক/ব্যক্তি: ${loan.name}\n")
        sb.append("মোট ঋণ: ৳${formatMoney(loan.principal)}\n")
        sb.append("পরিশোধ হয়েছে: ৳${formatMoney(totalPaid)}\n")
        sb.append("বাকি আছে: ৳${formatMoney(remaining)}\n")
        if (loan.dueDate != null) sb.append("পরিশোধের তারিখ: ${loan.dueDate}\n")

        if (payments.isNotEmpty()) {
            sb.append("\nপরিশোধের ইতিহাস:\n")
            payments.forEach { p -> sb.append("- ${p.date}: ৳${formatMoney(p.amount)}\n") }
        }
        return sb.toString()
    }

    fun getLendingStatement(lending: LendingAccount): String {
        val returns = lendingReturns.filter { it.lendingId == lending.id }
        val totalReturned = returns.sumOf { it.amount }
        val remaining = lending.amount - totalReturned

        val sb = StringBuilder()
        sb.append("🤝 পাওনা স্টেটমেন্ট\n")
        sb.append("ব্যক্তি: ${lending.person}\n")
        sb.append("মোট ধার দেওয়া: ৳${formatMoney(lending.amount)}\n")
        sb.append("ফেরত পাওয়া: ৳${formatMoney(totalReturned)}\n")
        sb.append("বাকি পাওনা: ৳${formatMoney(remaining)}\n")
        if (lending.dueDate != null) sb.append("ফেরত পাওয়ার তারিখ: ${lending.dueDate}\n")

        if (returns.isNotEmpty()) {
            sb.append("\nফেরত পাওয়ার ইতিহাস:\n")
            returns.forEach { r -> sb.append("- ${r.date}: ৳${formatMoney(r.amount)}\n") }
        }
        return sb.toString()
    }

    fun exportTransactionsCsv(context: Context, uri: Uri) {
        ReportExporter.exportToCsv(context, uri, transactions)
    }

    fun exportPersonStatementPdf(
        context: Context,
        uri: Uri,
        personName: String,
        initialAmount: Double,
        history: List<Pair<String, Double>>,
        isLending: Boolean
    ) {
        ReportExporter.exportPersonStatement(context, uri, personName, initialAmount, history, isLending)
    }

    fun exportSearchResultsPdf(context: Context, uri: Uri, query: String) {
        val filtered = transactions.filter { it.reason.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
        val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
        val expense = filtered.filter { it.type != "income" }.sumOf { it.amount }
        ReportExporter.exportToPdf(context, uri, filtered, income, expense, income - expense)
    }

    fun generateSearchStatement(query: String): String {
        val filtered = transactions.filter { it.reason.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
        val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
        val expense = filtered.filter { it.type != "income" }.sumOf { it.amount }

        val sb = StringBuilder()
        sb.append("📊 সার্চ রেজাল্ট সামারি: $query\n")
        sb.append("মোট আয়: ৳${formatMoney(income)}\n")
        sb.append("মোট খরচ: ৳${formatMoney(expense)}\n")
        sb.append("নিট ব্যালেন্স: ৳${formatMoney(income - expense)}\n\n")

        if (filtered.isNotEmpty()) {
            sb.append("লেনদেনের ইতিহাস:\n")
            filtered.sortedByDescending { it.date }.forEach { t ->
                sb.append("- ${t.date}: ৳${formatMoney(t.amount)} (${t.reason.ifBlank { t.category }})\n")
            }
        }
        return sb.toString()
    }

    // Add more functions as needed (e.g., setLoanInterestTerms, etc.)
}
