package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Household
import com.eleyas.expensetracker.ui.theme.AccentGreen
import com.eleyas.expensetracker.ui.theme.ExpenseRed

@Composable
fun FamilyDialog(
    household: Household?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onJoin: (String) -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    var householdName by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }
    var confirmLeave by remember { mutableStateOf(false) }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("পরিবার ছাড়বেন?") },
            text = { Text("Leave করলে পরিবারের shared home লেনদেন আপনার এখানে আর দেখা যাবে না।") },
            confirmButton = {
                Button(onClick = { confirmLeave = false; onLeave() }, colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)) { Text("ছাড়ুন") }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("বাতিল") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (household == null) "পরিবারের হিসাব" else "🏠 ${household.name}") },
        text = {
            if (household == null) {
                Column {
                    Text(
                        "পরিবারের সবাই মিলে Home আয় ও খরচ একসাথে ট্র্যাক করুন। নতুন পারিবারিক হিসাব তৈরি করুন অথবা Code দিয়ে Join করুন।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = householdName,
                        onValueChange = { householdName = it },
                        label = { Text("পরিবারের নাম (যেমন: আমাদের বাসা)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { if (householdName.isNotBlank()) onCreate(householdName) },
                        enabled = !busy && householdName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        if (busy) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        else Text("নতুন পারিবারিক হিসাব তৈরি করুন")
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it.uppercase().take(6) },
                        label = { Text("Join Code (৬ অক্ষর)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { if (joinCode.length == 6) onJoin(joinCode) },
                        enabled = !busy && joinCode.length == 6,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Code দিয়ে Join করুন")
                    }
                }
            } else {
                Column {
                    Text("Join Code", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            household.code,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = AccentGreen
                        )
                        Spacer(Modifier.width(10.dp))
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Amar Hisab Household Code", household.code))
                            android.widget.Toast.makeText(context, "📋 Code কপি হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
                        }) { Text("কপি") }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("সদস্য (${household.members.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    household.members.forEach { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = AccentGreen.copy(alpha = 0.15f)) {
                                Text(
                                    (member.name.ifBlank { "সদস্য" }).take(1).uppercase(),
                                    modifier = Modifier.padding(6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(member.name.ifBlank { "সদস্য" }, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                if (member.uid == household.createdBy) {
                                    Text("Admin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (household.members.size < 2) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "উপরের Code পরিবারের অন্যদের পাঠান — তারা Join করলেই Home লেনদেন একসাথে দেখা যাবে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (household != null) {
                TextButton(
                    onClick = { confirmLeave = true },
                    enabled = !busy,
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text(if (busy) "অপেক্ষা করুন..." else "পরিবার ছাড়ুন")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("বন্ধ করুন") }
            }
        },
        dismissButton = {}
    )
}