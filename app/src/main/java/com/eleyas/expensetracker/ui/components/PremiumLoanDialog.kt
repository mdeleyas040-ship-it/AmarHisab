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
import androidx.compose.ui.platform.LocalConfiguration
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
    val dialogMaxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp
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
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentDate)?.let { calendar.time = it }
            } catch (_: Exception) { }
        }
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateSelected("%02d/%02d/%04d".format(day, month + 1, year)) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = dialogMaxHeight)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dialogMaxHeight),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = accentDark),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, top = 18.dp, end = 10.dp, bottom = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(Color.White.copy(alpha = 0.14f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (sourceType == "bank") "BANK LOAN" else "PERSONAL LOAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (existingLoan == null) {
                                        if (sourceType == "bank") "ব্যাংক ঋণ যোগ করুন" else "ব্যক্তিগত ঋণ যোগ করুন"
                                    } else "ঋণ এডিট করুন",
                                    fontSize = 23.sp,
                                    lineHeight = 29.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (sourceType == "bank") "ব্যাংক থেকে নেওয়া ঋণের তথ্য" else "ব্যক্তির কাছ থেকে নেওয়া ঋণের তথ্য",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }
                            Surface(
                                onClick = onDismiss,
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.10f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, "বন্ধ করুন", tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text("ঋণের ধরন", fontSize = 11.sp, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { sourceMenu = true },
                                modifier = Modifier.fillMaxWidth().height(68.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.7.dp, accent),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).background(accent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                        Text(if (sourceType == "bank") "ব্যাংক ঋণ" else "ব্যক্তিগত ঋণ", fontWeight = FontWeight.Bold)
                                        Text(if (sourceType == "bank") "ব্যাংক থেকে" else "ব্যক্তির কাছ থেকে", fontSize = 10.sp, color = scheme.onSurfaceVariant)
                                    }
                                    Text("⌄", fontSize = 21.sp, color = accent)
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
                    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (sourceType == "person") {
                                    selectedPersonId = people.firstOrNull { p -> p.name.equals(it.trim(), ignoreCase = true) }?.id
                                    showPeople = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(if (sourceType == "bank") "ব্যাংকের নাম" else "ব্যক্তির নাম") },
                            placeholder = { Text(if (sourceType == "bank") "ব্যাংকের নাম লিখুন" else "ব্যক্তির নাম লিখুন") },
                            leadingIcon = { Icon(if (sourceType == "bank") Icons.Default.AccountBalance else Icons.Default.Person, null, tint = accent) },
                            trailingIcon = if (existingLoan == null && existingNames.isNotEmpty()) ({ TextButton(onClick = { nameMenu = true }) { Text("আগের নাম", color = accent, fontSize = 11.sp) } }) else null,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent)
                        )
                        DropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) {
                            existingNames.filter { it.isNotBlank() }.distinct().forEach { existingName ->
                                DropdownMenuItem(text = { Text(existingName) }, onClick = { name = existingName; nameMenu = false })
                            }
                        }
                        if (sourceType == "person") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showPeople = !showPeople }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("আগের ব্যক্তি", fontSize = 11.sp)
                                }
                                OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) {
                                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("ছবি যোগ", fontSize = 11.sp)
                                }
                            }
                            if (showPeople && people.isNotEmpty()) {
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = softSurface)) {
                                    Column(Modifier.padding(7.dp)) {
                                        people.filter { name.isBlank() || it.name.contains(name, true) }.take(5).forEach { p ->
                                            TextButton(onClick = { name = p.name; selectedPersonId = p.id; showPeople = false }, modifier = Modifier.fillMaxWidth()) { Text(p.name, modifier = Modifier.fillMaxWidth()) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
                        border = BorderStroke(1.dp, accent.copy(alpha = .20f))
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("ঋণের পরিমাণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                            OutlinedTextField(
                                value = principal,
                                onValueChange = { principal = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("মোট ঋণের টাকা") },
                                leadingIcon = { Text("৳", color = accent, fontSize = 21.sp, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent)
                            )
                            OutlinedTextField(
                                value = installment,
                                onValueChange = { installment = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("মাসিক কিস্তি (না থাকলে 0)") },
                                leadingIcon = { Icon(Icons.Default.Save, null, tint = accent, modifier = Modifier.size(20.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent)
                            )
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        LoanDateSelector("শুরুর তারিখ", date, accent, Modifier.weight(1f)) { openDatePicker(date) { date = it } }
                        LoanDateSelector("পরিশোধের তারিখ", dueDate.ifBlank { "সিলেক্ট করুন" }, accent, Modifier.weight(1f)) { openDatePicker(dueDate.ifBlank { date }) { dueDate = it } }
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        label = { Text("নোট (ঐচ্ছিক)") },
                        placeholder = { Text("ঐচ্ছিক অতিরিক্ত তথ্য") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = accent) },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent)
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(.72f).height(58.dp), shape = RoundedCornerShape(18.dp)) {
                            Text("বাতিল", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val amount = principal.replace(",", "").trim().toDoubleOrNull()
                                val monthly = installment.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                                if (name.isBlank()) {
                                    Toast.makeText(context, "নাম দিন।", Toast.LENGTH_SHORT).show()
                                } else if (amount == null || amount <= 0.0 || monthly < 0.0) {
                                    Toast.makeText(context, "সঠিক ঋণের টাকা দিন।", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSave(name.trim(), sourceType, amount, monthly, date, note.trim(), dueDate.takeIf { it.isNotBlank() }, selectedPersonId.takeIf { sourceType == "person" })
                                }
                            },
                            modifier = Modifier.weight(1.28f).height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(6.dp)); Text("ঋণ সংরক্ষণ", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 1.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, tint = accent.copy(alpha = .82f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("আপনার ঋণ সংক্রান্ত তথ্য নিরাপদ এবং সুরক্ষিত", fontSize = 9.sp, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanDateSelector(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.outline.copy(alpha = .75f)),
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = accent, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
