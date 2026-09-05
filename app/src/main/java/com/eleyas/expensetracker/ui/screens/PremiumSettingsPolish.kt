package com.eleyas.expensetracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.eleyas.expensetracker.ui.components.SmartReminderSettingsCard
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.DailyReminderManager

@Composable
fun PremiumSettingsPolish(
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
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var adminTapCount by remember { mutableStateOf(0) }

    val authUser = FirebaseAuth.getInstance().currentUser
    val isAdminAccount = currentUserId == "ibauSvNkMnQoZY4u1j84sd2PYZg1" ||
        authUser?.email == "mdeleyas040@gmail.com"
    val displayName = when {
        !authUser?.displayName.isNullOrBlank() -> authUser?.displayName.orEmpty()
        isAdminAccount -> "Owner / Admin"
        !authUser?.phoneNumber.isNullOrBlank() -> "ফোন অ্যাকাউন্ট"
        else -> "আমার অ্যাকাউন্ট"
    }
    val contactInfo = authUser?.email ?: authUser?.phoneNumber ?: "অ্যাকাউন্ট সংযুক্ত আছে"
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("1.0")
    }

    BackHandler(enabled = subView != null) { onSubViewChange(null) }

    if (subView != null) {
        Column(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSubViewChange(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "পিছনে")
                    }
                    Text(
                        text = when (subView) {
                            "personal" -> "ব্যক্তিগত তথ্য"
                            "currency" -> "মুদ্রা ও এক্সচেঞ্জ রেট"
                            "backup" -> "ব্যাকআপ ও রিস্টোর"
                            "json_backup" -> "JSON ডেটা"
                            else -> "সেটিংস"
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
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = .18f)),
                                onClick = {
                                    if (isAdminAccount) {
                                        adminTapCount++
                                        if (adminTapCount >= 5) {
                                            onUnlockAdmin()
                                            adminTapCount = 0
                                            Toast.makeText(context, "অ্যাডমিন কন্ট্রোল সক্রিয় হয়েছে", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    onPhotoClick()
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (profilePhotoUri != null) Icons.Default.Photo else if (isAdminAccount) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(15.dp))
                            Column(Modifier.weight(1f)) {
                                Text("আমার হিসাব", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .70f))
                                Text(displayName, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                                Text(contactInfo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f), maxLines = 1)
                            }
                            IconButton(onClick = onPhotoClick) {
                                Icon(Icons.Default.Edit, contentDescription = "প্রোফাইল পরিবর্তন")
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
                        ) {
                            Text(
                                "PREMIUM • আপনার হিসাব, এক জায়গায়",
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item { PolishSection("অ্যাকাউন্ট") }
        item { PolishRow(Icons.Default.Person, "ব্যক্তিগত তথ্য", "নাম ও অ্যাকাউন্টের তথ্য দেখুন বা পরিবর্তন করুন") { onSubViewChange("personal") } }

        item { PolishSection("হিসাব ও মুদ্রা") }
        item { PolishRow(Icons.Default.CurrencyExchange, "মুদ্রা ও এক্সচেঞ্জ রেট", if (usdToBdt > 0) "১ USD = ৳${"%.2f".format(usdToBdt)}" else "এক্সচেঞ্জ রেট সেট করুন") { onSubViewChange("currency") } }
        item { PolishRow(Icons.Default.Assessment, "মাসিক বাজেট", "ক্যাটাগরি অনুযায়ী খরচের সীমা নির্ধারণ করুন", onBudget) }
        item { PolishRow(Icons.Default.ContentCopy, "আর্থিক সারসংক্ষেপ কপি", "আপনার হিসাবের সারাংশ ক্লিপবোর্ডে কপি করুন", onCopy) }

        item { PolishSection("নোটিফিকেশন ও স্মার্ট ফিচার") }
        item { PolishSwitch(Icons.Default.Lightbulb, "দৈনিক ফিন্যান্সিয়াল টিপস", "প্রতিদিন নতুন আর্থিক টিপস দেখান", com.eleyas.expensetracker.util.DailyFinancialTips.isDailyTipEnabled(context)) { com.eleyas.expensetracker.util.DailyFinancialTips.setDailyTipEnabled(context, it) } }
        item { PolishRow(Icons.Default.FormatQuote, "আজকের টিপস দেখুন", "আজকের ফিন্যান্সিয়াল টিপস এখনই দেখুন", onDailyTipClick) }
        item { PolishSwitch(Icons.Default.NotificationsActive, "দৈনিক খরচ রিমাইন্ডার", "প্রতিদিন রাত ৯টায় খরচের কথা মনে করিয়ে দিন", DailyReminderManager.isReminderEnabled(context)) { DailyReminderManager.setReminderEnabled(context, it); Toast.makeText(context, if (it) "রিমাইন্ডার চালু হয়েছে" else "রিমাইন্ডার বন্ধ হয়েছে", Toast.LENGTH_SHORT).show() } }
        item { SmartReminderSettingsCard() }
        item { PolishSwitch(Icons.Default.DateRange, "সাপ্তাহিক সারসংক্ষেপ", "সপ্তাহের শুরুতে খরচের সারাংশ দেখান", com.eleyas.expensetracker.util.RecapNotificationManager.isWeeklyRecapEnabled(context)) { com.eleyas.expensetracker.util.RecapNotificationManager.setWeeklyRecapEnabled(context, it) } }
        item { PolishSwitch(Icons.Default.EventNote, "মাসিক সারসংক্ষেপ", "মাসের শুরুতে খরচের সারাংশ দেখান", com.eleyas.expensetracker.util.RecapNotificationManager.isMonthlyRecapEnabled(context)) { com.eleyas.expensetracker.util.RecapNotificationManager.setMonthlyRecapEnabled(context, it) } }
        item { PolishSwitch(Icons.Default.VolumeUp, "সাউন্ড ও ভাইব্রেশন", "লেনদেনের সময় শব্দ ও ভাইব্রেশন চালু রাখুন", com.eleyas.expensetracker.util.SoundHapticHelper.isSoundHapticEnabled(context)) { com.eleyas.expensetracker.util.SoundHapticHelper.setSoundHapticEnabled(context, it) } }

        item { PolishSection("ডেটা ও ব্যাকআপ") }
        item { PolishRow(Icons.Default.CloudUpload, "ক্লাউড ব্যাকআপ ও রিস্টোর", "ডেটা নিরাপদে সংরক্ষণ ও প্রয়োজন হলে ফিরিয়ে আনুন") { onSubViewChange("backup") } }
        item { PolishRow(Icons.Default.FolderZip, "JSON ব্যাকআপ", "ডেটা ফাইল এক্সপোর্ট বা ইমপোর্ট করুন") { onSubViewChange("json_backup") } }
        item { PolishRow(Icons.Default.TableChart, "CSV রিপোর্ট", "Excel বা Sheets-এ ব্যবহার করার জন্য রিপোর্ট তৈরি করুন", onExportCsv) }
        item { PolishRow(Icons.Default.PictureAsPdf, "PDF রিপোর্ট", "প্রফেশনাল রিপোর্ট তৈরি করুন", onExportPdf) }

        if (isAdminUnlocked && isAdminAccount) {
            item { PolishSection("অ্যাডমিন কন্ট্রোল") }
            item { PolishRow(Icons.Default.AdminPanelSettings, "ডেভেলপার কনসোল", "অ্যাপের উন্নত কন্ট্রোল ও ম্যানেজমেন্ট", onAdminConsole) }
            item { PolishRow(Icons.Default.CloudUpload, "আপডেট প্রকাশ", "নতুন অ্যাপ ভার্সন প্রকাশ করুন", onPushUpdate) }
        }

        item { PolishSection("অ্যাপ") }
        item { PolishRow(Icons.Default.SystemUpdate, "আপডেট চেক করুন", "নতুন ভার্সন আছে কিনা দেখুন", onCheckUpdate) }
        item { PolishRow(Icons.Default.Logout, "লগআউট", "এই অ্যাকাউন্ট থেকে নিরাপদে বের হয়ে যান", onClick = { showLogoutConfirm = true }) }
        item { PolishRow(Icons.Default.DeleteForever, "হিসাবের ডেটা রিসেট", "সতর্কতা: সংরক্ষিত হিসাব মুছে যাবে", onClick = { showResetConfirm = true }, danger = true) }

        item {
            Text(
                "Amar Hisab • v$versionName",
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("লগআউট করবেন?") },
            text = { Text("আপনি কি এই অ্যাকাউন্ট থেকে লগআউট করতে চান?") },
            confirmButton = { TextButton(onClick = { showLogoutConfirm = false; onLogout() }) { Text("লগআউট") } },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("বাতিল") } }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ডেটা রিসেট করবেন?") },
            text = { Text("এই কাজ করলে আপনার সংরক্ষিত হিসাবের ডেটা মুছে যেতে পারে। নিশ্চিত না হলে বাতিল করুন।") },
            confirmButton = { TextButton(onClick = { showResetConfirm = false; onReset() }) { Text("রিসেট করুন") } },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun PolishSection(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = .8.sp)
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f))
    }
}

@Composable
private fun PolishRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = if (danger) MaterialTheme.colorScheme.error.copy(alpha = .10f) else MaterialTheme.colorScheme.primary.copy(alpha = .10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .50f))
        }
    }
}

@Composable
private fun PolishSwitch(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
