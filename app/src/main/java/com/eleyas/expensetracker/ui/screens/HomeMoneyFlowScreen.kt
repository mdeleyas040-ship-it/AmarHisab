package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.HomeLendingDialog
import com.eleyas.expensetracker.ui.components.HomeLendingReturnDialog
import com.eleyas.expensetracker.ui.components.WarningPopupManager
import com.eleyas.expensetracker.util.FundSource
import com.eleyas.expensetracker.util.HomeLedgerEngine
import com.eleyas.expensetracker.util.HomeMoneyFlow
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.viewmodel.MainViewModel

@Composable
fun HomeMoneyFlowScreen(
    entries: List<HomeLedgerEntry>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    editable: Boolean = false,
    onEditTransaction: (Transaction) -> Unit = {},
    onDeleteTransaction: (Transaction) -> Unit = {},
    onShareTransaction: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val appViewModel: MainViewModel = viewModel()
    val ordered = entries.sortedByDescending { it.date }
    val summary = HomeLedgerEngine.summarize(entries)
    val homeLendings = appViewModel.lendings.filter { FundSource.isHomeLending(it) }
    var showHomeLendingDialog by remember { mutableStateOf(false) }
    var selectedReturnLending by remember { mutableStateOf<LendingAccount?>(null) }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("বাড়ির হিসাব", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (editable) "বাড়ির হিসাব পরিচালনা করুন" else "বাড়ির টাকার সম্পূর্ণ হিসাব",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.95f), MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)))
                ).padding(18.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.14f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("বাড়িতে অবশিষ্ট", color = Color.White.copy(alpha = 0.78f), fontSize = 12.sp)
                        Text("৳ ${formatMoney(summary.balance)}", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    FlowStat("মোট এসেছে", summary.totalIn, true, Modifier.weight(1f))
                    FlowStat("মোট গেছে", summary.totalOut, false, Modifier.weight(1f))
                }
            }
        }

        Card(
            onClick = { showHomeLendingDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                    }
                }
                Spacer(Modifier.size(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("বাড়ির টাকা দিয়ে ধার দিন", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Text("এই টাকা বাড়ির হিসাব থেকে বাদ হবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("→", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (homeLendings.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("বাড়ির ধার", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("ফেরত নেওয়া যাবে", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().height(170.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(homeLendings, key = { it.id }) { lending ->
                    val returned = appViewModel.lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
                    val remaining = (lending.amount - returned).coerceAtLeast(0.0)
                    Card(
                        onClick = { if (remaining > 0.0) selectedReturnLending = lending },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f).padding(horizontal = 9.dp)) {
                                Text(lending.person, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("বাকি: ৳${formatMoney(remaining)}", fontSize = 11.sp, color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                            Text(if (remaining > 0) "ফেরত নিন" else "সম্পূর্ণ ফেরত", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("লেনদেনের ইতিহাস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${ordered.size}টি এন্ট্রি", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

        if (ordered.isEmpty()) {
            Column(Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("এখনও কোনো বাড়ির লেনদেন নেই", fontWeight = FontWeight.Bold)
                Text("বাড়িতে টাকা পাঠানো বা বাড়ির খরচ যোগ করলে এখানে দেখা যাবে.", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(ordered, key = { it.id }) { entry ->
                    val transaction = entry.sourceId.toLongOrNull()?.let { id -> appViewModel.transactions.firstOrNull { it.id == id } }
                    HomeLedgerRow(
                        entry = entry,
                        editable = editable && transaction != null,
                        transaction = transaction,
                        onEdit = { transaction?.let(onEditTransaction) },
                        onDelete = { transaction?.let(onDeleteTransaction) },
                        onShare = { transaction?.let(onShareTransaction) }
                    )
                }
            }
        }
    }

    if (showHomeLendingDialog) {
        HomeLendingDialog(
            onDismiss = { showHomeLendingDialog = false },
            onSave = { person, amount, date, note ->
                val availableHome = appViewModel.homeBalance
                if (amount > availableHome) {
                    WarningPopupManager.show(title = "পর্যাপ্ত টাকা নেই", message = "বাড়ির হিসাবে পর্যাপ্ত টাকা নেই।\n\nঅবশিষ্ট: ৳${formatMoney(availableHome)}")
                } else {
                    val homeNote = listOf("[HOME]", note).filter { it.isNotBlank() }.joinToString(" ")
                    appViewModel.addLending(context, person, amount, date, homeNote, fundSource = "home")
                    showHomeLendingDialog = false
                }
            }
        )
    }

    selectedReturnLending?.let { lending ->
        val alreadyReturned = appViewModel.lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
        HomeLendingReturnDialog(
            lending = lending,
            alreadyReturned = alreadyReturned,
            onDismiss = { selectedReturnLending = null },
            onSave = { amount, date, note ->
                val remaining = (lending.amount - alreadyReturned).coerceAtLeast(0.0)
                if (amount > remaining) {
                    WarningPopupManager.show(title = "ফেরতের পরিমাণ বেশি", message = "বাকি পাওনার চেয়ে বেশি নেওয়া যাবে না।\n\nবাকি: ৳${formatMoney(remaining)}")
                } else {
                    val homeNote = listOf("[HOME_RETURN]", note).filter { it.isNotBlank() }.joinToString(" ")
                    appViewModel.addLendingReturn(context, lending, amount, date, homeNote)
                    selectedReturnLending = null
                }
            }
        )
    }
}

@Composable
private fun FlowStat(label: String, amount: Double, incoming: Boolean, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = 0.12f)) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (incoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            Spacer(Modifier.size(7.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
                Text("৳${formatMoney(amount)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeLedgerRow(
    entry: HomeLedgerEntry,
    editable: Boolean = false,
    transaction: Transaction? = null,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val isIn = entry.direction == HomeLedgerDirection.IN
    val sign = if (isIn) "+" else "−"
    val icon = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val accent = if (isIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(13.dp), color = accent.copy(alpha = 0.11f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(HomeMoneyFlow.sourceLabel(entry), style = MaterialTheme.typography.labelSmall, color = accent)
                Spacer(Modifier.height(3.dp))
                Text(entry.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.note.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(entry.note, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$sign৳${formatMoney(entry.amount)}", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                if (editable && transaction != null) {
                    Row {
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "শেয়ার", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "এডিট", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "ডিলিট", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
