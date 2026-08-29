package com.eleyas.expensetracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.DailyReminderManager
import com.eleyas.expensetracker.util.AppLanguageManager

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
    var showLogout by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    
    val isAdminAccount = currentUserId == "ibauSvNkMnQoZY4u1j84sd2PYZg1" || 
                         FirebaseAuth.getInstance().currentUser?.email == "mdeleyas040@gmail.com"

    var adminTapCount by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    
    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "1.0"
        }
    }

    BackHandler(enabled = subView != null) {
        onSubViewChange(null)
    }

    if (subView != null) {
        Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSubViewChange(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = when (subView) {
                            "personal" -> "Personal Information"
                            "currency" -> "Currency & Rates"
                            "backup" -> "Backup & Restore"
                            "json_backup" -> "JSON Data"
                            else -> "Settings"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            val authUser = FirebaseAuth.getInstance().currentUser
            val isAdminUID = currentUserId == "ibauSvNkMnQoZY4u1j84sd2PYZg1"
            val displayName = when {
                authUser?.displayName != null && authUser.displayName!!.isNotBlank() -> authUser.displayName
                isAdminUID -> "Owner / Admin"
                authUser?.phoneNumber != null -> "Phone User"
                else -> "User Account"
            }
            val contactInfo = authUser?.email ?: authUser?.phoneNumber ?: "Guest Mode"

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surface)
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = Green.copy(alpha = 0.1f),
                        onClick = {
                            if (isAdminAccount) {
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
                            if (profilePhotoUri != null) {
                                Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(35.dp), tint = Green)
                            } else {
                                Icon(if (isAdminAccount) Icons.Default.AdminPanelSettings else Icons.Default.Person, contentDescription = null, modifier = Modifier.size(35.dp), tint = Green)
                            }
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Green,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                        onClick = onPhotoClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(14.dp))
                Text(text = displayName!!, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Text(text = contactInfo, fontSize = 14.sp, color = secondary)
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFBC02D).copy(alpha = 0.15f)) {
                    Text("PREMIUM VERSION", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD4A017))
                }
            }
        }

        item { SettingsSectionHeader("Account Center") }
        item {
            SettingsItemRow(
                icon = Icons.Default.Person,
                title = "Personal Information",
                subtitle = "Manage your account details",
                onClick = { onSubViewChange("personal") }
            )
        }

        item { SettingsSectionHeader("Preferences") }
        item {
            var dailyTipEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.DailyFinancialTips.isDailyTipEnabled(context)) }
            SettingsItemRowWithSwitch(
                icon = Icons.Default.Lightbulb,
                title = "দৈনিক ফিন্যান্সিয়াল টিপস (Daily Tip)",
                subtitle = "অ্যাপ ওপেন করার সময় প্রতিদিন নতুন উক্তি/টিপস পপ-আপ",
                checked = dailyTipEnabled,
                onCheckedChange = { isChecked: Boolean ->
                    dailyTipEnabled = isChecked
                    com.eleyas.expensetracker.util.DailyFinancialTips.setDailyTipEnabled(context, isChecked)
                    if (isChecked) {
                        Toast.makeText(context, "✅ দৈনিক ফিন্যান্সিয়াল টিপস চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ দৈনিক ফিন্যান্সিয়াল টিপস বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.FormatQuote,
                title = "আজকের টিপস দেখুন",
                subtitle = "আজকের টাকা জমানো ও ফিন্যান্স ম্যানেজমেন্টের উক্তি",
                onClick = onDailyTipClick
            )
        }
        item {
            var reminderEnabled by remember { mutableStateOf(DailyReminderManager.isReminderEnabled(context)) }
            SettingsItemRowWithSwitch(
                icon = Icons.Default.NotificationsActive,
                title = "দৈনিক খরচ রিমাইন্ডার",
                subtitle = "প্রতিদিন রাতে ৯টায় খরচ এন্ট্রির নোটিফিকেশন",
                checked = reminderEnabled,
                onCheckedChange = { isChecked: Boolean ->
                    reminderEnabled = isChecked
                    DailyReminderManager.setReminderEnabled(context, isChecked)
                    if (isChecked) {
                        Toast.makeText(context, "✅ দৈনিক রিমাইন্ডার চালু করা হয়েছে (রাত ৯:০০)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ দৈনিক রিমাইন্ডার বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            var weeklyEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isWeeklyRecapEnabled(context)) }
            SettingsItemRowWithSwitch(
                icon = Icons.Default.DateRange,
                title = "সাপ্তাহিক খরচের সারসংক্ষেপ",
                subtitle = "প্রতি সপ্তাহের শুরুতে মোট খরচের নোটিফিকেশন",
                checked = weeklyEnabled,
                onCheckedChange = { isChecked: Boolean ->
                    weeklyEnabled = isChecked
                    com.eleyas.expensetracker.util.RecapNotificationManager.setWeeklyRecapEnabled(context, isChecked)
                    if (isChecked) {
                        Toast.makeText(context, "✅ সাপ্তাহিক সারসংক্ষেপ চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ সাপ্তাহিক সারসংক্ষেপ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            var monthlyEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.RecapNotificationManager.isMonthlyRecapEnabled(context)) }
            SettingsItemRowWithSwitch(
                icon = Icons.Default.EventNote,
                title = "মাসিক খরচের সারসংক্ষেপ",
                subtitle = "প্রতি মাসের শুরুতে মোট খরচের নোটিফিকেশন",
                checked = monthlyEnabled,
                onCheckedChange = { isChecked: Boolean ->
                    monthlyEnabled = isChecked
                    com.eleyas.expensetracker.util.RecapNotificationManager.setMonthlyRecapEnabled(context, isChecked)
                    if (isChecked) {
                        Toast.makeText(context, "✅ মাসিক সারসংক্ষেপ চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ মাসিক সারসংক্ষেপ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            var soundHapticEnabled by remember { mutableStateOf(com.eleyas.expensetracker.util.SoundHapticHelper.isSoundHapticEnabled(context)) }
            SettingsItemRowWithSwitch(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "সাউন্ড ও ভাইব্রেশন",
                subtitle = "লেনদেন সেভ, এডিট বা ডিলিট হলে শব্দ ও কম্পন feedback",
                checked = soundHapticEnabled,
                onCheckedChange = { isChecked: Boolean ->
                    soundHapticEnabled = isChecked
                    com.eleyas.expensetracker.util.SoundHapticHelper.setSoundHapticEnabled(context, isChecked)
                    if (isChecked) {
                        // চালু করার সাথে সাথে একটা demo feedback বাজবে
                        com.eleyas.expensetracker.util.SoundHapticHelper.playTransactionSavedFeedback(context)
                        Toast.makeText(context, "✅ সাউন্ড ও ভাইব্রেশন চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ সাউন্ড ও ভাইব্রেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item {
            SettingsItemRow(
                icon = Icons.Default.Language,
                title = "Currency & Exchange Rate",
                subtitle = if (usdToBdt > 0) "1 USD = ৳${"%.2f".format(usdToBdt)}" else "Set rates",
                onClick = { onSubViewChange("currency") }
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.Assessment,
                title = "Monthly Category Budgets",
                subtitle = "Manage your spending limits",
                onClick = onBudget
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.ContentCopy,
                title = "Copy Financial Summary",
                subtitle = "Copy to clipboard",
                onClick = onCopy
            )
        }

        item { SettingsSectionHeader("Backup & Export") }
        item {
            SettingsItemRow(
                icon = Icons.Default.CloudUpload,
                title = "Cloud & Local Backup",
                subtitle = "Sync and restore data",
                onClick = { onSubViewChange("backup") }
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.FolderZip,
                title = "JSON Backup File",
                subtitle = "Export or Import raw data",
                onClick = { onSubViewChange("json_backup") }
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.TableChart,
                title = "Export Excel (CSV) Report",
                subtitle = "Open data in Excel or Sheets",
                onClick = onExportCsv,
                tint = Color(0xFF2E7D32)
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.PictureAsPdf,
                title = "Export PDF Report",
                subtitle = "Generate professional report",
                onClick = onExportPdf,
                tint = Color(0xFF673AB7)
            )
        }

        if (isAdminAccount && isAdminUnlocked) {
            item { SettingsSectionHeader("Admin Control Panel") }
            item {
                SettingsItemRow(
                    icon = Icons.Default.Build,
                    title = "Developer Console",
                    subtitle = "অ্যাপের সব ফিচার এখান থেকে কন্ট্রোল করুন",
                    onClick = onAdminConsole,
                    tint = AccentGreen
                )
            }
            item {
                SettingsItemRow(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Publish Update",
                    subtitle = "সবার জন্য নতুন ভার্সন (v$currentVersionName) চালু করুন",
                    onClick = onPushUpdate,
                    tint = Color(0xFFFFD600)
                )
            }
        }
        
        item { SettingsSectionHeader("Others") }
        item {
            SettingsItemRow(
                icon = Icons.Default.SystemUpdate,
                title = "Check for Updates",
                subtitle = "Version $currentVersionName",
                onClick = onCheckUpdate
            )
        }
        item {
            SettingsItemRow(
                icon = Icons.Default.Logout,
                title = "Logout",
                subtitle = "Sign out from this account",
                onClick = { showLogout = !showLogout },
                titleColor = ExpenseRed
            )
            if (showLogout) {
                LogoutContent(onLogout)
            }
        }
        item { SettingsSectionHeader("Danger Zone") }
        item {
            SettingsItemRow(
                icon = Icons.Default.DeleteForever,
                title = "Reset Account Data",
                subtitle = "Tap to reveal reset controls",
                onClick = { showReset = !showReset },
                titleColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showReset) {
                ResetDataContent(onReset) { showReset = false }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Amar Hisab - Income & Expense Tracker", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = secondary)
                Text("Version $currentVersionName", fontSize = 11.sp, color = secondary.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Text("আপনার আয়, খরচ ও বাড়িতে পাঠানো টাকা সহজে ট্র্যাক করার জন্য।", fontSize = 11.sp, color = secondary.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
            }
        }
    }
}

@Composable
fun PersonalInformationContent(
    onEditName: () -> Unit
) {
    val authUser = FirebaseAuth.getInstance().currentUser
    
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                InfoMiniActionRow("আপনার নাম", authUser?.displayName ?: "সেট করা নেই", onEditName)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("ইমেইল অ্যাড্রেস", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(2.dp))
                    Text(authUser?.email ?: "সেট করা নেই", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = "আপনার অ্যাকাউন্টটি গুগল-এর সাথে যুক্ত। তাই পাসওয়ার্ড ও ইমেইল গুগল থেকেই নিয়ন্ত্রিত হয়।",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        )
    }
}

@Composable
fun InfoMiniActionRow(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Button(
            onClick = onEdit,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(30.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green.copy(alpha = 0.1f), contentColor = Green)
        ) {
            Text("পরিবর্তন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    tint: Color? = null
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = (tint ?: MaterialTheme.colorScheme.secondary).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint ?: MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text("〉", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun CurrencySettingsContent(loading: Boolean, bdt: Double, mvr: Double, error: String, onRefresh: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("লাইভ এক্সচেঞ্জ রেট", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp, color = Green)
                    Spacer(Modifier.height(8.dp))
                    Text("রেট আপডেট হচ্ছে...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (bdt > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        RateBox("🇺🇸 1 USD", "৳${"%.2f".format(bdt)}")
                        RateBox("🇲🇻 1 MVR", "৳${"%.4f".format(bdt/mvr)}")
                    }
                }
                
                if (error.isNotEmpty()) {
                    Text("❌ $error", color = ExpenseRed, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
                
                Spacer(Modifier.height(20.dp))
                
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text("🔄 রিফ্রেশ রেট", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text(
            "সর্বশেষ আপডেটেড রেট অনুযায়ী আপনার হিসাবের কনভার্সন করা হবে। ইন্টারনেট সংযোগ চালু রাখুন।",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun SettingsItemRowWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    tint: Color? = null
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = (tint ?: MaterialTheme.colorScheme.secondary).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint ?: MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun RateBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Green)
    }
}

@Composable
fun SecuritySettingsContent(onChangePassword: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text("Login & Security", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(10.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                SettingsSubRow(
                    icon = "🔑",
                    title = "Change Password",
                    subtitle = "Recommended to change every 6 months",
                    onClick = onChangePassword
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                SettingsSubRow(
                    icon = "🛡️",
                    title = "Two-Factor Authentication",
                    subtitle = "Add extra security to your account",
                    onClick = { /* Future implementation */ }
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        Text("Login Activity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(10.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📱", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("This Device", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Last active: Just now", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SettingsSubRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("〉", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun BackupSettingsContent(onBackup: () -> Unit, onRestore: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("ক্লাউড ব্যাকআপ ও রিস্টোর", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("আপনার সব লেনদেনের তথ্য ক্লাউডে নিরাপদ রাখুন যাতে ফোন পরিবর্তন করলেও ডাটা ফিরে পান।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onBackup, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                    Text("💾 এখনই ব্যাকআপ নিন", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("♻️ অটো ব্যাকআপ রিস্টোর", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FileBackupContent(onExport: () -> Unit, onImport: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("ফাইল এক্সপোর্ট ও ইমপোর্ট", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("পুরো হিসাবের ডাটা JSON ফাইল হিসেবে আপনার মেমোরিতে সেভ করে রাখতে পারেন।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onExport, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                    Text("📤 ফাইল এক্সপোর্ট করুন", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("📥 ফাইল ইমপোর্ট করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ResetDataContent(onReset: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = ExpenseRed.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(30.dp))
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text("ডাটা রিসেট নিশ্চিত করুন", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                Spacer(Modifier.height(10.dp))
                Text(
                    "সাবধান! এটি এই অ্যাকাউন্টের সব লেনদেন, ঋণ ও ধারের ডাটা চিরতরে মুছে ফেলবে। ক্লাউড এবং লোকাল—উভয় জায়গা থেকেই ডাটা ডিলিট হয়ে যাবে। এই কাজ আর ফিরিয়ে আনা সম্ভব নয়।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("হ্যাঁ, সব মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(10.dp))
                
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("বাতিল করুন", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun LogoutContent(onLogout: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Green.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Green, modifier = Modifier.size(30.dp))
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text("লগআউট নিশ্চিত করুন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(
                    "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্ট থেকে লগআউট করতে চান?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("লগআউট করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
