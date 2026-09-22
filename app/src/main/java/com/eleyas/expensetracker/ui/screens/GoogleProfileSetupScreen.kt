package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("আপনার প্রোফাইল সম্পূর্ণ করুন")
        Text("প্রথমবার Google দিয়ে Login করেছেন। কয়েকটি তথ্য দিলে Amar Hisab আপনার জন্য প্রস্তুত হবে।")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("নাম *") },
            singleLine = true
        )

        user?.email?.let {
            OutlinedTextField(
                value = it,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ইমেইল") },
                enabled = false,
                singleLine = true
            )
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") },
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currency,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text("মূল মুদ্রা") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencies.forEach { item ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            currency = item
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = monthlyIncome,
            onValueChange = { monthlyIncome = it.filter { ch -> ch.isDigit() || ch == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("মাসিক আয় (ঐচ্ছিক)") },
            singleLine = true
        )

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("দেশ (ঐচ্ছিক)") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

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
                    phone = phone,
                    currency = currency,
                    monthlyIncome = monthlyIncome.toDoubleOrNull(),
                    country = country,
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
            modifier = Modifier.fillMaxWidth()
        ) {
            if (saving) CircularProgressIndicator()
            else Text("শুরু করুন")
        }
    }
}
