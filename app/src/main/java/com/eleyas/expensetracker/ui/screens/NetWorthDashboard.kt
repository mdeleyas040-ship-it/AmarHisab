package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney

/**
 * সব সম্পদ (ব্যাংক/ক্যাশ ব্যালেন্স, বাড়ির টাকা, পাওনা) যোগ করে
 * সব দায় (ঋণ বাকি) বাদ দিয়ে ব্যবহারকারীর প্রকৃত "মোট সম্পদ" (Net Worth)
 * এক নজরে দেখানোর জন্য এই ড্যাশবোর্ড কার্ড।
 */
@Composable
fun NetWorthDashboard(
    cashBalance: Double,
    homeBalance: Double,
    moneyToReceive: Double,
    loanRemaining: Double
) {
    val totalAssets = cashBalance + homeBalance + moneyToReceive
    val totalLiabilities = loanRemaining
    val netWorth = totalAssets - totalLiabilities
    val isPositive = netWorth >= 0.0

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
                            Color(0xFF0E2A1B),
                            Color(0xFF102315),
                            Color(0xFF0B1A10)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF35D98A) else Color(0xFFFF6B6B),
                            modifier = Modifier.width(20.dp).height(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "মোট সম্পদ (Net Worth)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            if (isPositive) "স্বাস্থ্যকর" else "ঋণাত্মক",
                            color = if (isPositive) Color(0xFF35D98A) else Color(0xFFFF6B6B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "৳${formatMoney(netWorth)}",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NetWorthMiniStat(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "মোট সম্পদ",
                        amount = totalAssets,
                        amountColor = Color(0xFF35D98A)
                    )
                    NetWorthMiniStat(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CreditCard,
                        label = "মোট দায় (ঋণ)",
                        amount = totalLiabilities,
                        amountColor = Color(0xFFFF6B6B)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NetWorthBreakdownItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "ক্যাশ/ব্যাংক",
                        amount = cashBalance
                    )
                    NetWorthBreakdownItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.HomeWork,
                        label = "বাড়িতে",
                        amount = homeBalance
                    )
                    NetWorthBreakdownItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Handshake,
                        label = "পাওনা",
                        amount = moneyToReceive
                    )
                }

                // Match the vertical space of the other swipe cards.
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NetWorthMiniStat(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    amount: Double,
    amountColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.width(15.dp).height(15.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "৳${formatMoney(amount)}",
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NetWorthBreakdownItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    amount: Double
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.width(13.dp).height(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            "৳${formatMoney(amount)}",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
