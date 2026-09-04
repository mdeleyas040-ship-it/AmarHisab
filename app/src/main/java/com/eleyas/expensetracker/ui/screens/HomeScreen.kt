package com.eleyas.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    modifier: Modifier,
    currentUserId: String,
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    totalHome: Double,
    totalHomeExpense: Double,
    homeBalance: Double,
    loanReceived: Double = 0.0,
    loanPaid: Double = 0.0,
    loanRemaining: Double = 0.0,
    moneyLent: Double = 0.0,
    moneyReturned: Double = 0.0,
    moneyToReceive: Double = 0.0,
    birthday: Pair<Int, Int>?,
    wallets: List<Wallet> = emptyList(),
    onBirthdayChange: (Pair<Int, Int>?) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddHome: () -> Unit,
    onAddHomeExpense: () -> Unit,
    onAddWallet: () -> Unit,
    onWalletClick: (Wallet) -> Unit,
    getWalletBalance: (String) -> Double,
    onVoiceClick: () -> Unit,
    onShoppingList: () -> Unit = {},
    onVehicle: () -> Unit = {},
    transactions: List<Transaction> = emptyList(),
    household: Household? = null,
    onFamilyClick: () -> Unit = {},
    onDailyTipClick: () -> Unit = {},
    onReminderClick: (SmartReminder) -> Unit = {}
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val vm: MainViewModel = viewModel()
    var serverNotice by remember { mutableStateOf<String?>(null) }
    var showHomeMoneyFlow by remember { mutableStateOf(false) }
    var debtMode by rememberSaveable { mutableStateOf(0) }
    var showPremiumLoanDialog by remember { mutableStateOf(false) }
    var showPremiumLendingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.collection("config").document("app_notice")
            .addSnapshotListener { snapshot, _ -> serverNotice = snapshot?.getString("message")?.takeIf { it.isNotBlank() } }
    }
    BackHandler(enabled = showHomeMoneyFlow) { showHomeMoneyFlow = false }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing)
        ) {
            if (serverNotice != null) item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = .1f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, null, tint = Blue, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(10.dp)); Text(serverNotice!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue)
                    }
                }
            }
            val reminders = remember(transactions) { SmartReminderManager.getTransactionReminders(transactions) }
            if (reminders.isNotEmpty()) item { SmartReminderCard(reminders = reminders, onReminderClick = onReminderClick) }

            item {
                PremiumDebtDashboard(
                    mode = debtMode,
                    onModeChange = { debtMode = it },
                    loanReceived = loanReceived,
                    loanPaid = loanPaid.coerceAtLeast(0.0),
                    loanRemaining = loanRemaining.coerceAtLeast(0.0),
                    moneyLent = moneyLent,
                    moneyReturned = moneyReturned.coerceAtLeast(0.0),
                    moneyToReceive = moneyToReceive.coerceAtLeast(0.0),
                    onAddLoan = { showPremiumLoanDialog = true },
                    onAddLending = { showPremiumLendingDialog = true }
                )
            }

            item {
                Card(Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(CardRadius), Color.Black, AccentGreen.copy(alpha=.35f)), shape = RoundedCornerShape(CardRadius), colors = CardDefaults.cardColors(containerColor = Color(0xFF181B21))) {
                    Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1E222A), Color(0xFF12141A))))) {
                        Column(Modifier.fillMaxWidth().padding(CardPadding)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column { Text("মোট ব্যালেন্স", color = AccentGreen.copy(alpha=.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium); Spacer(Modifier.height(4.dp)); Text("৳ ${formatMoney(balance)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold); Text("বাংলাদেশি টাকা (BDT)", color = Color.White.copy(alpha=.45f), fontSize = 10.sp) }
                                Surface(Modifier.size(50.dp), RoundedCornerShape(14.dp), color = AccentGreen.copy(alpha=.1f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = AccentGreen, modifier = Modifier.size(28.dp)) } }
                            }
                            Spacer(Modifier.height(24.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatMiniBox(Modifier.weight(1f), "আয়", totalIncome, IncomeGreen); StatMiniBox(Modifier.weight(1f), "খরচ", totalExpense, ExpenseRed); StatMiniBox(Modifier.weight(1f), "বাড়িতে", totalHome, Blue) }
                            Spacer(Modifier.height(16.dp)); BirthdayCountdownCard(currentUserId, birthday, { onBirthdayChange(it) }, isCompact = true)
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("আমার অ্যাকাউন্টসমূহ", fontSize = 19.sp, fontWeight = FontWeight.Bold); TextButton(onClick = onAddWallet) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("নতুন", fontWeight = FontWeight.Bold) } }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(wallets) { wallet -> Card(onClick={onWalletClick(wallet)}, Modifier.width(150.dp).height(100.dp), RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color(wallet.color.toLong() and 0xFFFFFFFFL))) { Column(Modifier.padding(14.dp).fillMaxSize(), verticalArrangement=Arrangement.SpaceBetween) { Text(wallet.name,color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold,maxLines=1); Column { Text("৳${formatMoney(getWalletBalance(wallet.id))}",color=Color.White,fontSize=16.sp,fontWeight=FontWeight.ExtraBold); Text(wallet.type,color=Color.White.copy(alpha=.7f),fontSize=10.sp) } } } } }
            }

            item { Text("দ্রুত অ্যাকশন", fontSize = 19.sp, fontWeight = FontWeight.Bold) }
            item {
                val actions = listOf(Triple(Icons.Default.AddCircle,"আয় যোগ",IncomeGreen) to onAddIncome, Triple(Icons.Default.RemoveCircle,"খরচ যোগ",ExpenseRed) to onAddExpense, Triple(Icons.Default.Home,"বাড়িতে পাঠান",Blue) to onAddHome, Triple(Icons.Default.ReceiptLong,"বাড়ির খরচ",Color(0xFFF59E0B)) to onAddHomeExpense, Triple(Icons.Default.Mic,"ভয়েস এন্ট্রি",Color(0xFF9C27B0)) to onVoiceClick, Triple(Icons.Default.ShoppingCart,"বাজারের ফর্দ",AccentGreen) to onShoppingList, Triple(Icons.Default.DirectionsCar,"Vehicle & Maintenance",Color(0xFF1565C0)) to onVehicle)
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)) { for (i in actions.indices step 2) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) { QuickActionCard(Modifier.weight(1f),actions[i].first.first,actions[i].first.second,actions[i].first.third,actions[i].second); if(i+1<actions.size) QuickActionCard(Modifier.weight(1f),actions[i+1].first.first,actions[i+1].first.second,actions[i+1].first.third,actions[i+1].second) else Spacer(Modifier.weight(1f)) } }
            }

            item {
                Card(onClick={showHomeMoneyFlow=true}, Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp)) { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) { Row(verticalAlignment=Alignment.CenterVertically) { Surface(Modifier.size(42.dp),RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.primary.copy(alpha=.12f)) { Box(contentAlignment=Alignment.Center) { Icon(Icons.Default.HomeWork,null,tint=MaterialTheme.colorScheme.primary) } }; Spacer(Modifier.width(10.dp)); Column { Text("বাড়ির হিসাব",fontSize=17.sp,fontWeight=FontWeight.Bold); Text("বাড়ির সব টাকা এক জায়গায়",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant) } }; Icon(Icons.Default.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.height(12.dp)); HomeSummaryRow("বাড়িতে পাঠানো",totalHome,Blue); HomeSummaryRow("বাড়ির খরচ",totalHomeExpense,Color(0xFFF59E0B)); HorizontalDivider(Modifier.padding(vertical=7.dp)); HomeSummaryRow("বাড়িতে অবশিষ্ট",homeBalance,IncomeGreen) } }
            }
        }

        if (showHomeMoneyFlow) Surface(Modifier.fillMaxSize(), color=MaterialTheme.colorScheme.background) { HomeMoneyFlowScreen(HomeMoneyFlow.entries(vm), onBack={showHomeMoneyFlow=false}) }

        if (showPremiumLoanDialog) PremiumLoanDialog(onDismiss={showPremiumLoanDialog=false}, existingLoan=null, existingNames=vm.loans.map{it.name}.distinct(), onSave={name,sourceType,principal,monthlyInstallment,startDate,note,dueDate -> vm.addLoan(context,name,sourceType,principal,monthlyInstallment,startDate,note,dueDate); showPremiumLoanDialog=false})
        if (showPremiumLendingDialog) LendingDialog(onDismiss={showPremiumLendingDialog=false}, onSave={person,amount,date,note,dueDate -> vm.addLending(context,person,amount,date,note,dueDate); showPremiumLendingDialog=false})
    }
}

@Composable
fun QuickActionCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Card(onClick=onClick, modifier=modifier.height(65.dp), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface), elevation=CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal=16.dp), verticalAlignment=Alignment.CenterVertically) { Surface(Modifier.size(38.dp),RoundedCornerShape(10.dp),color=color.copy(alpha=.15f)) { Box(contentAlignment=Alignment.Center) { Icon(icon,null,tint=color,modifier=Modifier.size(20.dp)) } }; Spacer(Modifier.width(12.dp)); Text(title,fontSize=14.sp,fontWeight=FontWeight.Bold) }
    }
}
