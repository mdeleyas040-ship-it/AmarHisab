package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun PremiumLoanSelectorItem(
    loan: LoanAccount,
    loanPayments: List<LoanPayment> = emptyList(),
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
    val total = loan.principal.coerceAtLeast(0.0)
    val remaining = (total - paid).coerceAtLeast(0.0)
    val progress = if (total > 0.0) (paid / total).coerceIn(0.0, 1.0) else 0.0
    val avatarPalette = listOf(
        Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFF06B6D4),
        Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEF4444)
    )
    val avatarColor = avatarPalette[(loan.name.hashCode() and Int.MAX_VALUE) % avatarPalette.size]

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarColor.copy(alpha = 0.90f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        loan.name.trim().firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(loan.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "মোট ঋণ: ৳${formatMoney(total)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RadioButton(selected = selected, onClick = onClick)
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("পরিশোধিত ৳${formatMoney(paid)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (remaining <= 0.0) "সম্পূর্ণ পরিশোধিত" else "বাকি ৳${formatMoney(remaining)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining <= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
            ) {
                Box(
                    Modifier.fillMaxWidth(progress.toFloat()).height(6.dp).clip(RoundedCornerShape(50)).background(avatarColor)
                )
            }
        }
    }
}

@Composable
fun PremiumLoanSelector(
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment> = emptyList(),
    selectedLoanId: Long? = null,
    onLoanSelected: (Long) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val filteredLoans = loans.filter { it.name.contains(query.trim(), ignoreCase = true) }

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("খুঁজুন নাম দিয়ে...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(430.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(filteredLoans, key = { it.id }) { loan ->
                PremiumLoanSelectorItem(
                    loan = loan,
                    loanPayments = loanPayments,
                    selected = selectedLoanId == loan.id,
                    onClick = { onLoanSelected(loan.id) }
                )
            }
        }
    }
}
