@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.eleyas.expensetracker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.*
import com.eleyas.expensetracker.ui.screens.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.eleyas.expensetracker.ui.vehicle.VehicleModule

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmarHisabTheme {
                AuthGate()
                AppUpdateDialog(context = LocalContext.current)
            }
        }
    }
}

@Composable
fun AuthGate() {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    val recoveryPrefs = remember { context.getSharedPreferences("admin_recovery", Context.MODE_PRIVATE) }
    var isRecoveryMode by remember { mutableStateOf(recoveryPrefs.getBoolean("recovery_active", false)) }

    if (currentUser != null || isRecoveryMode) {
        val uid = currentUser?.uid ?: "admin_recovery"
        AmarHisabApp(currentUserId = uid, onLogout = {
            FirebaseAuth.getInstance().signOut()
            currentUser = null
            recoveryPrefs.edit().putBoolean("recovery_active", false).apply()
            isRecoveryMode = false
        })
    } else {
        GoogleLoginScreen(onLoginSuccess = { currentUser = FirebaseAuth.getInstance().currentUser })
    }
}

@Composable
fun AmarHisabApp(currentUserId: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    val prefs = remember(currentUserId) { AccountStorage.getPrefs(context, currentUserId) }

    LaunchedEffect(currentUserId) {
        viewModel.init(context, currentUserId, prefs)
        DailyReminderManager.scheduleDailyReminder(context)
        RecapNotificationManager.scheduleWeeklyRecap(context)
        RecapNotificationManager.scheduleMonthlyRecap(context)
    }
    LaunchedEffect(Unit) {
        com.eleyas.expensetracker.util.SMSReceiver.setSMSSuggestionCallback {
            com.eleyas.expensetracker.util.SMSSuggestionHolder.updateSuggestions(context)
        }
    }

    val transactions = viewModel.transactions
    val loans = viewModel.loans
    val categoryBudgets = viewModel.categoryBudgets
    val loanPayments = viewModel.loanPayments
    val loanInterestTerms = viewModel.loanInterestTerms
    val lendings = viewModel.lendings
    val lendingReturns = viewModel.lendingReturns
    val wallets = viewModel.wallets
    val usdToBdt = viewModel.usdToBdt
    val usdToMvr = viewModel.usdToMvr
    val notifications = viewModel.notifications
    val birthday = viewModel.birthday
    val household = viewModel.household
    val rateLoading = viewModel.rateLoading
    val rateError = viewModel.rateError

    val tickerInsight = remember(transactions) { FinancialInsights.generateInsight(transactions) }
    val tickerTip = remember { DailyFinancialTips.getTodayTip() }
    val tickerMessages = remember(tickerInsight, tickerTip) {
        listOf(
            "💡 $tickerInsight",
            "📜 আজকের টিপস: ${tickerTip.quote}${if (tickerTip.author.isNullOrBlank()) "" else "  — ${tickerTip.author}"}"
        )
    }

    var selectedTab by remember(currentUserId) { mutableIntStateOf(0) }
    var showNotificationScreen by remember { mutableStateOf(false) }
    var showBudgetDialog by remember(currentUserId) { mutableStateOf(false) }
    var customCategories by remember(currentUserId) { mutableStateOf(loadCustomCategories(prefs)) }
    var showAdminConsole by remember { mutableStateOf(false) }
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var showAddDialog by remember(currentUserId) { mutableStateOf(false) }
    var showCalendarScreen by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var addType by remember(currentUserId) { mutableStateOf("income") }
    var editingTransaction by remember(currentUserId) { mutableStateOf<Transaction?>(null) }
    var deletingTransaction by remember(currentUserId) { mutableStateOf<Transaction?>(null) }
    var showDeleteDialog by remember(currentUserId) { mutableStateOf(false) }
    var showLoanDialog by remember(currentUserId) { mutableStateOf(false) }
    var showLoanPaymentDialog by remember(currentUserId) { mutableStateOf(false) }
    var selectedLoan by remember(currentUserId) { mutableStateOf<LoanAccount?>(null) }
    var editingLoan by remember(currentUserId) { mutableStateOf<LoanAccount?>(null) }
    var editingBorrowing by remember(currentUserId) { mutableStateOf<Pair<LoanAccount, LoanBorrowing>?>(null) }
    var deletingBorrowing by remember(currentUserId) { mutableStateOf<Pair<LoanAccount, LoanBorrowing>?>(null) }
    var showLendingDialog by remember(currentUserId) { mutableStateOf(false) }
    var showLendingReturnDialog by remember(currentUserId) { mutableStateOf(false) }
    var selectedLending by remember(currentUserId) { mutableStateOf<LendingAccount?>(null) }
    var sharingLoanPdf by remember { mutableStateOf<LoanAccount?>(null) }
    var sharingLendingPdf by remember { mutableStateOf<LendingAccount?>(null) }
    var showWalletDialog by remember(currentUserId) { mutableStateOf(false) }
    var selectedWalletForEdit by remember(currentUserId) { mutableStateOf<Wallet?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDailyTipDialog by remember { mutableStateOf(false) }
    var showTickerDetail by remember { mutableStateOf(false) }
    var showFamilyDialog by remember { mutableStateOf(false) }
    var showCalculatorScreen by remember { mutableStateOf(false) }
    var showShoppingList by remember { mutableStateOf(false) }
    var showVehicleModule by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showSplitBillDialog by remember(currentUserId) { mutableStateOf(false) }
    var selectedSplitBill by remember(currentUserId) { mutableStateOf<SplitBillGroup?>(null) }
    var splitBills by remember(currentUserId) { mutableStateOf(SplitBillStorage.loadSplits(context, currentUserId)) }
    var isSearchActive by remember { mutableStateOf(false) }
    var globalSearchQuery by remember { mutableStateOf("") }
    var updateInfo by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }
    var settingsSubView by remember { mutableStateOf<String?>(null) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var profilePhotoUri by remember(currentUserId) { mutableStateOf(prefs.getString("profile_photo_uri", null)) }
    var smsSuggestions by remember { SMSSuggestionHolder.suggestions }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(currentUserId) {
        firestore.collection("config").document("app_notice").addSnapshotListener { snapshot, _ ->
            val maintenance = snapshot?.getBoolean("maintenance") ?: false
            showMaintenanceDialog = maintenance && FirebaseAuth.getInstance().currentUser?.email != "mdeleyas040@gmail.com"
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profilePhotoUri = it.toString()
            prefs.edit().putString("profile_photo_uri", it.toString()).apply()
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let { if (exportBackupToUri(context, it, transactions, usdToBdt, usdToMvr, loans, loanPayments, lendings, lendingReturns, wallets)) Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show() }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val backup = importBackupFromUri(context, it)
            if (backup != null) {
                viewModel.updateCloudData(backup.transactions, backup.loans, backup.loanPayments, backup.lendings, backup.lendingReturns, backup.wallets)
                Toast.makeText(context, "Backup imported", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val pdfExportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let { ReportExporter.exportToPdf(context, it, transactions, viewModel.totalIncome, viewModel.totalExpense, viewModel.balance) }
    }
    val csvExportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let { viewModel.exportTransactionsCsv(context, it) }
    }
    val loanPdfExportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let {
            sharingLoanPdf?.let { loan ->
                val history = viewModel.loanPayments.filter { it.loanId == loan.id }.map { it.date to it.amount }
                viewModel.exportPersonStatementPdf(context, it, loan.name, loan.principal, history, false)
            }
        }
    }
    val lendingPdfExportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let {
            sharingLendingPdf?.let { lending ->
                val history = viewModel.lendingReturns.filter { it.lendingId == lending.id }.map { it.date to it.amount }
                viewModel.exportPersonStatementPdf(context, it, lending.person, lending.amount, history, true)
            }
        }
    }
    var sharingSearchQuery by remember { mutableStateOf<String?>(null) }
    val searchPdfExportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let {
            sharingSearchQuery?.let { query -> viewModel.exportSearchResultsPdf(context, it, query) }
        }
    }

    BackHandler(
        enabled = showCalendarScreen || showCalculatorScreen || showShoppingList || showVehicleModule || showNotificationScreen || showSettingsScreen || settingsSubView != null || selectedTab != 0 || isSearchActive
    ) {
        when {
            showCalendarScreen -> showCalendarScreen = false
            showCalculatorScreen -> showCalculatorScreen = false
            showShoppingList -> showShoppingList = false
            showNotificationScreen -> showNotificationScreen = false
            showVehicleModule -> showVehicleModule = false
            settingsSubView != null -> settingsSubView = null
            showSettingsScreen -> showSettingsScreen = false
            isSearchActive -> { isSearchActive = false; globalSearchQuery = "" }
            selectedTab != 0 -> selectedTab = 0
        }
    }

    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            if (!showCalendarScreen && !showCalculatorScreen && !showShoppingList && !showNotificationScreen && !showSettingsScreen && !isSearchActive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CenterAlignedTopAppBar(
                        scrollBehavior = topBarScrollBehavior,
                        navigationIcon = {
                            Box {
                                IconButton(onClick = { showTopMenu = true }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Main menu")
                                }
                                DropdownMenu(
                                    expanded = showTopMenu,
                                    onDismissRequest = { showTopMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("পরিবার শেয়ার") },
                                        onClick = { showTopMenu = false; showFamilyDialog = true }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("ক্যালেন্ডার") },
                                        onClick = { showTopMenu = false; showCalendarScreen = true }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("বাজারের ফর্দ") },
                                        onClick = { showTopMenu = false; showShoppingList = true }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("সেটিংস") },
                                        onClick = { showTopMenu = false; showSettingsScreen = true }
                                    )
                                }
                            }
                        },
                        title = {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Amar Hisab"
                                    1 -> "আমার আয়"
                                    2 -> "আমার খরচ"
                                    3 -> "আমার রিপোর্ট"
                                    4 -> "ঋণ ও ধার"
                                    else -> "Amar Hisab"
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedTab == 4) {
                                    IconButton(onClick = { showCalculatorScreen = true }) {
                                        Icon(Icons.Default.Calculate, contentDescription = "Calculator")
                                    }
                                }
                                IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                    Icon(if (isSearchActive) Icons.Default.Clear else Icons.Default.Search, contentDescription = "Search")
                                }
                                if (selectedTab == 0) {
                                    IconButton(onClick = { showNotificationScreen = true }) {
                                        BadgedBox(badge = { if (notifications.any { !it.isRead }) Badge() }) {
                                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    NewsTickerBar(
                        messages = tickerMessages,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onTickerClick = { showTickerDetail = true }
                    )
                }
            }
        },
        bottomBar = {
            if (!showCalendarScreen && !showCalculatorScreen && !showShoppingList && !showNotificationScreen && !showSettingsScreen && !isSearchActive) {
                AnimatedVisibility(
                    visible = topBarScrollBehavior.state.collapsedFraction < 0.5f,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    AnimatedBottomNavigation(selectedTab = selectedTab, onTabSelected = {
                        selectedTab = it.coerceIn(0, 4)
                        isSearchActive = false
                        globalSearchQuery = ""
                    })
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            BirthdayPopupCheck(currentUserId, birthday)
            when (selectedTab) {
                0 -> HomeScreen(Modifier.fillMaxSize(), currentUserId, viewModel.balance, viewModel.totalIncome, viewModel.totalExpense, viewModel.totalHome, viewModel.totalHomeExpense, viewModel.homeBalance, viewModel.totalLoanReceived, viewModel.totalLoanPaid, viewModel.totalLoanRemaining, viewModel.totalMoneyLent, viewModel.totalMoneyReturned, viewModel.totalMoneyToReceive, birthday, wallets, { viewModel.updateBirthday(context, it) }, { addType = "income"; showAddDialog = true }, { addType = "expense"; showAddDialog = true }, { addType = "home"; showAddDialog = true }, { addType = "home_expense"; showAddDialog = true }, { showWalletDialog = true; selectedWalletForEdit = null }, { selectedWalletForEdit = it; showWalletDialog = true }, { viewModel.getWalletBalance(it) }, onVoiceClick = { showVoiceDialog = true }, onShoppingList = { showShoppingList = true }, onVehicle = { showVehicleModule = true }, transactions = transactions, household = household, onFamilyClick = { showFamilyDialog = true })
                1 -> IncomeScreen(Modifier.fillMaxSize(), transactions.filter { it.type == "income" }, wallets, usdToBdt, usdToMvr, globalSearchQuery, { addType = "income"; editingTransaction = null; showAddDialog = true }, { editingTransaction = it; showAddDialog = true }, { deletingTransaction = it; showDeleteDialog = true })
                2 -> ExpenseScreen(Modifier.fillMaxSize(), transactions.filter { it.type == "expense" || it.type == "home" }, wallets, usdToBdt, usdToMvr, globalSearchQuery, { addType = "expense"; editingTransaction = null; showAddDialog = true }, { addType = "home"; editingTransaction = null; showAddDialog = true }, { editingTransaction = it; showAddDialog = true }, { deletingTransaction = it; showDeleteDialog = true }, splitBills = splitBills, onAddSplitBill = { showSplitBillDialog = true }, onSplitBillClick = { selectedSplitBill = it })
                3 -> ReportScreen(Modifier.fillMaxSize(), transactions, wallets, categoryBudgets, usdToBdt, usdToMvr, { editingTransaction = it; showAddDialog = true }, { deletingTransaction = it; showDeleteDialog = true })
                4 -> LoansScreen(Modifier.fillMaxSize(), loans, loanPayments, lendings, lendingReturns, { showLoanDialog = true }, { selectedLoan = it; showLoanPaymentDialog = true }, { editingLoan = it; showLoanDialog = true }, { loan, borrowing -> editingBorrowing = loan to borrowing }, { loan, borrowing -> deletingBorrowing = loan to borrowing }, { showLendingDialog = true }, { selectedLending = it; showLendingReturnDialog = true }, loanInterestTerms, onShowCalculator = { showCalculatorScreen = true }, onShareLoan = { loan, isPdf -> if (isPdf) { sharingLoanPdf = loan; loanPdfExportLauncher.launch("Statement_${loan.name.replace(" ", "_")}.pdf") } else { val text = viewModel.getLoanStatement(loan); val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, text) }; context.startActivity(android.content.Intent.createChooser(intent, "Share via")) } }, onShareLending = { lending, isPdf -> if (isPdf) { sharingLendingPdf = lending; lendingPdfExportLauncher.launch("Statement_${lending.person.replace(" ", "_")}.pdf") } else { val text = viewModel.getLendingStatement(lending); val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, text) }; context.startActivity(android.content.Intent.createChooser(intent, "Share via")) } }, searchQuery = globalSearchQuery)
            }

            if (showSettingsScreen) {
                SettingsScreen(Modifier.fillMaxSize(), currentUserId, usdToBdt, usdToMvr, rateLoading, rateError, settingsSubView, profilePhotoUri, isAdminUnlocked, { photoLauncher.launch("image/*") }, { isAdminUnlocked = true }, { settingsSubView = it }, { viewModel.refreshRate() }, { viewModel.saveAutoBackup(context); Toast.makeText(context, "✅ Auto backup updated", Toast.LENGTH_SHORT).show() }, {
                    val restored = loadAutoBackup(context, currentUserId)
                    if (restored != null) { viewModel.updateCloudData(restored.transactions, restored.loans, restored.loanPayments, restored.lendings, restored.lendingReturns); Toast.makeText(context, "✅ Auto backup restore হয়েছে", Toast.LENGTH_SHORT).show() }
                    else Toast.makeText(context, "❌ কোনো Auto Backup পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }, { exportBackupLauncher.launch("AmarHisab_Backup.json") }, { importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }, { pdfExportLauncher.launch("AmarHisab_Report_${SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(Date())}.pdf") }, { csvExportLauncher.launch("AmarHisab_Export_${SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())}.csv") }, {
                    val text = "আমার হিসাব\nআয়: ৳${formatMoney(viewModel.totalIncome)}\nখরচ: ৳${formatMoney(viewModel.totalExpense)}\nব্যালেন্স: ৳${formatMoney(viewModel.balance)}"
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(android.content.ClipData.newPlainText("আমার হিসাব", text))
                }, { showBudgetDialog = true }, { viewModel.resetCurrentAccountData(context) { } }, { showEditName = true }, {
                    firestore.collection("config").document("app_version").get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val latest = doc.getLong("latestVersionCode") ?: 1L
                            val current = try { val pInfo = context.packageManager.getPackageInfo(context.packageName, 0); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong() } catch (e: Exception) { 1L }
                            if (latest > current) { updateInfo = doc.data; showUpdateDialog = true } else Toast.makeText(context, "আপনার অ্যাপটি লেটেস্ট ভার্সনে আছে", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, { showAdminConsole = true }, {
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
                    firestore.collection("config").document("app_version").set(mapOf("latestVersionCode" to vCode, "updateMessage" to "নতুন ভার্সন ${pInfo.versionName} এসেছে। এখনই আপডেট করে নিন!"), com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener { Toast.makeText(context, "আপডেট সফলভাবে পাবলিশ হয়েছে!", Toast.LENGTH_LONG).show() }
                }, onLogout)
            }

            if (showCalendarScreen) CalendarScreen(Modifier.fillMaxSize(), transactions = transactions)
            if (showCalculatorScreen) CalculatorScreen(onBack = { showCalculatorScreen = false })
            if (showShoppingList) ShoppingListScreen(Modifier.fillMaxSize(), items = viewModel.shoppingItems, onBack = { showShoppingList = false }, onAdd = { n, a, cu, c, nt -> viewModel.addShoppingItem(context, n, a, cu, c, nt) }, onToggle = { id -> viewModel.toggleShoppingItem(context, id) }, onRemove = { item -> viewModel.removeShoppingItem(context, item) }, onEdit = { item -> viewModel.updateShoppingItem(context, item) }, onConvert = { cb -> viewModel.convertCheckedToExpenses(context, cb) }, onClearAdded = { viewModel.clearAddedShoppingItems(context) })
            if (showVehicleModule) VehicleModule(userId = currentUserId, modifier = Modifier.fillMaxSize(), onBack = { showVehicleModule = false })
            if (showNotificationScreen) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    NotificationScreen(notifications = notifications, onNotificationClick = { /* Handle click */ })
                    IconButton(onClick = { showNotificationScreen = false }, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            }
            SearchOverlay(active = isSearchActive, searchScope = selectedTab, transactions = transactions, loans = loans, lendings = lendings, loanPayments = loanPayments, lendingReturns = lendingReturns, wallets = wallets, usdToBdt = usdToBdt, usdToMvr = usdToMvr, onEditTransaction = { editingTransaction = it; showAddDialog = true }, onDeleteTransaction = { deletingTransaction = it; showDeleteDialog = true }, onShareResults = { query, isPdf -> if (isPdf) { sharingSearchQuery = query; searchPdfExportLauncher.launch("Report_${query.replace(" ", "_")}.pdf") } else { val text = viewModel.generateSearchStatement(query); val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, text) }; context.startActivity(android.content.Intent.createChooser(intent, "Share Search Results")) } }, onClose = { isSearchActive = false; globalSearchQuery = "" })
        }
    }

    if (showVoiceDialog) VoiceInputDialog(onDismiss = { showVoiceDialog = false }, onResult = { result -> addType = result.type; editingTransaction = Transaction(id = 0, type = result.type, amount = result.amount ?: 0.0, currency = "BDT", category = result.category, reason = result.originalText, date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())); showAddDialog = true; showVoiceDialog = false })
    if (smsSuggestions.isNotEmpty()) SMSSuggestionDialog(suggestions = smsSuggestions, onAddTransaction = { suggestion -> editingTransaction = Transaction(id = 0, type = suggestion.transactionType, amount = suggestion.amount, currency = "BDT", category = suggestion.category, reason = suggestion.description, date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), walletId = "default_cash"); addType = suggestion.transactionType; showAddDialog = true; com.eleyas.expensetracker.util.SMSReceiver.removeSuggestion(context, suggestion.id); com.eleyas.expensetracker.util.SMSSuggestionHolder.updateSuggestions(context) }, onDismiss = { suggestion -> com.eleyas.expensetracker.util.SMSReceiver.removeSuggestion(context, suggestion.id); com.eleyas.expensetracker.util.SMSSuggestionHolder.updateSuggestions(context) }, onDismissAll = { com.eleyas.expensetracker.util.SMSSuggestionHolder.suggestions.value = emptyList() })
    if (showAdminConsole) AdminConsoleDialog(onDismiss = { showAdminConsole = false })
    if (showMaintenanceDialog) AlertDialog(onDismissRequest = { }, title = { Text("Maintenance Mode") }, text = { Text("অ্যাপটি বর্তমানে আপগ্রেড করা হচ্ছে।") }, confirmButton = { })
    if (showUpdateDialog && updateInfo != null) { val force = updateInfo!!["forceUpdate"] as? Boolean ?: false; AlertDialog(onDismissRequest = { if (!force) showUpdateDialog = false }, title = { Text("Update Available") }, text = { Text(updateInfo!!["updateMessage"] as? String ?: "") }, confirmButton = { Button(onClick = { try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(updateInfo!!["updateUrl"] as? String ?: ""))) } catch (e: Exception) { } }) { Text("Update Now") } }) }
    if (showEditName) { val user = FirebaseAuth.getInstance().currentUser; var newName by remember { mutableStateOf(user?.displayName ?: "") }; AlertDialog(onDismissRequest = { showEditName = false }, title = { Text("Edit Name") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Full Name") }) }, confirmButton = { Button(onClick = { user?.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(newName).build())?.addOnSuccessListener { showEditName = false } }) { Text("Update") } }) }
    if (showWalletDialog) WalletDialog(onDismiss = { showWalletDialog = false; selectedWalletForEdit = null }, existingWallet = selectedWalletForEdit, onSave = { n, t, b, c, cl -> if (selectedWalletForEdit != null) viewModel.updateWallet(context, selectedWalletForEdit!!.copy(name = n, type = t, initialBalance = b, currency = c, color = cl)) else viewModel.addWallet(context, n, t, b, c, cl); showWalletDialog = false; selectedWalletForEdit = null }, onDelete = { if (selectedWalletForEdit != null) { viewModel.deleteWallet(context, selectedWalletForEdit!!.id); showWalletDialog = false; selectedWalletForEdit = null } })
    if (showAddDialog) AddTransactionDialog(addType, editingTransaction, loans, loanPayments, wallets, customCategories, { newCategory -> val normalized = newCategory.trim().replace(Regex("\\s+"), " "); if (normalized.isNotBlank()) { customCategories = (customCategories + normalized).distinct().sortedBy { it.lowercase() }; saveCustomCategories(prefs, customCategories) } }, { showAddDialog = false; editingTransaction = null }, { a, c, cat, r, d, w, i -> if (editingTransaction != null && editingTransaction!!.id != 0L) viewModel.updateTransaction(context, editingTransaction!!.copy(amount = a, currency = c, category = cat, reason = r, date = d, walletId = w, receiptImage = i)) else viewModel.saveTransaction(context, a, c, cat, r, d, addType, w, i) { showAddDialog = false }; showAddDialog = false; editingTransaction = null }, { id, a, d -> loans.firstOrNull { it.id == id }?.let { viewModel.addLoanPayment(context, it, a, d, "বাড়িতে পাঠানো টাকা থেকে Loan payment", true) } })
    if (showBudgetDialog) CategoryBudgetDialog(categoryBudgets, customCategories, { showBudgetDialog = false }, { viewModel.saveCategoryBudgetAction(context, it); showBudgetDialog = false }, { newCategory -> val normalized = newCategory.trim().replace(Regex("\\s+"), " "); if (normalized.isNotBlank()) { customCategories = (customCategories + normalized).distinct().sortedBy { it.lowercase() }; saveCustomCategories(prefs, customCategories) } })
    if (editingBorrowing != null) BorrowingDialog(editingBorrowing!!.second, { editingBorrowing = null }, { a, d, n -> viewModel.updateBorrowing(context, editingBorrowing!!.first, editingBorrowing!!.second, a, d, n); editingBorrowing = null })
    if (deletingBorrowing != null) AlertDialog(onDismissRequest = { deletingBorrowing = null }, title = { Text("ঋণের entry মুছবেন?") }, text = { Text("৳${formatMoney(deletingBorrowing!!.second.amount)} — ${displayLoanDate(deletingBorrowing!!.second.date)}") }, confirmButton = { Button(onClick = { viewModel.deleteBorrowing(context, deletingBorrowing!!.first, deletingBorrowing!!.second); deletingBorrowing = null }, colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)) { Text("মুছে ফেলুন") } }, dismissButton = { TextButton(onClick = { deletingBorrowing = null }) { Text("বাতিল") } })
    if (showLoanDialog) LoanDialog({ showLoanDialog = false; editingLoan = null }, editingLoan, loans.map { it.name }.distinct(), { n, s, p, m, d, nt, dd -> if (editingLoan != null) viewModel.updateLoan(context, editingLoan!!, n, s, p, m, d, nt, dd) else viewModel.addLoan(context, n, s, p, m, d, nt, dd); showLoanDialog = false })
    if (showLoanPaymentDialog && selectedLoan != null) LoanPaymentDialog(selectedLoan!!, { selectedLoan = null; showLoanPaymentDialog = false }, { a, d, n -> viewModel.addLoanPayment(context, selectedLoan!!, a, d, n, false); selectedLoan = null; showLoanPaymentDialog = false })
    if (showLendingDialog) LendingDialog({ showLendingDialog = false }, { p, a, d, n, dd -> viewModel.addLending(context, p, a, d, n, dd); showLendingDialog = false })
    if (showLendingReturnDialog && selectedLending != null) LendingReturnDialog(selectedLending!!, { selectedLending = null; showLendingReturnDialog = false }, { a, d, n -> viewModel.addLendingReturn(context, selectedLending!!, a, d, n); selectedLending = null; showLendingReturnDialog = false })
    if (showDeleteDialog && deletingTransaction != null) AlertDialog(onDismissRequest = { showDeleteDialog = false; deletingTransaction = null }, title = { Text("লেনদেন মুছে ফেলবেন?") }, text = { Text("${deletingTransaction!!.reason.ifBlank { deletingTransaction!!.category }} — ${deletingTransaction!!.currency} ${formatMoney(deletingTransaction!!.amount)}") }, confirmButton = { Button(onClick = { viewModel.deleteTransaction(context, deletingTransaction!!); showDeleteDialog = false; deletingTransaction = null }, colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)) { Text("মুছে ফেলুন") } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false; deletingTransaction = null }) { Text("বাতিল") } })
    if (showSplitBillDialog) SplitBillDialog(onDismiss = { showSplitBillDialog = false }, onSave = { newSplit -> splitBills = splitBills + newSplit; SplitBillStorage.saveSplits(context, currentUserId, splitBills); showSplitBillDialog = false; Toast.makeText(context, "✅ স্প্লিট বিল সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show() })
    if (selectedSplitBill != null) SplitBillDetailDialog(split = selectedSplitBill!!, onDismiss = { selectedSplitBill = null }, onDelete = { splitToDelete -> splitBills = splitBills.filter { it.id != splitToDelete.id }; SplitBillStorage.saveSplits(context, currentUserId, splitBills); selectedSplitBill = null; Toast.makeText(context, "🗑️ স্প্লিট বিল মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show() })
    if (showFamilyDialog) FamilyDialog(household = household, busy = viewModel.householdBusy, onDismiss = { showFamilyDialog = false }, onCreate = { name -> viewModel.createHousehold(context, name) { ok, msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() } }, onJoin = { code -> viewModel.joinHousehold(context, code) { ok, msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() } }, onLeave = { viewModel.leaveHousehold(context) { ok, msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() } })
    if (showTickerDetail) TickerDetailDialog(insight = tickerInsight, tip = tickerTip, onDismiss = { showTickerDetail = false })
}

@Composable
fun DailyTipPopupCheck(userId: String) {
    val context = LocalContext.current
    var showTip by remember(userId) { mutableStateOf(DailyFinancialTips.shouldShowDailyTip(context, userId)) }
    if (showTip) {
        DailyTipDialog(onDismiss = { DailyFinancialTips.markDailyTipShown(context, userId); showTip = false })
    }
}
