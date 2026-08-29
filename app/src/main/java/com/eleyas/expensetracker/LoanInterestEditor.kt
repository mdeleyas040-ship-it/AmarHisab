package com.eleyas.expensetracker

import com.eleyas.expensetracker.model.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Interest editor.
 *
 * আলাদা Interest screen/button নয়।
 * এটি পরে existing Loan detail থেকে খোলা হবে।
 */
@Composable
fun LoanInterestEditor(
    loanName: String,
    existing: LoanInterestTerms?,
    onDismiss: () -> Unit,
    onSave: (LoanInterestTerms) -> Unit,
    loanId: Long
) {
    var rateText by remember {
        mutableStateOf(
            if ((existing?.interestRate ?: 0.0) > 0.0)
                existing?.interestRate.toString()
            else
                ""
        )
    }

    var totalInterestText by remember {
        mutableStateOf(
            if ((existing?.totalInterest ?: 0.0) > 0.0)
                existing?.totalInterest.toString()
            else
                ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ঋণের সুদের তথ্য",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = loanName.ifBlank { "ঋণ" },
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "সুদের তথ্য এখানে আলাদা থাকবে। মূল ঋণের টাকা পরিবর্তন হবে না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("সুদের হার (%)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalInterestText,
                    onValueChange = { totalInterestText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("মোট সুদ") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val totalInterest =
                        totalInterestText.toDoubleOrNull() ?: 0.0

                    onSave(
                        LoanInterestTerms(
                            loanId = loanId,
                            interestRate = rate.coerceAtLeast(0.0),
                            totalInterest = totalInterest.coerceAtLeast(0.0),
                            interestType = "fixed"
                        )
                    )
                }
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("বাতিল")
            }
        }
    )
}