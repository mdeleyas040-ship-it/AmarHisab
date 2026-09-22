package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleProfileSetupScreen(
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    var name by remember { mutableStateOf(user?.displayName.orEmpty()) }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("BDT") }
    var monthlyIncome by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val currencies = listOf("BDT", "MVR", "USD")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.size(14.dp))

                Text(
                    text = "Amar Hisab-এ স্বাগতম",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    text = "আপনার প্রোফাইলটি একবার সেটআপ করুন",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    "Google থেকে পাওয়া তথ্যের ভিত্তিতে কিছু তথ্য আগে থেকেই পূরণ করা হয়েছে।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ProfileFieldLabel("প্রয়োজনীয় তথ্য")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("নাম *") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            supportingText = { Text("এই নামটি Amar Hisab-এ আপনার প্রোফাইলে দেখাবে।") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        user?.email?.let {
            OutlinedTextField(
                value = it,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Google ইমেইল") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        ProfileFieldLabel("ঐচ্ছিক তথ্য")

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("মোবাইল নম্বর") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("দেশ") },
            leadingIcon = { Icon(Icons.Default.Public, null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currency,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("মূল মুদ্রা") },
                leadingIcon = { Icon(Icons.Default.Wallet, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                supportingText = { Text("আপনার হিসাবের প্রধান মুদ্রা নির্বাচন করুন।") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencies.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (item) {
                                        "BDT" -> "বাংলাদেশি টাকা"
                                        "MVR" -> "মালদ্বীপের রুফিয়া"
                                        else -> "মার্কিন ডলার"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            currency = item
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = monthlyIncome,
            onValueChange = { monthlyIncome = it.filter { ch -> ch.isDigit() || ch == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("মাসিক আয়") },
            leadingIcon = { Icon(Icons.Default.Savings, null) },
            supportingText = { Text("ইচ্ছা করলে পরে সেটিংস থেকেও পরিবর্তন করতে পারবেন।") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.size(2.dp))

        Button(
            onClick = {
                val cleanName = name.trim()
                if (cleanName.isBlank()) {
                    onError("নাম লিখুন।")
                    return@Button
                }

                saving = true
                com.eleyas.expensetracker.repository.FirestoreRepository.completeUserProfile(
                    name = cleanName,
                    phone = phone.trim(),
                    currency = currency,
                    monthlyIncome = monthlyIncome.toDoubleOrNull(),
                    country = country.trim(),
                    onSuccess = {
                        saving = false
                        onComplete()
                    },
                    onError = {
                        saving = false
                        onError(it)
                    }
                )
            },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(vertical = 15.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.size(10.dp))
                Text("সংরক্ষণ হচ্ছে…", fontWeight = FontWeight.Bold)
            } else {
                Text("শুরু করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "আপনার তথ্য নিরাপদে আপনার Amar Hisab প্রোফাইলে সংরক্ষণ করা হবে।",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileFieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
