package com.eleyas.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.AchievementBadgesCard
import com.eleyas.expensetracker.ui.components.MonthlyBalanceForecastCard
import com.eleyas.expensetracker.ui.components.SmartReminderCard
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*
import com.eleyas.expensetracker.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
    onScratchpad: () -> Unit = {},
    onVehicle: () -> Unit = {},
    transactions: List<Transaction> = emptyList(),
    categoryBudgets: List<CategoryBudget> = emptyList(),
    usdToBdt: Double = 0.0,
    usdToMvr: Double = 0.0,
    household: Household? = null,
    onFamilyClick: () -> Unit = {},
    onDailyTipClick: () -> Unit = {},
    onReminderClick: (SmartReminder) -> Unit = {}
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    val appViewModel: MainViewModel = viewModel()
    var showHomeMoneyFlow by remember { mutableStateOf(false) }
    var serverNotice by remember { mutableStateOf<String?>(null) }
    var balanceCardPage by remember { mutableIntStateOf(0) }

    val smartReminders = remember(transactions) {
        SmartReminderManager.getTransactionReminders(transactions)
    }

    val achievementBadges = remember(transactions, categoryBudgets, usdToBdt, usdToMvr) {
        AchievementCalculator.evaluate(transactions, categoryBudgets, usdToBdt, usdToMvr)
    }

    LaunchedEffect(Unit) {
        firestore.collection("config")
            .document("app_notice")
            .addSnapshotListener { snapshot, _ ->
                serverNotice = snapshot
                    ?.getString("message")
                    ?.takeIf { it.isNotBlank() }
            }
    }

    BackHandler(enabled = showHomeMoneyFlow) {
        showHomeMoneyFlow = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                horizontal = ScreenHorizontalPadding,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing)
        ) {
            if (serverNotice != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Blue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(serverNotice!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue)
                        }
                    }
                }
            }

            if (smartReminders.isNotEmpty()) {
                item {
                    SmartReminderCard(reminders = smartReminders, onReminderClick = onReminderClick)
                }
            }

            item {
                SmoothSwipeableBalanceCards(
                    page = balanceCardPage,
                    onPageChange = { balanceCardPage = it },
                    first = {
                        PremiumHomeHeader(
                            balance = balance,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalHome = totalHome,
                            currentUserId = currentUserId,
                            birthday = birthday,
                            onBirthdayChange = onBirthdayChange
                        )
                    },
                    second = {
                        NetWorthDashboard(
                            cashBalance = balance,
                            homeBalance = homeBalance,
                            moneyToReceive = moneyToReceive,
                            loanRemaining = loanRemaining
                        )
                    },
                    third = {
                        PremiumHomeAccountCard(
                            totalHome = totalHome,
                            totalHomeExpense = totalHomeExpense,
                            homeBalance = homeBalance
                        )
                    }
                )
            }

            item {
                MonthlyBalanceForecastCard(
                    balance = balance,
                    transactions = transactions,
                    usdToBdt = usdToBdt,
                    usdToMvr = usdToMvr
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("আমার অ্যাকাউন্টসমূহ", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onAddWallet) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("নতুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(wallets) { wallet ->
                        Card(
                            onClick = { onWalletClick(wallet) },
                            modifier = Modifier.width(150.dp).height(100.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(wallet.color.toLong() and 0xFFFFFFFFL))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(wallet.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Column {
                                    Text("৳${formatMoney(getWalletBalance(wallet.id))}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(wallet.type, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("দ্রুত অ্যাকশন", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val actions = listOf(
                        Triple(Icons.Default.AddCircle, "আয় যোগ", IncomeGreen) to onAddIncome,
                        Triple(Icons.Default.RemoveCircle, "খরচ যোগ", ExpenseRed) to onAddExpense,
                        Triple(Icons.Default.Home, "বাড়িতে পাঠান", Blue) to onAddHome,
                        Triple(Icons.Default.ReceiptLong, "বাড়ির খরচ", Color(0xFFF59E0B)) to onAddHomeExpense,
                        Triple(Icons.Default.Mic, "ভয়েস এন্ট্রি", Color(0xFF9C27B0)) to onVoiceClick,
                        Triple(Icons.Default.ShoppingCart, "বাজারের ফর্দ", AccentGreen) to onShoppingList,
                        Triple(Icons.Default.NoteAlt, "কুইক মেমো", Color(0xFF00897B)) to onScratchpad,
                        Triple(Icons.Default.DirectionsCar, "গাড়ি ও রক্ষণাবেক্ষণ", Color(0xFF1565C0)) to onVehicle
                    )

                    for (i in actions.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = actions[i].first.first,
                                title = actions[i].first.second,
                                color = actions[i].first.third,
                                onClick = actions[i].second
                            )
                            if (i + 1 < actions.size) {
                                QuickActionCard(
                                    modifier = Modifier.weight(1f),
                                    icon = actions[i + 1].first.first,
                                    title = actions[i + 1].first.second,
                                    color = actions[i + 1].first.third,
                                    onClick = actions[i + 1].second
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                PremiumJourneyCard(currentUserId = currentUserId, transactions = transactions, loanRemaining = loanRemaining)
            }

            item {
                FinancialMemoryTimeline(
                    milestones = appViewModel.financialMilestones,
                    transactions = transactions,
                    onSave = { appViewModel.saveFinancialMilestone(context, it) },
                    onDelete = { appViewModel.deleteFinancialMilestone(context, it) }
                )
            }

            item {
                AchievementBadgesCard(badges = achievementBadges)
            }

            item {
                PremiumDebtSummary(
                    loanRemaining = loanRemaining,
                    moneyToReceive = moneyToReceive
                )
            }

            item {
                HisaberSarangso(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    moneyToReceive = moneyToReceive,
                    loanRemaining = loanRemaining
                )
            }
        }

        if (showHomeMoneyFlow) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                HomeMoneyFlowScreen(
                    entries = HomeMoneyFlow.entries(appViewModel),
                    onBack = { showHomeMoneyFlow = false }
                )
            }
        }
    }
}

@Composable
private fun SmoothSwipeableBalanceCards(
    page: Int,
    onPageChange: (Int) -> Unit,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    third: @Composable () -> Unit
) {
    val dragOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val pages = listOf(first, second, third)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(page) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val maxDrag = size.width.toFloat()
                        scope.launch {
                            dragOffset.snapTo((dragOffset.value + dragAmount).coerceIn(-maxDrag, maxDrag))
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val offset = dragOffset.value
                            val threshold = width * 0.22f
                            val nextPage = when {
                                offset <= -threshold && page < pages.lastIndex -> page + 1
                                offset >= threshold && page > 0 -> page - 1
                                else -> page
                            }
                            val targetOffset = when {
                                nextPage > page -> -width
                                nextPage < page -> width
                                else -> 0f
                            }
                            dragOffset.animateTo(targetOffset, tween(180))
                            if (nextPage != page) onPageChange(nextPage)
                            dragOffset.snapTo(0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch { dragOffset.animateTo(0f, tween(160)) }
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = -page * widthPx + dragOffset.value }
        ) {
            pages.forEach { content ->
                Box(modifier = Modifier.width(maxWidth)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun PremiumHomeAccountCard(
    totalHome: Double,
    totalHomeExpense: Double,
    homeBalance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF102A3F),
                            Color(0xFF0C2032),
                            Color(0xFF081722)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color(0xFF55B8FF), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("বাড়ির হিসাব", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.08f)) {
                        Text("বাড়িতে", color = Color(0xFF55B8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("৳${formatMoney(homeBalance)}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("বাড়িতে বর্তমানে অবশিষ্ট", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                Spacer(Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeAccountMiniStat(Modifier.weight(1f), Icons.Default.Home, "বাড়িতে পাঠানো", totalHome, Color(0xFF55B8FF))
                    HomeAccountMiniStat(Modifier.weight(1f), Icons.Default.ReceiptLong, "বাড়ির খরচ", totalHomeExpense, Color(0xFFFFB52E))
                }
                Spacer(Modifier.height(12.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.08f)) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("বাড়িতে অবশিষ্ট", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                        Text("৳${formatMoney(homeBalance)}", color = IncomeGreen, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeAccountMiniStat(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    amount: Double,
    amountColor: Color
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.08f)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, maxLines = 1)
            }
            Spacer(Modifier.height(6.dp))
            Text("৳${formatMoney(amount)}", color = amountColor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier.height(65.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
