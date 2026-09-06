package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun PremiumDebtSummary(
    loanRemaining: Double,
    moneyToReceive: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111613)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Text(
                text = "💳 আমার হিসাব",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                DebtSummaryItem(
                    modifier = Modifier.weight(1f),
                    title = "আমার ঋণ",
                    subtitle = "বাকি আছে",
                    amount = loanRemaining,
                    amountColor = Color(0xFFFF4055),
                    backgroundColor = Color(0xFF401720)
                )

                DebtSummaryItem(
                    modifier = Modifier.weight(1f),
                    title = "আমার পাওনা",
                    subtitle = "ফেরত পাবো",
                    amount = moneyToReceive,
                    amountColor = Color(0xFF159BFF),
                    backgroundColor = Color(0xFF102C48)
                )
            }
        }
    }
}

@Composable
private fun DebtSummaryItem(
    modifier: Modifier,
    title: String,
    subtitle: String,
    amount: Double,
    amountColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp
                ),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 10.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "৳${formatMoney(amount)}",
                color = amountColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}