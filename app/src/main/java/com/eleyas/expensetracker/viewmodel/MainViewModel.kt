package com.eleyas.expensetracker.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleyas.expensetracker.NotificationItem
import com.eleyas.expensetracker.NotificationStorage
import com.eleyas.expensetracker.*
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.repository.HouseholdRepository
import com.eleyas.expensetracker.repository.syncAllLoanAndLendingData
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
    var shoppingItems by mutableStateOf<List<ShoppingItem>>(emptyList())
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
    val totalLoanReceived by derivedStateOf { loans.sumOf { it.principal } }
    val totalLoanInterest by derivedStateOf { loans.sumOf { loan -> loanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0 } }
    val totalLoanPaid by derivedStateOf { loanPayments.sumOf { it.amount } }
    val totalLoanRemaining by derivedStateOf { (totalLoanReceived + totalLoanInterest - totalLoanPaid).coerceAtLeast(0.0) }
    val totalMoneyLent by derivedStateOf { lendings.sumOf { it.amount } }
    val totalMoneyReturned by derivedStateOf { lendingReturns.sumOf { it.amount } }
    val totalMoneyToReceive by derivedStateOf { (totalMoneyLent - totalMoneyReturned).coerceAtLeast(0.0) }
    val homeBalance by derivedStateOf { totalHome - totalHomeExpense }
    val balance by derivedStateOf { wallets.sumOf { it.initialBalance } + totalIncome + totalLoanReceived + totalMoneyReturned - totalExpense - totalHome - totalLoanPaid - totalMoneyLent }

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
        shoppingItems = ShoppingListStorage.load(context, userId)
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
                val remaining = loan.principal - paid
                
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
        
        val transReg = firestore.collection("users").document(userId).collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cloudError = "Cloud data load হয়নি: ${error.message}"
                    cloudLoading = false
                    return@addSnapshotListener
                }
                val cloudTransactions = snapshot?.documents?.mapNotNull { firestoreDocumentToTransaction(it) } ?: emptyList()
                val deletedIds = prefs.getStringSet("deleted_transaction_ids", emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
                val activeCloudTransactions = cloudTransactions.filter { it.id !in deletedIds }
                personalTransactions = activeCloudTransactions
                saveTransactions(prefs, activeCloudTransactions)
                cloudLoading = false
                cloudError = ""
            }
        registrations.add(transReg)

        val loansReg = firestore.collection("users").document(userId).collection("loans")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val cloudLoans = snapshot.documents.mapNotNull { doc ->
                        try {
                            val borrowingArray = doc.get("borrowings") as? List<Map<String, Any>>
                            val borrowings = borrowingArray?.map { b ->
                                LoanBorrowing(
                                    id = (b["id"] as? Number)?.toLong() ?: 0L,
                                    loanId = (b["loanId"] as? Number)?.toLong() ?: 0L,
                                    amount = (b["amount"] as? Number)?.toDouble() ?: 0.0,
                                    date = b["date"] as? String ?: "",
                                    note = b["note"] as? String ?: ""
                                )
                            } ?: emptyList()

                            LoanAccount(
                                id = doc.getLong("id") ?: 0L,
                                name = doc.getString("name") ?: "",
                                sourceType = doc.getString("sourceType") ?: "bank",
                                principal = doc.getDouble("principal") ?: 0.0,
                                monthlyInstallment = doc.getDouble("monthlyInstallment") ?: 0.0,
                                startDate = doc.getString("startDate") ?: "",
                                note = doc.getString("note") ?: "",
                                lastEditedDate = doc.getString("lastEditedDate") ?: "",
                                editHistory = doc.get("editHistory") as? List<String> ?: emptyList(),
                                borrowings = borrowings
                            )
                        } catch (e: Exception) { null }
                    }
                    if (cloudLoans.isNotEmpty()) {
                        loans = cloudLoans
                        saveLoans(prefs, cloudLoans)
                    }
                }
            }
        registrations.add(loansReg)

        val paymentsReg = firestore.collection("users").document(userId).collection("loanPayments")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val cloudPayments = snapshot.documents.mapNotNull { doc ->
                        try {
                            LoanPayment(
                                id = doc.getLong("id") ?: 0L,
                                loanId = doc.getLong("loanId") ?: 0L,
                                amount = doc.getDouble("amount") ?: 0.0,
                                date = doc.getString("date") ?: "",
                                note = doc.getString("note") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    if (cloudPayments.isNotEmpty()) {
                        loanPayments = cloudPayments
                        saveLoanPayments(prefs, cloudPayments)
                    }
                }
            }
        registrations.add(paymentsReg)

        val lendingsReg = firestore.collection("users").document(userId).collection("lendings")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val cloudLendings = snapshot.documents.mapNotNull { doc ->
                        try {
                            LendingAccount(
                                id = doc.getLong("id") ?: 0L,
                                person = doc.getString("person") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                date = doc.getString("date") ?: "",
                                note = doc.getString("note") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    if (cloudLendings.isNotEmpty()) {
                        lendings = cloudLendings
                        saveLendings(prefs, cloudLendings)
                    }
                }
            }
        registrations.add(lendingsReg)

        val returnsReg = firestore.collection("users").document(userId).collection("lendingReturns")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val cloudReturns = snapshot.documents.mapNotNull { doc ->
                        try {
                            LendingReturn(
                                id = doc.getLong("id") ?: 0L,
                                lendingId = doc.getLong("lendingId") ?: 0L,
                                amount = doc.getDouble("amount") ?: 0.0,
                                date = doc.getString("date") ?: "",
                                note = doc.getString("note") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    if (cloudReturns.isNotEmpty()) {
                        lendingReturns = cloudReturns
                        saveLendingReturns(prefs, cloudReturns)
                    }
                }
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
                !((it.type == "home" || it.type == "home_expense") && it.addedByUid != null)
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
        val homeTxns = personalTransactions.filter { it.type == "home" || it.type == "home_expense" }
        if (homeTxns.isEmpty()) return

        personalTransactions = personalTransactions.map { t ->
            if (t.type == "home" || t.type == "home_expense") {
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
        onComplete: () -> Unit
    ) {
        val transaction = Transaction(
            id = System.currentTimeMillis(),
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
        val isSharedHome = household != null && (type == "home" || type == "home_expense")
        val marked = if (isSharedHome) transaction.copy(
            addedByUid = currentUserId,
            addedByName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        ) else transaction

        personalTransactions = (personalTransactions + marked).distinctBy { it.id }
        saveTransactions(prefs, personalTransactions)
        saveAutoBackup(context)

        // নতুন লেনদেন সফলভাবে সেভ হলে sound + মৃদু vibration feedback
        SoundHapticHelper.playTransactionSavedFeedback(context)

        if (currentUserId != "guest") {
            saveTransactionToFirestore(firestore, currentUserId, marked, {
                Toast.makeText(context, "☁️ Cloud-এ save হয়েছে", Toast.LENGTH_SHORT).show()
            }, { message ->
                Toast.makeText(context, "⚠️ Cloud save হয়নি: $message", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, "⚠️ $category Budget Limit Cross হয়েছে!", Toast.LENGTH_LONG).show()
                    val newNotification = NotificationItem(
                        title = "🚨 $category Budget Limit Crossed",
                        message = "Budget limit cross হয়েছে।\nখরচ: ৳${formatMoney(spent)} / Limit: ৳${formatMoney(budget.limit)}"
                    )
                    notifications = notifications + newNotification
                    NotificationStorage.save(context, notifications, currentUserId)
                } else if (percentage >= 0.80) {
                    Toast.makeText(context, "⚠️ $category Budget-এর 80% ব্যবহার হয়েছে。", Toast.LENGTH_LONG).show()
                    val newNotification = NotificationItem(
                        title = "⚠️ $category Budget Warning",
                        message = "Budget-এর 80% ব্যবহার হয়েছে।\nখরচ: ৳${formatMoney(spent)} / Limit: ৳${formatMoney(budget.limit)}"
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
        saveAutoBackup(context)

        // লেনদেন সফলভাবে এডিট হলে হালকা feedback
        SoundHapticHelper.playTransactionUpdatedFeedback(context)

        if (currentUserId != "guest") {
            saveTransactionToFirestore(firestore, currentUserId, updatedTransaction, {
                Toast.makeText(context, "✏️ Cloud data update হয়েছে", Toast.LENGTH_SHORT).show()
            }, { message ->
                Toast.makeText(context, "⚠️ Cloud update হয়নি: $message", Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, "🗑️ Cloud data delete হয়েছে", Toast.LENGTH_SHORT).show()
            }, { message ->
                Toast.makeText(context, "⚠️ Cloud delete হয়নি: $message", Toast.LENGTH_LONG).show()
            })
        }
    }

    fun saveCategoryBudgetAction(context: Context, budget: CategoryBudget) {
        categoryBudgets = categoryBudgets.filterNot { it.month == budget.month && it.category == budget.category } + budget
        saveCategoryBudgets(prefs, categoryBudgets)
        Toast.makeText(context, "✅ ${budget.category} Budget Save হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addLoan(context: Context, name: String, type: String, amount: Double, monthly: Double, date: String, note: String, dueDate: String? = null) {
        val newLoan = LoanAccount(System.currentTimeMillis(), name, type, amount, monthly, date, note, dueDate = dueDate)
        loans = loans + newLoan
        persistLoanData(context)
        Toast.makeText(context, "✅ নতুন ঋণ যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun updateLoan(context: Context, loan: LoanAccount, name: String, type: String, amount: Double, monthly: Double, date: String, note: String, dueDate: String? = null) {
        val updated = loan.copy(name = name, sourceType = type, principal = amount, monthlyInstallment = monthly, startDate = date, note = note, lastEditedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()), editHistory = loan.editHistory + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()), dueDate = dueDate)
        loans = loans.map { if (it.id == loan.id) updated else it }
        persistLoanData(context)
        Toast.makeText(context, "✅ ঋণের তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addLoanPayment(context: Context, loan: LoanAccount, amount: Double, date: String, note: String, isHome: Boolean) {
        val payment = LoanPayment(System.currentTimeMillis(), loan.id, amount, date, note)
        loanPayments = loanPayments + payment
        persistLoanData(context)
        if (!isHome) Toast.makeText(context, "✅ পরিশোধের তথ্য সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addLending(context: Context, person: String, amount: Double, date: String, note: String, dueDate: String? = null) {
        val lending = LendingAccount(System.currentTimeMillis(), person, amount, date, note, dueDate = dueDate)
        lendings = lendings + lending
        persistLoanData(context)
        Toast.makeText(context, "✅ ধারের তথ্য সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun addLendingReturn(context: Context, lending: LendingAccount, amount: Double, date: String, note: String) {
        val ret = LendingReturn(System.currentTimeMillis(), lending.id, amount, date, note)
        lendingReturns = lendingReturns + ret
        persistLoanData(context)
        Toast.makeText(context, "✅ ধার ফেরতের তথ্য সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun updateBorrowing(context: Context, loan: LoanAccount, borrowing: LoanBorrowing, amount: Double, date: String, note: String) {
        val updatedBorrowing = borrowing.copy(amount = amount, date = date, note = note)
        val updatedLoan = loan.copy(borrowings = loan.borrowings.map { if (it.id == borrowing.id) updatedBorrowing else it })
        loans = loans.map { if (it.id == loan.id) updatedLoan else it }
        persistLoanData(context)
        Toast.makeText(context, "✅ ঋণের এন্ট্রি আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun deleteBorrowing(context: Context, loan: LoanAccount, borrowing: LoanBorrowing) {
        val updatedLoan = loan.copy(borrowings = loan.borrowings.filter { it.id != borrowing.id })
        loans = loans.map { if (it.id == loan.id) updatedLoan else it }
        persistLoanData(context)
        Toast.makeText(context, "🗑️ এন্ট্রিটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
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
            syncAllLoanAndLendingData(firestore, currentUserId, loans, loanPayments, lendings, lendingReturns)
        }
    }

    fun addWallet(context: Context, name: String, type: String, initialBalance: Double, currency: String, color: Int) {
        val newWallet = Wallet(id = "wallet_${System.currentTimeMillis()}", name = name, type = type, initialBalance = initialBalance, currency = currency, color = color)
        wallets = wallets + newWallet
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
        Toast.makeText(context, "✅ নতুন অ্যাকাউন্ট যোগ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun updateWallet(context: Context, wallet: Wallet) {
        wallets = wallets.map { if (it.id == wallet.id) wallet else it }
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
    }

    fun deleteWallet(context: Context, walletId: String) {
        if (walletId == "default_cash") {
            Toast.makeText(context, "ডিফল্ট অ্যাকাউন্ট মোছা সম্ভব নয়", Toast.LENGTH_SHORT).show()
            return
        }
        wallets = wallets.filter { it.id != walletId }
        saveWallets(prefs, wallets)
        saveAutoBackup(context)
        Toast.makeText(context, "🗑️ অ্যাকাউন্ট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
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

    fun saveAutoBackup(context: Context) {
        saveAutoBackup(context, currentUserId, transactions, usdToBdt, usdToMvr, loans, loanPayments, lendings, lendingReturns, wallets)
    }

    // --------------------------------------------------
    // বাজারের ফর্দ / Shopping List
    // --------------------------------------------------

    private fun persistShoppingList(context: Context) {
        ShoppingListStorage.save(context, currentUserId, shoppingItems)
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
