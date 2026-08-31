package com.eleyas.expensetracker.model

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Amar Hisab — আলাদা Loan Interest module.
 *
 * গুরুত্বপূর্ণ:
 * - LoanPayment = মূল ঋণ/Principal পরিশোধ
 * - LoanInterestPayment = শুধু সুদ/Interest
 * - Interest কখনো Principal balance কমায় না
 * - পুরোনো LoanPayment data অপরিবর্তিত থাকে
 */

data class LoanInterestPayment(
    val id: Long,
    val loanId: Long,
    val amount: Double,
    val date: String,
    val note: String
)

fun saveLoanInterestPayments(
    prefs: SharedPreferences,
    payments: List<LoanInterestPayment>
) {
    val array = JSONArray()

    payments.forEach { payment ->
        array.put(
            JSONObject().apply {
                put("id", payment.id)
                put("loanId", payment.loanId)
                put("amount", payment.amount)
                put("date", payment.date)
                put("note", payment.note)
            }
        )
    }

    prefs.edit()
        .putString("loanInterestPayments", array.toString())
        .apply()
}

fun loadLoanInterestPayments(
    prefs: SharedPreferences
): List<LoanInterestPayment> {
    val saved = prefs.getString("loanInterestPayments", null)
        ?: return emptyList()

    return try {
        val array = JSONArray(saved)

        List(array.length()) { index ->
            val obj = array.getJSONObject(index)

            LoanInterestPayment(
                id = obj.optLong("id"),
                loanId = obj.optLong("loanId"),
                amount = obj.optDouble("amount"),
                date = obj.optString("date"),
                note = obj.optString("note")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun totalLoanInterest(
    payments: List<LoanInterestPayment>
): Double = payments.sumOf { it.amount }

fun loanInterestFor(
    payments: List<LoanInterestPayment>,
    loanId: Long
): Double = payments
    .filter { it.loanId == loanId }
    .sumOf { it.amount }

@Composable
fun LoanInterestDialog(
    loanName: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: String, note: String) -> Unit
) {
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("") }

    var date by remember {
        mutableStateOf(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )
    }

    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📈 সুদ / Interest",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = loanName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "শুধু সুদের টাকা এখানে লিখুন। এটি মূল ঋণের বাকি কমাবে না।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("সুদের টাকা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()

                    if (amount != null && amount > 0.0) {
                        onSave(
                            amount,
                            date,
                            note.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text("সুদ সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}