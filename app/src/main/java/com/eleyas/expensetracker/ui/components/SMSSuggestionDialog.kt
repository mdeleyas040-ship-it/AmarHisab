package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.SMSSuggestion
import com.eleyas.expensetracker.ui.theme.ExpenseRed
import com.eleyas.expensetracker.ui.theme.IncomeGreen

@Composable
fun SMSSuggestionDialog(
    suggestions: List<SMSSuggestion>,
    onAddTransaction: (SMSSuggestion) -> Unit,
    onDismiss: (SMSSuggestion) -> Unit,
    onDismissAll: () -> Unit
) {
    if (suggestions.isEmpty()) return

    AlertDialog(
        onDismissRequest = { onDismissAll() },
        title = {
            Text(
                "ব্যাংকিং লেনদেন সুপারিশ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suggestions) { suggestion ->
                    SMSSuggestionCard(
                        suggestion = suggestion,
                        onAdd = { onAddTransaction(suggestion) },
                        onDismiss = { onDismiss(suggestion) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismissAll() },
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("সব বাদ দিন", fontSize = 12.sp)
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SMSSuggestionCard(
    suggestion: SMSSuggestion,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (suggestion.transactionType == "expense") 
                ExpenseRed.copy(alpha = 0.1f) 
            else 
                IncomeGreen.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.bankName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = suggestion.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (suggestion.transactionType == "expense") "-" else "+",
                    color = if (suggestion.transactionType == "expense") ExpenseRed else IncomeGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Amount and Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "৳ ${"%.0f".format(suggestion.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (suggestion.transactionType == "expense") ExpenseRed else IncomeGreen
                )
                
                Surface(
                    modifier = Modifier
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    color = if (suggestion.transactionType == "expense") 
                        ExpenseRed.copy(alpha = 0.2f) 
                    else 
                        IncomeGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = suggestion.category,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (suggestion.transactionType == "expense") ExpenseRed else IncomeGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("যোগ করুন", fontSize = 11.sp)
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("বাদ দিন", fontSize = 11.sp)
                }
            }
        }
    }
}
