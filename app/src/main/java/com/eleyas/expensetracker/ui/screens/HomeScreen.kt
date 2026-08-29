package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.eleyas.expensetracker.model.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.eleyas.expensetracker.*
import com.eleyas.expensetracker.ui.components.HomeSummaryRow
import com.eleyas.expensetracker.ui.components.StatMiniBox
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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
    transactions: List<Transaction> = emptyList(),
    household: Household? = null,
    onFamilyClick: () -> Unit = {},
    onDailyTipClick: () -> Unit = {}
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var serverNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        firestore.collection("config").document("app_notice").addSnapshotListener { snapshot, _ ->
            serverNotice = snapshot?.getString("message")?.takeIf { it.isNotBlank() }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (serverNotice != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.1f))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Blue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(serverNotice!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue)
                    }
                }
            }
        }
        
        

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, shape = RoundedCornerShape(CardRadius), ambientColor = Color.Black, spotColor = AccentGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(CardRadius),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181B21)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E222A), Color(0xFF12141A))
                            )
                        )
                ) {
                    Column(Modifier.fillMaxWidth().padding(CardPadding)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text("মোট ব্যালেন্স", color = AccentGreen.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("৳ ${formatMoney(balance)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                Text("বাংলাদেশি টাকা (BDT)", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = AccentGreen.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Mini Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatMiniBox(Modifier.weight(1f), "আয়", totalIncome, IncomeGreen)
                            StatMiniBox(Modifier.weight(1f), "খরচ", totalExpense, ExpenseRed)
                            StatMiniBox(Modifier.weight(1f), "বাড়িতে", totalHome, Blue)
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        // Birthday Countdown inside Balance Card
                        BirthdayCountdownCard(
                            userId = currentUserId, 
                            currentBirthday = birthday,
                            onBirthdaySet = { onBirthdayChange(it) },
                            isCompact = true
                        )
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wallets) { wallet ->
                    Card(
                        onClick = { onWalletClick(wallet) },
                        modifier = Modifier.width(150.dp).height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(wallet.color.toLong() and 0xFFFFFFFFL))
                    ) {
                        Column(Modifier.padding(14.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
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
            Text(
                "দ্রুত অ্যাকশন",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val actions = listOf(
                    Triple(Icons.Default.AddCircle, "আয় যোগ", IncomeGreen) to onAddIncome,
                    Triple(Icons.Default.RemoveCircle, "খরচ যোগ", ExpenseRed) to onAddExpense,
                    Triple(Icons.Default.Home, "বাড়িতে পাঠান", Blue) to onAddHome,
                    Triple(Icons.Default.ReceiptLong, "বাড়ির খরচ", Color(0xFFF59E0B)) to onAddHomeExpense,
                    Triple(Icons.Default.Mic, "ভয়েস এন্ট্রি", Color(0xFF9C27B0)) to onVoiceClick,
                    Triple(Icons.Default.ShoppingCart, "বাজারের ফর্দ", AccentGreen) to onShoppingList
                )

                // Render in 2 columns
                for (i in actions.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("বাড়ির হিসাব", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(10.dp))
                    HomeSummaryRow("বাড়িতে পাঠানো", totalHome, Blue)
                    HomeSummaryRow("বাড়ির খরচ", totalHomeExpense, Color(0xFFF59E0B))
                    HorizontalDivider(Modifier.padding(vertical = 7.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    HomeSummaryRow("বাড়িতে অবশিষ্ট", homeBalance, IncomeGreen)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("আমার ঋণ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("৳${formatMoney(loanRemaining)}", color = ExpenseRed, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("বাকি আছে", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("আমার পাওনা", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("৳${formatMoney(moneyToReceive)}", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("ফেরত পাবো", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
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
    Card(
        onClick = onClick,
        modifier = modifier.height(65.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
