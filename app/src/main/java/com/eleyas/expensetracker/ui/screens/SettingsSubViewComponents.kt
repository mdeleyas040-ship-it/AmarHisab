package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PersonalInformationContent(onEditName: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSubCard {
            Text("অ্যাকাউন্ট তথ্য", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                user?.displayName?.takeIf { it.isNotBlank() } ?: "নাম সেট করা হয়নি",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                user?.email ?: user?.phoneNumber ?: "অ্যাকাউন্ট তথ্য পাওয়া যায়নি",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onEditName) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("নাম পরিবর্তন করুন")
            }
        }
    }
}

@Composable
fun CurrencySettingsContent(
    rateLoading: Boolean,
    usdToBdt: Double,
    usdToMvr: Double,
    rateError: String,
    onRefreshRate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSubCard {
            Text("বর্তমান এক্সচেঞ্জ রেট", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (rateLoading) {
                CircularProgressIndicator()
            } else {
                Text("১ USD = ৳${"%.2f".format(usdToBdt)}", fontSize = 18.sp)
                Text("১ USD = MVR ${"%.2f".format(usdToMvr)}", fontSize = 18.sp)
            }
            if (rateError.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(rateError, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRefreshRate, enabled = !rateLoading) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("রেট আপডেট করুন")
            }
        }
    }
}

@Composable
fun BackupSettingsContent(onBackup: () -> Unit, onRestore: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSubCard {
            Text("ক্লাউড ব্যাকআপ", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("আপনার হিসাবের ডেটা ব্যাকআপ বা রিস্টোর করুন।")
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onBackup) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("ব্যাকআপ")
                }
                OutlinedButton(onClick = onRestore) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("রিস্টোর")
                }
            }
        }
    }
}

@Composable
fun FileBackupContent(onExport: () -> Unit, onImport: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSubCard {
            Text("JSON ডেটা", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("ডেটা JSON ফাইলে সংরক্ষণ করুন অথবা আগের JSON ডেটা ইমপোর্ট করুন।")
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onExport) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("এক্সপোর্ট")
                }
                OutlinedButton(onClick = onImport) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("ইমপোর্ট")
                }
            }
        }
    }
}

@Composable
private fun SettingsSubCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) { content() }
    }
}
