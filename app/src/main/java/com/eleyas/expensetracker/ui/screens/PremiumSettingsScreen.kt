package com.eleyas.expensetracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.ui.components.SmartReminderSettingsCard
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.DailyReminderManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PremiumSettingsScreen(
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
    var adminTapCount by remember { mutableIntStateOf(0) }

    val authUser = FirebaseAuth.getInstance().currentUser
    val isAdminAccount = currentUserId == "ibauSvNkMnQoZY4u1j84sd2PYZg1" ||
        authUser?.email == "mdeleyas040@gmail.com"
    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (_: Exception) { "1.0" }
    }

    BackHandler(enabled = subView != null) { onSubViewChange(null) }

    if (subView != null) {
        SettingsSubView(
            modifier = modifier,
            subView = subView,
            rateLoading = rateLoading,
            usdToBdt = usdToBdt,
            usdToMvr = usdToMvr,
            rateError = rateError,
            onSubViewChange = onSubViewChange,
            onRefreshRate = onRefreshRate,
            onBackup = onBackup,
            onRestore = onRestore,
            onExport = onExport,
            onImport = onImport,
            onEditName = onEditName
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            PremiumSettingsHeader(
                authUser = authUser,
                isAdminAccount = isAdminAccount,
                profilePhotoUri = profilePhotoUri,
                onPhotoClick = {
                    if (isAdminAccount) {
                        adminTapCount++
                        if (adminTapCount >= 5) {
                            onUnlockAdmin()
                            Toast.makeText(context, "👑 Admin Panel Activated", Toast.LENGTH_SHORT).show()
                            adminTapCount = 0
                        }
                    }
                    onPhotoClick()
                },
                currentVersionName = currentVersionName
            )
        }

        item { SettingsGroupTitle("ACCOUNT") }
        item {
            SettingsGroupCard {
                PremiumSettingsRow(Icons.Default.Person, "ব্যক্তিগত তথ্য", "নাম ও অ্যাকাউন্টের তথ্য", { onSubViewChange("personal") })
            }
        }

        item { SettingsGroupTitle("হিসাব ও মুদ্রা") }
        item {
            SettingsGroupCard {
                PremiumSettingsRow(
                    Icons.Default.Language,
                    "মুদ্রা ও এক্সচেঞ্জ রেট",
                    if (usdToBdt > 0) "1 USD = ৳${"%.2f".format(usdToBdt)} • MVR ${"%.2f".format(usdToMvr)}" else "মুদ্রার রেট সেট করুন",
                    { onSubViewChange("currency") }
                )
                GroupDivider()
                PremiumSettingsRow(Icons.Default.Assessment, "মাসিক ক্যাটাগরি বাজেট", "প্রতিটি ক্যাটাগরির খরচের সীমা", onBudget)
                GroupDivider()
                PremiumSettingsRow(Icons.Default.ContentCopy, "ফিন্যান্সিয়াল সামারি কপি", "সারাংশ ক্লিপবোর্ডে কপি করুন", onCopy)
            }
        }

        item { SettingsGroupTitle("নোটিফিকেশন ও স্মার্ট ফিচার") }
        item {
            SettingsGroupCard {
                var dailyTipEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.DailyFinancialTips.isDailyTipEnabled(context)) }
                PremiumSwitchRow(
                    Icons.Default.Lightbulb,
                    "দৈনিক ফিন্যান্সিয়াল টিপস",
                    "প্রতিদিন নতুন টাকা-পয়সার টিপস দেখাবে",
                    dailyTipEnabled
                ) {
                    dailyTipEnabled = it
                    com.eleyas.expensetracker.util.DailyFinancialTips.setDailyTipEnabled(context, it)
                }
                GroupDivider()
                PremiumSettingsRow(Icons.Default.FormatQuote, "আজকের টিপস দেখুন", "আজকের আর্থিক উক্তি ও পরামর্শ", onDailyTipClick)
                GroupDivider()
                var reminderEnabled by remember { mutableStateOf(DailyReminderManager.isReminderEnabled(context)) }
                PremiumSwitchRow(Icons.Default.NotificationsActive, "দৈনিক খরচ রিমাইন্ডার", "প্রতিদিন রাত ৯টায় খরচ এন্ট্রির নোটিফিকেশন", reminderEnabled) {
                    reminderEnabled = it
                    DailyReminderManager.setReminderEnabled(context, it)
                    Toast.makeText(context, if (it) "✅ দৈনিক রিমাইন্ডার চালু" else "❌ দৈনিক রিমাইন্ডার বন্ধ", Toast.LENGTH_SHORT).show()
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SmartReminderSettingsCard()
        }
        item {
            SettingsGroupCard {
                var weeklyEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isWeeklyRecapEnabled(context)) }
                PremiumSwitchRow(Icons.Default.DateRange, "সাপ্তাহিক সারসংক্ষেপ", "প্রতি সপ্তাহে মোট খরচের নোটিফিকেশন", weeklyEnabled) {
                    weeklyEnabled = it
                    com.eleyas.expensetracker.util.RecapNotificationManager.setWeeklyRecapEnabled(context, it)
                }
                GroupDivider()
                var monthlyEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isMonthlyRecapEnabled(context)) }
                PremiumSwitchRow(Icons.Default.EventNote, "মাসিক সারসংক্ষেপ", "প্রতি মাসে মোট খরচের নোটিফিকেশন", monthlyEnabled) {
                    monthlyEnabled = it
                    com.eleyas.expensetracker.util.RecapNotificationManager.setMonthlyRecapEnabled(context, it)
                }
                GroupDivider()
                var soundEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.SoundHapticHelper.isSoundHapticEnabled(context)) }
                PremiumSwitchRow(Icons.AutoMirrored.Filled.VolumeUp, "সাউন্ড ও ভাইব্রেশন", "লেনদেনে শব্দ ও কম্পন feedback", soundEnabled) {
                    soundEnabled = it
                    com.eleyas.expensetracker.util.SoundHapticHelper.setSoundHapticEnabled(context, it)
                    if (it) com.eleyas.expensetracker.util.SoundHapticHelper.playTransactionSavedFeedback(context)
                }
            }
        }

        item { SettingsGroupTitle("ডেটা ও ব্যাকআপ") }
        item {
            SettingsGroupCard {
                PremiumSettingsRow(Icons.Default.CloudUpload, "ক্লাউড ও লোকাল ব্যাকআপ", "ডেটা সেভ ও রিস্টোর করুন", { onSubViewChange("backup") })
                GroupDivider()
                PremiumSettingsRow(Icons.Default.FolderZip, "JSON ব্যাকআপ", "Raw data export / import", { onSubViewChange("json_backup") })
                GroupDivider()
                PremiumSettingsRow(Icons.Default.TableChart, "CSV রিপোর্ট", "Excel বা Google Sheets-এ খুলুন", onExportCsv, Color(0xFF2E7D32))
                GroupDivider()
                PremiumSettingsRow(Icons.Default.PictureAsPdf, "PDF রিপোর্ট", "Professional financial report তৈরি করুন", onExportPdf, Color(0xFF673AB7))
            }
        }

        if (isAdminAccount && isAdminUnlocked) {
            item { SettingsGroupTitle("ADMIN CONTROL") }
            item {
                SettingsGroupCard {
                    PremiumSettingsRow(Icons.Default.Build, "Developer Console", "অ্যাপের ফিচার কন্ট্রোল করুন", onAdminConsole, AccentGreen)
                    GroupDivider()
                    PremiumSettingsRow(Icons.Default.AdminPanelSettings, "Publish Update", "সবার জন্য নতুন ভার্সন চালু করুন", onPushUpdate, Color(0xFFFFB300))
                }
            }
        }

        item { SettingsGroupTitle("অ্যাপ ও অন্যান্য") }
        item {
            SettingsGroupCard {
                PremiumSettingsRow(Icons.Default.SystemUpdate, "আপডেট চেক করুন", "বর্তমান ভার্সন $currentVersionName", onCheckUpdate)
                GroupDivider()
                PremiumSettingsRow(Icons.Default.Logout, "লগআউট", "এই অ্যাকাউন্ট থেকে সাইন আউট করুন", { showLogout = !showLogout }, ExpenseRed)
            }
            if (showLogout) item { LogoutContent(onLogout) }
        }

        item { SettingsGroupTitle("DANGER ZONE") }
        item {
            SettingsGroupCard {
                PremiumSettingsRow(Icons.Default.DeleteForever, "অ্যাকাউন্ট ডেটা রিসেট", "সব ডেটা মুছে ফেলার অপশন", { showReset = !showReset }, MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showReset) item { ResetDataContent(onReset) { showReset = false } }
        }

        item {
            Column(
                Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Amar Hisab", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Income • Expense • Loan • Lending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f))
                Text("Version $currentVersionName", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f), modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun PremiumSettingsHeader(
    authUser: com.google.firebase.auth.FirebaseUser?,
    isAdminAccount: Boolean,
    profilePhotoUri: String?,
    onPhotoClick: () -> Unit,
    currentVersionName: String
) {
    val name = when {
        !authUser?.displayName.isNullOrBlank() -> authUser?.displayName ?: "User Account"
        isAdminAccount -> "Owner / Admin"
        !authUser?.phoneNumber.isNullOrBlank() -> "Phone User"
        else -> "User Account"
    }
    val contact = authUser?.email ?: authUser?.phoneNumber ?: "Guest Mode"

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(82.dp),
                    shape = CircleShape,
                    color = Green.copy(alpha = .10f),
                    border = BorderStroke(1.dp, Green.copy(alpha = .25f)),
                    onClick = onPhotoClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (profilePhotoUri != null) Icons.Default.Photo else if (isAdminAccount) Icons.Default.AdminPanelSettings else Icons.Default.Person, null, tint = Green, modifier = Modifier.size(34.dp))
                    }
                }
                Surface(modifier = Modifier.size(27.dp), shape = CircleShape, color = Green, border = BorderStroke(2.dp, Color.White), onClick = onPhotoClick) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
                }
            }
            Spacer(Modifier.height(11.dp))
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(contact, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFBC02D).copy(alpha = .13f)) {
                Text("PREMIUM • v$currentVersionName", modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC79200))
            }
        }
    }
}

@Composable
private fun SettingsGroupTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 22.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { Column(content = content) }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 58.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
}

@Composable
private fun PremiumSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = tint.copy(alpha = .10f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PremiumSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSubView(
    modifier: Modifier,
    subView: String,
    rateLoading: Boolean,
    usdToBdt: Double,
    usdToMvr: Double,
    rateError: String,
    onSubViewChange: (String?) -> Unit,
    onRefreshRate: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onEditName: () -> Unit
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSubViewChange(null) }) { Icon(Icons.Default.ArrowBack, "Back") }
                Text(
                    when (subView) {
                        "personal" -> "ব্যক্তিগত তথ্য"
                        "currency" -> "মুদ্রা ও রেট"
                        "backup" -> "ব্যাকআপ ও রিস্টোর"
                        "json_backup" -> "JSON ডেটা"
                        else -> "Settings"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
}
