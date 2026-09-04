package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun PremiumDebtDashboard(
    mode: Int,
    onModeChange: (Int) -> Unit,
    loanReceived: Double,
    loanPaid: Double,
    loanRemaining: Double,
    moneyLent: Double,
    moneyReturned: Double,
    moneyToReceive: Double,
    onAddLoan: () -> Unit,
    onAddLending: () -> Unit
) {
    val loanMode = mode == 0
    val primary = if (loanMode) Color(0xFF0B9A4A) else Color(0xFF18A990)
    val balance = if (loanMode) loanRemaining else moneyToReceive
    val totalLabel = if (loanMode) "মোট নেওয়া" else "মোট ধার"
    val total = if (loanMode) loanReceived else moneyLent
    val paidLabel = if (loanMode) "পরিশোধ" else "ফেরত পেলাম"
    val paid = if (loanMode) loanPaid else moneyReturned
    val remainingLabel = if (loanMode) "বাকি" else "পাওনা"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(52.dp), RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .12f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (loanMode) Icons.Default.CreditCard else Icons.Default.Handshake, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (loanMode) "LOAN BALANCE" else "LENDING BALANCE", color = Color.White.copy(alpha = .72f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                        Text(if (loanMode) "বর্তমান বাকি ঋণ" else "বর্তমান পাওনা", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .10f)) {
                    Text(if (loanMode) "ঋণ" else "ধার", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("৳${formatMoney(balance)}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumDebtMiniStat(Modifier.weight(1f), Icons.Default.AccountBalanceWallet, totalLabel, total)
                PremiumDebtMiniStat(Modifier.weight(1f), Icons.Default.CheckCircle, paidLabel, paid)
                PremiumDebtMiniStat(Modifier.weight(1f), Icons.Default.Schedule, remainingLabel, balance)
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(onClick = { onModeChange(0) }, Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (loanMode) primary else MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(Icons.Default.CreditCard, null); Spacer(Modifier.width(8.dp)); Text("আমার ঋণ", fontWeight = FontWeight.Bold)
        }
        Button(onClick = { onModeChange(1) }, Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (!loanMode) primary else MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(Icons.Default.Handshake, null); Spacer(Modifier.width(8.dp)); Text("ধার দিয়েছি", fontWeight = FontWeight.Bold)
        }
    }

    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        PremiumDebtPlainStat(Icons.Default.CreditCard, totalLabel, total, if (loanMode) Blue else Blue)
        PremiumDebtPlainStat(Icons.Default.CheckCircle, paidLabel, paid, IncomeGreen)
        PremiumDebtPlainStat(Icons.Default.Schedule, remainingLabel, balance, ExpenseRed)
    }

    Spacer(Modifier.height(8.dp))
    Button(
        onClick = if (loanMode) onAddLoan else onAddLending,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = primary)
    ) {
        Text("＋ ${if (loanMode) "ঋণ যোগ করুন" else "ধার দিন"}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PremiumDebtMiniStat(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, amount: Double) {
    Surface(modifier.height(74.dp), shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .10f)) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.White.copy(alpha = .82f), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text(label, color = Color.White.copy(alpha = .76f), fontSize = 10.sp, maxLines = 1) }
            Text("৳${formatMoney(amount)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun PremiumDebtPlainStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, amount: Double, tint: Color) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.widthIn(min = 82.dp).padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2) }
        Spacer(Modifier.height(4.dp)); Text("৳${formatMoney(amount)}", color = tint, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold); Text("BDT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
