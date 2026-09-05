package com.eleyas.expensetracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.eleyas.expensetracker.ui.components.SmartReminderSettingsCard
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.DailyReminderManager

@Composable
fun SettingsScreen(
    modifier: Modifier,
    currentUserId: String,
    usdToBdt: Double,
    usdToMvr: Double,
    rateLoading: Boolean,
    rateError: String,
    subView: String?,
    profilePhotoUri: String?,
    isAdminUnlocked: Boolean,
    onPhotoClick: () -> Unit,
    onUnlockAdmin: () -> Unit,
    onSubViewChange: (String?) -> Unit,
    onRefreshRate: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onCopy: () -> Unit,
    onBudget: () -> Unit,
    onReset: () -> Unit,
    onEditName: () -> Unit,
    onCheckUpdate: () -> Unit,
    onAdminConsole: () -> Unit,
    onPushUpdate: () -> Unit,
    onLogout: () -> Unit,
    onDailyTipClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLogout by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var adminTapCount by remember { mutableStateOf(0) }
    val isAdmin = currentUserId == "ibauSvNkMnQoZY4u1j84sd2PYZg1" || FirebaseAuth.getInstance().currentUser?.email == "mdeleyas040@gmail.com"
    val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrDefault("1.0") }

    BackHandler(enabled = subView != null) { onSubViewChange(null) }

    if (subView != null) {
        Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onSubViewChange(null) }) { Icon(Icons.Default.ArrowBack, "Back") }
                    Text(when (subView) {
                        "personal" -> "ব্যক্তিগত তথ্য"
                        "currency" -> "মুদ্রা ও রেট"
                        "backup" -> "ব্যাকআপ ও রিস্টোর"
                        "json_backup" -> "JSON ডেটা"
                        else -> "সেটিংস"
                    }, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(Modifier.weight(1f)) {
                when (subView) {
                    "personal" -> PersonalInformationContent(onEditName)
                    "currency" -> CurrencySettingsContent(rateLoading, usdToBdt, usdToMvr, rateError, onRefreshRate)
                    "backup" -> BackupSettingsContent(onBackup, onRestore)
                    "json_backup" -> FileBackupContent(onExport, onImport)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            val user = FirebaseAuth.getInstance().currentUser
            val name = user?.displayName?.takeIf { it.isNotBlank() } ?: if (isAdmin) "Owner / Admin" else "আমার হিসাব"
            val contact = user?.email ?: user?.phoneNumber ?: "Guest Mode"
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(vertical = 28.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        Modifier.size(90.dp), shape = CircleShape, color = Green.copy(alpha = .10f),
                        onClick = {
                            if (isAdmin) {
                                adminTapCount++
                                if (adminTapCount >= 5) {
                                    onUnlockAdmin()
                                    Toast.makeText(context, "👑 Admin Panel Activated", Toast.LENGTH_SHORT).show()
                                }
                            }
                            onPhotoClick()
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person, null, Modifier.size(38.dp), tint = Green)
                        }
                    }
                    Surface(Modifier.size(28.dp), shape = CircleShape, color = Green, onClick = onPhotoClick) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(contact, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFBC02D).copy(alpha = .15f)) {
                    Text("PREMIUM VERSION", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD4A017))
                }
            }
        }

        item { SettingsSectionHeader("Account Center") }
        item { SettingsItemRow(Icons.Default.Person, "ব্যক্তিগত তথ্য", "আপনার অ্যাকাউন্টের তথ্য পরিবর্তন করুন") { onSubViewChange("personal") } }

        item { SettingsSectionHeader("Preferences") }
        item { SettingsItemRow(Icons.Default.Lightbulb, "দৈনিক ফিন্যান্সিয়াল টিপস", "প্রতিদিন নতুন টাকা সঞ্চয় ও ব্যবস্থাপনা টিপস") { onDailyTipClick() } }
        item {
            var enabled by remember { mutableStateOf(com.eleyas.expensetracker.util.DailyFinancialTips.isDailyTipEnabled(context)) }
            SettingsItemSwitch(Icons.Default.Lightbulb, "দৈনিক টিপস পপ-আপ", "অ্যাপ চালুর সময় প্রতিদিন টিপস দেখাবে", enabled) {
                enabled = it; com.eleyas.expensetracker.util.DailyFinancialTips.setDailyTipEnabled(context, it)
            }
        }
        item {
            var enabled by remember { mutableStateOf(DailyReminderManager.isReminderEnabled(context)) }
            SettingsItemSwitch(Icons.Default.NotificationsActive, "দৈনিক খরচ রিমাইন্ডার", "প্রতিদিন রাত ৯টায় খরচ এন্ট্রির নোটিফিকেশন", enabled) {
                enabled = it; DailyReminderManager.setReminderEnabled(context, it)
            }
        }
        item { SmartReminderSettingsCard() }
        item {
            var enabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isWeeklyRecapEnabled(context)) }
            SettingsItemSwitch(Icons.Default.DateRange, "সাপ্তাহিক খরচের সারসংক্ষেপ", "প্রতি সপ্তাহে মোট খরচের নোটিফিকেশন", enabled) {
                enabled = it; com.eleyas.expensetracker.util.RecapNotificationManager.setWeeklyRecapEnabled(context, it)
            }
        }
        item {
            var enabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isMonthlyRecapEnabled(context)) }
            SettingsItemSwitch(Icons.Default.EventNote, "মাসিক খরচের সারসংক্ষেপ", "প্রতি মাসে মোট খরচের নোটিফিকেশন", enabled) {
                enabled = it; com.eleyas.expensetracker.util.RecapNotificationManager.setMonthlyRecapEnabled(context, it)
            }
        }
        item {
            var enabled by remember { mutableStateOf(com.eleyas.expensetracker.util.SoundHapticHelper.isSoundHapticEnabled(context)) }
            SettingsItemSwitch(Icons.AutoMirrored.Filled.VolumeUp, "সাউন্ড ও ভাইব্রেশন", "লেনদেনে শব্দ ও কম্পন feedback", enabled) {
                enabled = it; com.eleyas.expensetracker.util.SoundHapticHelper.setSoundHapticEnabled(context, it)
            }
        }
        item { SettingsItemRow(Icons.Default.CurrencyExchange, "মুদ্রা ও এক্সচেঞ্জ রেট", if (usdToBdt > 0) "1 USD = ৳${"%.2f".format(usdToBdt)}" else "রেট সেট করুন") { onSubViewChange("currency") } }
        item { SettingsItemRow(Icons.Default.Assessment, "মাসিক ক্যাটাগরি বাজেট", "আপনার খরচের সীমা নির্ধারণ করুন", onBudget) }
        item { SettingsItemRow(Icons.Default.ContentCopy, "ফাইন্যান্সিয়াল সামারি কপি", "ক্লিপবোর্ডে হিসাব কপি করুন", onCopy) }

        item { SettingsSectionHeader("Backup & Export") }
        item { SettingsItemRow(Icons.Default.CloudUpload, "ক্লাউড ব্যাকআপ ও রিস্টোর", "ডাটা নিরাপদে ব্যাকআপ ও ফিরিয়ে আনুন") { onSubViewChange("backup") } }
        item { SettingsItemRow(Icons.Default.FolderZip, "JSON ব্যাকআপ ফাইল", "ডাটা এক্সপোর্ট বা ইমপোর্ট করুন") { onSubViewChange("json_backup") } }
        item { SettingsItemRow(Icons.Default.TableChart, "CSV রিপোর্ট এক্সপোর্ট", "Excel বা Sheets-এ খুলুন", onExportCsv) }
        item { SettingsItemRow(Icons.Default.PictureAsPdf, "PDF রিপোর্ট এক্সপোর্ট", "প্রফেশনাল রিপোর্ট তৈরি করুন", onExportPdf) }

        if (isAdmin && isAdminUnlocked) {
            item { SettingsSectionHeader("Admin Control Panel") }
            item { SettingsItemRow(Icons.Default.Build, "Developer Console", "অ্যাপের ফিচার কন্ট্রোল করুন", onAdminConsole) }
            item { SettingsItemRow(Icons.Default.AdminPanelSettings, "Publish Update", "নতুন ভার্সন v$version প্রকাশ করুন", onPushUpdate) }
        }

        item { SettingsSectionHeader("Others") }
        item { SettingsItemRow(Icons.Default.SystemUpdate, "আপডেট চেক করুন", "বর্তমান ভার্সন $version", onCheckUpdate) }
        item { SettingsItemRow(Icons.Default.Logout, "লগআউট", "অ্যাকাউন্ট থেকে সাইন আউট করুন", { showLogout = !showLogout }, ExpenseRed) }
        if (showLogout) item { LogoutContent(onLogout) }
        item { SettingsSectionHeader("Danger Zone") }
        item { SettingsItemRow(Icons.Default.DeleteForever, "অ্যাকাউন্ট ডাটা রিসেট", "সব ডাটা মুছে ফেলার অপশন", { showReset = !showReset }, ExpenseRed) }
        if (showReset) item { ResetDataContent(onReset) { showReset = false } }

        item {
            Column(Modifier.fillMaxWidth().padding(top = 34.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Amar Hisab - Income & Expense Tracker", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Version $version", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(title.uppercase(), Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
}

@Composable
private fun SettingsItemRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = titleColor); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f))
        }
    }
    HorizontalDivider(Modifier.padding(start = 76.dp), thickness = .5.dp)
}

@Composable
private fun SettingsItemSwitch(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(onClick = { onCheckedChange(!checked) }, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) } }
            Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
    HorizontalDivider(Modifier.padding(start = 76.dp), thickness = .5.dp)
}
