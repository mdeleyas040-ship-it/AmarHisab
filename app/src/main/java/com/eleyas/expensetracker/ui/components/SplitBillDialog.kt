package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.SplitBillGroup
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SplitBillDialog(
    onDismiss: () -> Unit,
    onSave: (SplitBillGroup) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmountStr by remember { mutableStateOf("") }
    var membersInput by remember { mutableStateOf("") } // comma separated
    var paidBy by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val membersList = remember(membersInput) {
        membersInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
    val perPersonShare = if (membersList.isNotEmpty() && totalAmount > 0) totalAmount / membersList.size else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন স্প্লিট বিল হিসাব", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ইভেন্ট বা বিষয়ের নাম (যেমন: হোটেল ডিনার)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = { totalAmountStr = it },
                    label = { Text("মোট খরচ (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = membersInput,
                    onValueChange = { membersInput = it },
                    label = { Text("সদস্যদের নাম (কমা দিয়ে লিখুন)") },
                    placeholder = { Text("রহিম, করিম, শাকিল") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("টাকা কে প্রদান করেছেন?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (membersList.isNotEmpty() && totalAmount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("হিসাব বিবরণী:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Green)
                            Spacer(Modifier.height(4.dp))
                            Text("প্রতি জনের অংশ: ৳${formatMoney(perPersonShare)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && totalAmount > 0 && membersList.isNotEmpty()) {
                        val group = SplitBillGroup(
                            title = title,
                            totalAmount = totalAmount,
                            members = membersList,
                            paidBy = paidBy.ifBlank { membersList.firstOrNull() ?: "Self" },
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            note = note
                        )
                        onSave(group)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
