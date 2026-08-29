package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.SplitBillGroup
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun SplitBillDetailDialog(
    split: SplitBillGroup,
    onDismiss: () -> Unit,
    onDelete: (SplitBillGroup) -> Unit
) {
    val perPerson = if (split.members.isNotEmpty()) split.totalAmount / split.members.size else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(split.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("তারিখ: ${split.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("মোট খরচ: ৳${formatMoney(split.totalAmount)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("পরিশোধ করেছেন: ${split.paidBy}", fontSize = 13.sp)
                
                Spacer(Modifier.height(4.dp))
                Text("সদস্য প্রতি অংশ (Share): ৳${formatMoney(perPerson)}", fontWeight = FontWeight.Bold, color = Green)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Text("সদস্যদের হিসাব বিবরণী:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                split.members.forEach { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• $member", fontSize = 14.sp)
                        Text("৳${formatMoney(perPerson)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDelete(split) },
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) {
                Text("মুছে ফেলুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}
