package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.ui.theme.Blue
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.ui.theme.IncomeGreen
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun PremiumLoansDashboard(
    totalBorrowed: Double,
    totalPaid: Double,
    totalRemaining: Double,
    totalLent: Double,
    totalReturned: Double,
    totalReceivable: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Blue.copy(alpha = .12f)) {
                        Icon(Icons.Default.AccountBalance, null, Modifier.padding(10.dp), tint = Blue)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ঋণ ও ধার", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("আপনার নেওয়া ও দেওয়া টাকার সারসংক্ষেপ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PremiumLoanMetric("মোট ঋণ", totalBorrowed, Blue, Icons.Default.ArrowDownward, Modifier.weight(1f))
                    PremiumLoanMetric("পরিশোধ", totalPaid, IncomeGreen, Icons.Default.ArrowUpward, Modifier.weight(1f))
                    PremiumLoanMetric("বাকি", totalRemaining, ExpenseRed, Icons.Default.Schedule, Modifier.weight(1f))
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("ধার দেওয়া", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("৳${formatMoney(totalLent)}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Blue)
                    Text("ফেরত: ৳${formatMoney(totalReturned)}", fontSize = 11.sp, color = IncomeGreen)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("পাওনা", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("৳${formatMoney(totalReceivable)}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = ExpenseRed)
                }
            }
        }
    }
}

@Composable
private fun PremiumLoanMetric(
    label: String,
    amount: Double,
    tint: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = tint.copy(alpha = .08f)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, Modifier.size(18.dp), tint = tint)
            Spacer(Modifier.height(5.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("৳${formatMoney(amount)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = tint)
        }
    }
}
