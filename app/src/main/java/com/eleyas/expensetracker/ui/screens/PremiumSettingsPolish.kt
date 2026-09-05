package com.eleyas.expensetracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    if (subView != null) {
        Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ onSubViewChange(null) }) { Icon(Icons.Default.ArrowBack, "পিছনে") }
                Text(when (subView) { "personal" -> "ব্যক্তিগত তথ্য"; "currency" -> "মুদ্রা ও রেট"; "backup" -> "ব্যাকআপ ও রিস্টোর"; "json_backup" -> "JSON ডেটা"; else -> "সেটিংস" }, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            when (subView) {
                "personal" -> PersonalInformationContent(onEditName)
                "currency" -> CurrencySettingsContent(rateLoading, usdToBdt, usdToMvr, rateError, onRefreshRate)
                "backup" -> BackupSettingsContent(onBackup, onRestore)
                "json_backup" -> FileBackupContent(onExport, onImport)
            }
        }
        return
    }

    LazyColumn(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp)) {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(58.dp), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                            Box(contentAlignment = Alignment.Center) { Icon(if (profilePhotoUri == null) Icons.Default.Person else Icons.Default.Photo, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("আমার হিসাব", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text("আপনার হিসাবের সব নিয়ন্ত্রণ এক জায়গায়", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                        }
                        IconButton(onClick = onPhotoClick) { Icon(Icons.Default.Edit, "প্রোফাইল") }
                    }
                }
            }
        }
        item { PolishSection("অ্যাকাউন্ট") }
        item { PolishRow(Icons.Default.Person, "ব্যক্তিগত তথ্য", "নাম ও অ্যাকাউন্টের তথ্য", { onSubViewChange("personal") }) }

        item { PolishSection("হিসাব ও মুদ্রা") }
        item { PolishRow(Icons.Default.CurrencyExchange, "মুদ্রা ও এক্সচেঞ্জ রেট", if (usdToBdt > 0) "1 USD = ৳${"%.2f".format(usdToBdt)}" else "রেট সেট করুন", { onSubViewChange("currency") }) }
        item { PolishRow(Icons.Default.Assessment, "মাসিক বাজেট", "ক্যাটাগরি অনুযায়ী খরচের সীমা", onBudget) }
        item { PolishRow(Icons.Default.ContentCopy, "আর্থিক সারসংক্ষেপ কপি", "হিসাবের সারাংশ ক্লিপবোর্ডে কপি করুন", onCopy) }

        item { PolishSection("নোটিফিকেশন ও স্মার্ট ফিচার") }
        item { PolishSwitch(Icons.Default.Lightbulb, "দৈনিক ফিন্যান্সিয়াল টিপস", "প্রতিদিন নতুন টিপস দেখান", com.eleyas.expensetracker.util.DailyFinancialTips.isDailyTipEnabled(context)) { com.eleyas.expensetracker.util.DailyFinancialTips.setDailyTipEnabled(context, it) } }
        item { PolishRow(Icons.Default.FormatQuote, "আজকের টিপস দেখুন", "আজকের ফিন্যান্সিয়াল টিপস", onDailyTipClick) }
        item { PolishSwitch(Icons.Default.NotificationsActive, "দৈনিক খরচ রিমাইন্ডার", "প্রতিদিন রাত ৯টায় মনে করিয়ে দিন", DailyReminderManager.isReminderEnabled(context)) { DailyReminderManager.setReminderEnabled(context, it); Toast.makeText(context, if (it) "রিমাইন্ডার চালু হয়েছে" else "রিমাইন্ডার বন্ধ হয়েছে", Toast.LENGTH_SHORT).show() } }
        item { SmartReminderSettingsCard() }
        item { PolishSwitch(Icons.Default.DateRange, "সাপ্তাহিক সারসংক্ষেপ", "সপ্তাহের শুরুতে খরচের সারাংশ", com.eleyas.expensetracker.util.RecapNotificationManager.isWeeklyRecapEnabled(context)) { com.eleyas.expensetracker.util.RecapNotificationManager.setWeeklyRecapEnabled(context, it) } }
        item { PolishSwitch(Icons.Default.EventNote, "মাসিক সারসংক্ষেপ", "মাসের শুরুতে খরচের সারাংশ", com.eleyas.expensetracker.util.RecapNotificationManager.isMonthlyRecapEnabled(context)) { com.eleyas.expensetracker.util.RecapNotificationManager.setMonthlyRecapEnabled(context, it) } }
        item { PolishSwitch(Icons.Default.VolumeUp, "সাউন্ড ও ভাইব্রেশন", "লেনদেনের সময় feedback", com.eleyas.expensetracker.util.SoundHapticHelper.isSoundHapticEnabled(context)) { com.eleyas.expensetracker.util.SoundHapticHelper.setSoundHapticEnabled(context, it) } }

        item { PolishSection("ডেটা ও ব্যাকআপ") }
        item { PolishRow(Icons.Default.CloudUpload, "ক্লাউড ব্যাকআপ ও রিস্টোর", "ডেটা নিরাপদে সংরক্ষণ ও ফিরিয়ে আনুন", { onSubViewChange("backup") }) }
        item { PolishRow(Icons.Default.FolderZip, "JSON ব্যাকআপ", "ডেটা ফাইল এক্সপোর্ট বা ইমপোর্ট", { onSubViewChange("json_backup") }) }
        item { PolishRow(Icons.Default.TableChart, "CSV রিপোর্ট", "Excel বা Sheets-এ ব্যবহার করুন", onExportCsv) }
        item { PolishRow(Icons.Default.PictureAsPdf, "PDF রিপোর্ট", "প্রফেশনাল রিপোর্ট তৈরি করুন", onExportPdf) }

        if (isAdminUnlocked) {
            item { PolishSection("অ্যাডমিন কন্ট্রোল") }
            item { PolishRow(Icons.Default.AdminPanelSettings, "ডেভেলপার কনসোল", "অ্যাপের উন্নত কন্ট্রোল", onAdminConsole) }
            item { PolishRow(Icons.Default.CloudUpload, "আপডেট প্রকাশ", "নতুন ভার্সন প্রকাশ করুন", onPushUpdate) }
        }

        item { PolishSection("অ্যাপ") }
        item { PolishRow(Icons.Default.SystemUpdate, "আপডেট চেক করুন", "নতুন ভার্সন আছে কিনা দেখুন", onCheckUpdate) }
        item { PolishRow(Icons.Default.Logout, "লগআউট", "এই অ্যাকাউন্ট থেকে বের হয়ে যান", onLogout) }
        item { PolishRow(Icons.Default.DeleteForever, "অ্যাকাউন্ট ডেটা রিসেট", "সব হিসাব মুছে ফেলার অপশন", onReset) }
    }
}

@Composable private fun PolishSection(title: String) { Text(title, Modifier.padding(start = 20.dp, top = 18.dp, bottom = 7.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp) }

@Composable private fun PolishRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp)) } }
            Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .55f))
        }
    }
    HorizontalDivider(Modifier.padding(start = 74.dp), thickness = .5.dp)
}

@Composable private fun PolishSwitch(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp)) } }
        Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked, onChange)
    }
}
