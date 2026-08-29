package com.eleyas.expensetracker

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Loan detail card.
 *
 * এটি আলাদা Interest button নয়।
 * Existing Loan card/detail-এর ভিতরে Principal + Interest summary
 * দেখানোর জন্য তৈরি করা হয়েছে।
 *
 * MainActivity.kt-এর কোনো data model পরিবর্তন করে না।
 */
@Composable
fun LoanInterestCard(
    principal: Double,
    totalInterest: Double,
    totalPaid: Double,
    modifier: Modifier = Modifier
) {
    val totalPayable = principal + totalInterest
    val remaining = (totalPayable - totalPaid).coerceAtLeast(0.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ঋণের বিস্তারিত",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            LoanInterestRow(
                label = "মূল ঋণ",
                amount = principal
            )

            LoanInterestRow(
                label = "মোট সুদ",
                amount = totalInterest
            )

            LoanInterestRow(
                label = "মোট পরিশোধযোগ্য",
                amount = totalPayable,
                bold = true
            )

            LoanInterestRow(
                label = "পরিশোধ হয়েছে",
                amount = totalPaid
            )

            Spacer(Modifier.height(5.dp))

            LoanInterestRow(
                label = "বাকি",
                amount = remaining,
                bold = true
            )
        }
    }
}

@Composable
private fun LoanInterestRow(
    label: String,
    amount: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = "৳${"%,.2f".format(amount)}",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}