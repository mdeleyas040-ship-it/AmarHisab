package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.LoanPayment
import com.eleyas.expensetracker.util.displayLoanDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LoanPaymentEditDialog(
    payment: LoanPayment,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    val context = LocalContext.current

    var amount by remember(payment.id) {
        mutableStateOf(payment.amount.toString())
    }

    var date by remember(payment.id) {
        mutableStateOf(payment.date)
    }

    var note by remember(payment.id) {
        mutableStateOf(payment.note)
    }

    var amountError by remember(payment.id) {
        mutableStateOf(false)
    }

    val datePicker = remember(payment.id, date) {
        {
            val calendar = Calendar.getInstance()

            val parsed =
                runCatching {
                    SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    ).parse(date)
                }.getOrNull()

            if (parsed != null) {
                calendar.time = parsed
            }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    date =
                        String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                        )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Loan payment এডিট",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text("পরিশোধের পরিমাণ")
                    },
                    leadingIcon = {
                        Text(
                            "৳",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    }
                )

                if (amountError) {
                    Text(
                        "সঠিক পরিমাণ দিন।",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 4.dp
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = displayLoanDate(date),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text("তারিখ")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = datePicker
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "তারিখ নির্বাচন"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text("নোট")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount =
                        amount
                            .replace(",", "")
                            .trim()
                            .toDoubleOrNull()

                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = true
                    } else {
                        onSave(
                            parsedAmount,
                            date,
                            note
                        )
                    }
                }
            ) {
                Text("সংরক্ষণ করুন")
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
