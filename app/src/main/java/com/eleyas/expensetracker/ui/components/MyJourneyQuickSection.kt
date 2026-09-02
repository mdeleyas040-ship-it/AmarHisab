package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyJourneyQuickSection(
    settings: MyJourneySettings,
    totalDebt: Double,
    transactions: List<Transaction>,
    onEdit: (MyJourneySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(false) }

    val journey = MyJourneyCalculator.journeyLabel(settings.arrivalDate)
    val averageIncome = MyJourneyCalculator.averageMonthlyIncome(transactions, months = 3)
    val averageExpense = MyJourneyCalculator.averageMonthlyExpense(transactions, months = 3)
    val monthlyDebtCapacity = (averageIncome - averageExpense).coerceAtLeast(0.0)
    val repaymentDays = MyJourneyCalculator.repaymentDays(totalDebt, averageIncome, averageExpense)
    val debtFreeDate = repaymentDays?.let { MyJourneyCalculator.debtFreeDate(it) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDetails = true },
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF202631), Color(0xFF11151B))
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🇲🇻 মালদ্বীপ আমার যাত্রা",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (settings.arrivalDate.isBlank()) "আসার তারিখ সেট করুন" else "মালদ্বীপে আছেন • $journey",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 11.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "বিস্তারিত",
                tint = Color.White.copy(alpha = 0.65f)
            )
        }
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = {
                Text("🇲🇻 মালদ্বীপ আমার যাত্রা", fontWeight = FontWeight.ExtraBold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    JourneyDetailRow("মালদ্বীপে আছেন", journey)
                    JourneyDetailRow("গড় আয় / মাস", "৳ ${formatMoney(averageIncome)}")
                    JourneyDetailRow("গড় খরচ / মাস", "৳ ${formatMoney(averageExpense)}")
                    JourneyDetailRow("ঋণে যাবে / মাস", "৳ ${formatMoney(monthlyDebtCapacity)}")
                    JourneyDetailRow("মোট বাকি ঋণ", "৳ ${formatMoney(totalDebt)}")
                    JourneyDetailRow(
                        "ঋণ শেষ হতে",
                        repaymentDays?.let { "$it দিন" } ?: "বর্তমান আয়-খরচে সম্ভব নয়"
                    )
                    debtFreeDate?.let {
                        JourneyDetailRow("সম্ভাব্য ঋণমুক্তির তারিখ", MyJourneyCalculator.formatDate(it))
                    }
                    Text(
                        "আয় ও খরচ গত ৩ মাসের transaction থেকে অটো হিসাব করা হচ্ছে।",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDetails = false
                    onEdit(settings)
                }) { Text("তারিখ Edit") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDetails = false }) {
                    Text("বন্ধ")
                }
            }
        )
    }
}

@Composable
private fun JourneyDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
