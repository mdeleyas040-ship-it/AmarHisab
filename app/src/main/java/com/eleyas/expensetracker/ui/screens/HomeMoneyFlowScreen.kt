package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.HomeLedgerDirection
import com.eleyas.expensetracker.model.HomeLedgerEntry
import com.eleyas.expensetracker.util.HomeLedgerEngine
import com.eleyas.expensetracker.util.HomeMoneyFlow
import java.util.Locale

@Composable
fun HomeMoneyFlowScreen(
    entries: List<HomeLedgerEntry>,
    modifier: Modifier = Modifier
) {
    val ordered = entries.sortedByDescending { it.date }
    val summary = HomeLedgerEngine.summarize(entries)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏠", fontSize = 28.sp)
            Spacer(Modifier.padding(6.dp))
            Text("বাড়ির হিসাব", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("মোট এসেছে", summary.totalIn, Modifier.weight(1f))
            SummaryCard("মোট গেছে", summary.totalOut, Modifier.weight(1f))
            SummaryCard("অবশিষ্ট", summary.balance, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()

        if (ordered.isEmpty()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("এখনও কোনো বাড়ির লেনদেন নেই", fontWeight = FontWeight.SemiBold)
                Text("বাড়িতে টাকা পাঠানো বা বাড়ির খরচ যোগ করলে এখানে দেখা যাবে.", modifier = Modifier.padding(16.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ordered, key = { it.id }) { HomeLedgerRow(it) }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Double, modifier: Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(formatAmount(amount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun HomeLedgerRow(entry: HomeLedgerEntry) {
    val sign = if (entry.direction == HomeLedgerDirection.IN) "+" else "−"
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(13.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.title, fontWeight = FontWeight.SemiBold)
                    Text(HomeMoneyFlow.sourceLabel(entry), style = MaterialTheme.typography.labelMedium)
                }
                Text("$sign${formatAmount(entry.amount)}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(entry.date, style = MaterialTheme.typography.labelSmall)
            if (entry.note.isNotBlank()) Text(entry.note, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatAmount(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)
