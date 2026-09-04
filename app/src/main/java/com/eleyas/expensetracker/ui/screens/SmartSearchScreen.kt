package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.Transaction

@Composable
fun SmartSearchScreen(
    transactions: List<Transaction>,
    query: String,
    onQueryChange: (String) -> Unit,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    var filter by remember { mutableStateOf("সব") }
    val filters = listOf("সব", "আয়", "খরচ", "ধার", "পাওনা", "পরিশোধ", "নেওয়া")
    val normalizedQuery = query.trim().lowercase()
    val results = remember(transactions, normalizedQuery, filter) {
        transactions.filter { item ->
            val text = listOf(item.type, item.category, item.reason, item.date, item.currency)
                .joinToString(" ").lowercase()
            (normalizedQuery.isBlank() || text.contains(normalizedQuery)) &&
                (filter == "সব" || smartFilterMatches(item, filter))
        }.sortedByDescending { it.date }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "খুঁজুন") },
            placeholder = { Text("নাম, কারণ, খাত বা তারিখ লিখে খুঁজুন") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { label ->
                FilterChip(
                    selected = filter == label,
                    onClick = { filter = label },
                    label = { Text(label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (query.isBlank()) "সাম্প্রতিক হিসাব" else "${results.size}টি ফলাফল",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (results.isEmpty()) {
            Text("কোনো হিসাব পাওয়া যায়নি।", modifier = Modifier.padding(vertical = 24.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = { it.id }) { item ->
                    Card(
                        onClick = { onTransactionClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.category.ifBlank { "হিসাব" }, style = MaterialTheme.typography.titleSmall)
                                Text("${item.amount} ${item.currency}", style = MaterialTheme.typography.titleSmall)
                            }
                            if (item.reason.isNotBlank()) {
                                Text(item.reason, modifier = Modifier.padding(top = 4.dp))
                            }
                            Text(item.date, modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

private fun smartFilterMatches(item: Transaction, filter: String): Boolean {
    val value = "${item.type} ${item.category} ${item.reason}".lowercase()
    return when (filter) {
        "আয়" -> value.contains("আয়") || value.contains("income")
        "খরচ" -> value.contains("খরচ") || value.contains("expense")
        "ধার" -> value.contains("ধার") || value.contains("loan")
        "পাওনা" -> value.contains("পাওনা") || value.contains("lend") || value.contains("দেনা")
        "পরিশোধ" -> value.contains("পরিশোধ") || value.contains("payment") || value.contains("paid")
        "নেওয়া" -> value.contains("নেওয়া") || value.contains("borrow")
        else -> true
    }
}
