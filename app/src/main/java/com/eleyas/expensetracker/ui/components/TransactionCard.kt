package com.eleyas.expensetracker.ui.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.AnomalyDetector
import com.eleyas.expensetracker.util.displayTransactionDate
import com.eleyas.expensetracker.util.formatMoney

@Composable
fun TransactionCard(
    transaction: Transaction,
    usdToBdt: Double,
    usdToMvr: Double,
    walletName: String = "",
    comparisonTransactions: List<Transaction> = emptyList(),
    onEdit: (Transaction) -> Unit = {},
    onDelete: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val incomeGreen = Color(0xFF168A45)
    val expenseRed = Color(0xFFD32F2F)
    val blue = Color(0xFF1976D2)
    val warning = Color(0xFFE65100)
    val cardRadius = 18.dp

    var mediaPlayer by remember(transaction.id, transaction.audioMemoPath) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(transaction.id, transaction.audioMemoPath) { mutableStateOf(false) }

    DisposableEffect(transaction.id, transaction.audioMemoPath) {
        onDispose { mediaPlayer?.release() }
    }

    fun stopVoiceNote() {
        mediaPlayer?.let { runCatching { it.stop() }; it.release() }
        mediaPlayer = null
        isPlaying = false
    }

    fun playVoiceNote() {
        val path = transaction.audioMemoPath
        if (path.isNullOrBlank()) return
        stopVoiceNote()
        try {
            val player = MediaPlayer().apply {
                setDataSource(context, Uri.parse(path))
                setOnCompletionListener { release(); mediaPlayer = null; isPlaying = false }
                setOnErrorListener { _, _, _ -> release(); mediaPlayer = null; isPlaying = false; Toast.makeText(context, "ভয়েস নোট চালানো যায়নি।", Toast.LENGTH_SHORT).show(); true }
                prepare()
                start()
            }
            mediaPlayer = player
            isPlaying = true
        } catch (exception: Exception) {
            mediaPlayer = null
            isPlaying = false
            Toast.makeText(context, "ভয়েস নোট চালানো যায়নি।", Toast.LENGTH_SHORT).show()
        }
    }

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
        "MVR" -> if (usdToMvr > 0) transaction.amount * (usdToBdt / usdToMvr) else 0.0
        else -> 0.0
    }
    val isAnomalous = remember(transaction, comparisonTransactions, usdToBdt, usdToMvr) {
        comparisonTransactions.isNotEmpty() && AnomalyDetector.isAnomalous(transaction, comparisonTransactions, usdToBdt, usdToMvr)
    }

    fun shareTransaction() {
        val typeLabel = when (transaction.type) {
            "income" -> "আয়"
            "expense" -> "খরচ"
            "home_expense" -> "বাড়ির খরচ"
            else -> transaction.type.ifBlank { "লেনদেন" }
        }
        val walletLine = walletName.takeIf { it.isNotBlank() }?.let { "\nওয়ালেট: $it" } ?: ""
        val text = buildString {
            append("Amar Hisab\n")
            append("$typeLabel\n")
            append("বিবরণ: ${transaction.reason.ifBlank { transaction.category }}\n")
            append("ক্যাটাগরি: ${transaction.category}\n")
            append("তারিখ: ${displayTransactionDate(transaction.date)}\n")
            append("পরিমাণ: ${transaction.currency} ${formatMoney(transaction.amount)}\n")
            append("BDT মূল্য: ৳${formatMoney(bdt)}")
            append(walletLine)
            if (transaction.note.isNotBlank()) append("\nনোট: ${transaction.note}")
        }
        try {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }, "লেনদেন শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা যায়নি।", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.10f)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(transaction.reason.ifBlank { transaction.category }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (isAnomalous) {
                        Spacer(Modifier.width(7.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = warning.copy(alpha = 0.12f)) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "অস্বাভাবিক লেনদেন", tint = warning, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("অস্বাভাবিক", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = warning)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text("${transaction.category}  •  ${displayTransactionDate(transaction.date)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${transaction.currency} ${formatMoney(transaction.amount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                    }
                    if (walletName.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(walletName, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("৳${formatMoney(bdt)}", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Row {
                    if (transaction.audioMemoPath != null) {
                        IconButton(onClick = { if (isPlaying) stopVoiceNote() else playVoiceNote() }, modifier = Modifier.size(32.dp)) {
                            Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.Mic, contentDescription = if (isPlaying) "ভয়েস নোট বন্ধ করুন" else "ভয়েস নোট শুনুন", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (transaction.receiptImage != null) {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(transaction.receiptImage); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open receipt: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { shareTransaction() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "লেনদেন শেয়ার করুন", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onEdit(transaction) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(transaction) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = expenseRed)
                    }
                }
            }
        }
    }
}
