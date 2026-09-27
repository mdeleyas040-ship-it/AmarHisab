package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.model.PersonProfile
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.util.persistPersonProfilePhoto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PremiumLoanDialog(
    onDismiss: () -> Unit,
    existingLoan: LoanAccount? = null,
    existingNames: List<String> = emptyList(),
    people: List<PersonProfile> = emptyList(),
    onProfilePhotoSaved: (PersonProfile) -> Unit = {},
    onSave: (String, String, Double, Double, String, String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var sourceType by remember { mutableStateOf(existingLoan?.sourceType ?: "bank") }
    val accent = if (sourceType == "bank") Color(0xFF1688F7) else Color(0xFF0FAF93)
    val accentDark = if (sourceType == "bank") Color(0xFF1767B6) else Color(0xFF078B78)
    val softSurface = accent.copy(alpha = 0.075f)

    var name by remember { mutableStateOf(existingLoan?.name ?: "") }
    var principal by remember { mutableStateOf(existingLoan?.principal?.let(::formatMoney) ?: "") }
    var installment by remember { mutableStateOf(existingLoan?.monthlyInstallment?.let(::formatMoney) ?: "") }
    var date by remember { mutableStateOf(existingLoan?.startDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember { mutableStateOf(existingLoan?.dueDate ?: "") }
    var note by remember { mutableStateOf(existingLoan?.note ?: "") }
    var sourceMenu by remember { mutableStateOf(false) }
    var nameMenu by remember { mutableStateOf(false) }
    var selectedPersonId by remember { mutableStateOf(existingLoan?.personId) }
    var showPeople by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && sourceType == "person" && name.isNotBlank()) {
            val path = persistPersonProfilePhoto(context, uri)
            if (path != null) {
                val id = selectedPersonId ?: java.util.UUID.randomUUID().toString()
                selectedPersonId = id
                onProfilePhotoSaved(PersonProfile(id = id, name = name.trim(), photoUri = path))
            }
        }
    }

    fun openDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        if (currentDate.isNotBlank() && currentDate != "সিলেক্ট করুন") {
            try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentDate)?.let { calendar.time = it } } catch (_: Exception) { }
        }
        DatePickerDialog(context, { _, year, month, day -> onDateSelected("%02d/%02d/%04d".format(day, month + 1, year)) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = 680.dp),
                contentPadding = PaddingValues(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 22.dp, bottomEnd = 22.dp),
                        colors = CardDefaults.cardColors(containerColor = accentDark),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(64.dp).background(Color.White.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(33.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (sourceType == "bank") "BANK LOAN" else "PERSONAL LOAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Color.White.copy(alpha = .72f))
                                Text(if (existingLoan == null) { if (sourceType == "bank") "ব্যাংক ঋণ যোগ করুন" else "ব্যক্তিগত ঋণ যোগ করুন" } else "ঋণ এডিট করুন", fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text(if (sourceType == "bank") "ব্যাংক থেকে নেওয়া ঋণের তথ্য" else "ব্যক্তির কাছ থেকে নেওয়া ঋণের তথ্য", fontSize = 10.sp, color = Color.White.copy(alpha = .72f))
                            }
                            Surface(onClick = onDismiss, Modifier.size(40.dp), RoundedCornerShape(13.dp), color = Color.White.copy(alpha = .10f)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "বন্ধ করুন", tint = Color.White, modifier = Modifier.size(24.dp)) }
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("ঋণের ধরন", fontSize = 10.sp, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { sourceMenu = true }, Modifier.fillMaxWidth().height(62.dp), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.6.dp, accent), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).background(accent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) { Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent, modifier = Modifier.size(20.dp)) }
                                    Spacer(Modifier.width(9.dp))
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                        Text(if (sourceType == "bank") "ব্যাংক ঋণ" else "ব্যক্তিগত ঋণ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(if (sourceType == "bank") "ব্যাংক থেকে" else "ব্যক্তির কাছ থেকে", fontSize = 9.sp, color = scheme.onSurfaceVariant)
                                    }
                                    Text("⌄", fontSize = 20.sp, color = accent)
                                }
                            }
                            DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                                DropdownMenuItem(text = { Text("ব্যাংক ঋণ") }, leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF1688F7)) }, onClick = { sourceType = "bank"; sourceMenu = false })
                                DropdownMenuItem(text = { Text("ব্যক্তিগত ঋণ") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF0FAF93)) }, onClick = { sourceType = "person"; sourceMenu = false })
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it; if (sourceType == "person") { selectedPersonId = people.firstOrNull { p -> p.name.equals(it.trim(), true) }?.id; showPeople = true } }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (sourceType == "bank") "ব্যাংকের নাম" else "ব্যক্তির নাম") }, placeholder = { Text(if (sourceType == "bank") "ব্যাংকের নাম লিখুন" else "ব্যক্তির নাম লিখুন") }, leadingIcon = { Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent) }, trailingIcon = if (existingLoan == null && existingNames.isNotEmpty()) ({ TextButton(onClick = { nameMenu = true }) { Text("আগের নাম", color = accent, fontSize = 10.sp) } }) else null, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent))
                        DropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) { existingNames.filter { it.isNotBlank() }.distinct().forEach { existingName -> DropdownMenuItem(text = { Text(existingName) }, onClick = { name = existingName; nameMenu = false }) } }
                        if (sourceType == "person") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                OutlinedButton(onClick = { showPeople = !showPeople }, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Person, null, Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("আগের ব্যক্তি", fontSize = 10.sp) }
                                OutlinedButton(onClick = { photoPicker.launch("image/*") }, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.AddAPhoto, null, Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("ছবি যোগ", fontSize = 10.sp) }
                            }
                            if (showPeople && people.isNotEmpty()) {
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = softSurface)) { Column(Modifier.padding(6.dp)) { people.filter { name.isBlank() || it.name.contains(name, true) }.take(5).forEach { p -> TextButton(onClick = { name = p.name; selectedPersonId = p.id; showPeople = false }, Modifier.fillMaxWidth()) { Text(p.name, Modifier.fillMaxWidth()) } } } }
                            }
                        }
                    }
                }

                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .10f)), border = BorderStroke(1.dp, accent.copy(alpha = .20f))) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("ঋণের পরিমাণ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                            OutlinedTextField(value = principal, onValueChange = { principal = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("মোট ঋণের টাকা") }, leadingIcon = { Text("৳", color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent))
                            OutlinedTextField(value = installment, onValueChange = { installment = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("মাসিক কিস্তি (না থাকলে 0)") }, leadingIcon = { Icon(Icons.Default.Save, null, tint = accent, modifier = Modifier.size(19.dp)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent))
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanDateSelector("শুরুর তারিখ", date, accent, Modifier.weight(1f)) { openDatePicker(date) { date = it } }
                        LoanDateSelector("পরিশোধের তারিখ", dueDate.ifBlank { "সিলেক্ট করুন" }, accent, Modifier.weight(1f)) { openDatePicker(dueDate.ifBlank { date }) { dueDate = it } }
                    }
                }

                item {
                    OutlinedTextField(value = note, onValueChange = { note = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("নোট (ঐচ্ছিক)") }, placeholder = { Text("ঐচ্ছিক অতিরিক্ত তথ্য") }, leadingIcon = { Icon(Icons.Default.Edit, null, tint = accent) }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent))
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        OutlinedButton(onClick = onDismiss, Modifier.weight(.78f).height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("বাতিল", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        Button(onClick = {
                            val amount = principal.replace(",", "").trim().toDoubleOrNull()
                            val monthly = installment.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                            if (name.isBlank()) Toast.makeText(context, "নাম দিন।", Toast.LENGTH_SHORT).show()
                            else if (amount == null || amount <= 0.0 || monthly < 0.0) Toast.makeText(context, "সঠিক ঋণের টাকা দিন।", Toast.LENGTH_SHORT).show()
                            else onSave(name.trim(), sourceType, amount, monthly, date, note.trim(), dueDate.takeIf { it.isNotBlank() }, selectedPersonId.takeIf { sourceType == "person" })
                        }, Modifier.weight(1.22f).height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)) {
                            Icon(Icons.Default.Save, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("ঋণ সংরক্ষণ", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 1.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, tint = accent.copy(alpha = .82f), Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("আপনার ঋণ সংক্রান্ত তথ্য নিরাপদ এবং সুরক্ষিত", fontSize = 8.sp, color = scheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDateSelector(label: String, value: String, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(70.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.outline.copy(alpha = .75f)), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = accent, Modifier.size(20.dp)); Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(2.dp)); Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
