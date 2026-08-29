package com.eleyas.expensetracker.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.displayTransactionDate
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun TransactionCard(
    transaction: Transaction,
    usdToBdt: Double,
    usdToMvr: Double,
    walletName: String = "",
    onEdit: (Transaction) -> Unit = {},
    onDelete: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val incomeGreen = Color(0xFF168A45)
    val expenseRed = Color(0xFFD32F2F)
    val blue = Color(0xFF1976D2)
    val cardRadius = 18.dp

    val icon = when (transaction.type) {
        "income" -> Icons.Default.AddCircle
        "expense" -> Icons.Default.RemoveCircle
        "home_expense" -> Icons.Default.HomeWork
        else -> Icons.Default.Home
    }

    val color = when (transaction.type) {
        "income" -> incomeGreen
        "expense" -> expenseRed
        else -> blue
    }

    val bdt = when (transaction.currency) {
        "BDT" -> transaction.amount
        "USD" -> transaction.amount * usdToBdt
        "MVR" ->
            if (usdToMvr > 0) {
                transaction.amount * (usdToBdt / usdToMvr)
            } else 0.0
        else -> 0.0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.10f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    transaction.reason.ifBlank {
                        transaction.category
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    "${transaction.category}  •  ${displayTransactionDate(transaction.date)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "${transaction.currency} ${formatMoney(transaction.amount)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 7.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                    if (walletName.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = walletName,
                            fontSize = 10.sp,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "৳${formatMoney(bdt)}",
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )

                Row {
                    if (transaction.receiptImage != null) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(transaction.receiptImage)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open receipt: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    IconButton(
                        onClick = { onEdit(transaction) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(
                        onClick = { onDelete(transaction) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = expenseRed)
                    }
                }
            }
        }
    }
}
