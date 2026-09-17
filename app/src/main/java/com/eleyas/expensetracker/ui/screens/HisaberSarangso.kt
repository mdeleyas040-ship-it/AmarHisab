package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.ui.theme.Blue
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.ui.theme.IncomeGreen
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun HisaberSarangso(
    totalIncome: Double,
    totalExpense: Double,
    moneyToReceive: Double,
    loanRemaining: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "হিসাবের সারাংশ",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SarangsoMiniCard(
                modifier = Modifier.weight(1f),
                title = "আয়",
                amount = totalIncome,
                icon = Icons.Default.ArrowUpward,
                iconColor = IncomeGreen
            )

            SarangsoMiniCard(
                modifier = Modifier.weight(1f),
                title = "খরচ",
                amount = totalExpense,
                icon = Icons.Default.ArrowDownward,
                iconColor = ExpenseRed
            )

            SarangsoMiniCard(
                modifier = Modifier.weight(1f),
                title = "পাওনা",
                amount = moneyToReceive,
                icon = Icons.Default.SwapHoriz,
                iconColor = Blue
            )

            SarangsoMiniCard(
                modifier = Modifier.weight(1f),
                title = "ধার",
                amount = loanRemaining,
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
private fun SarangsoMiniCard(
    modifier: Modifier,
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(9.dp),
                color = iconColor.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(6.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "৳${formatMoney(amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = iconColor,
                maxLines = 1
            )
        }
    }
}