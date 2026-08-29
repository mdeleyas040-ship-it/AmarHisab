package com.eleyas.expensetracker.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun HomeSummaryRow(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text("৳${formatMoney(amount)}", color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatMiniBox(modifier: Modifier, label: String, amount: Double, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.08f)) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("৳${formatMoney(amount)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun SmallSummaryCard(modifier: Modifier, title: String, amount: Double, color: Color, icon: ImageVector) {
    Card(modifier, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, color = color, fontSize = 13.sp)
            }
            Spacer(Modifier.height(5.dp))
            Text("৳${formatMoney(amount)}", color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("BDT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LoanInfoRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("৳${formatMoney(amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoanInfoRowText(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReportBar(label: String, amount: Double, maxAmount: Double, color: Color) {
    val progress = if (maxAmount > 0) (amount / maxAmount).toFloat() else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("৳${formatMoney(amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun CategoryBar(category: String, amount: Double, maxAmount: Double) {
    ReportBar(category, amount, maxAmount, MaterialTheme.colorScheme.primary)
}

@Composable
fun AdminConsoleDialog(onDismiss: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var message by remember { mutableStateOf("") }
    var isMaintenance by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        firestore.collection("config").document("app_notice").get().addOnSuccessListener { doc ->
            message = doc.getString("message") ?: ""
            isMaintenance = doc.getBoolean("maintenance") ?: false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Developer Console") },
        text = {
            Column {
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("App Notice Message") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMaintenance, onCheckedChange = { isMaintenance = it })
                    Text("Enable Maintenance Mode")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                firestore.collection("config").document("app_notice").set(mapOf("message" to message, "maintenance" to isMaintenance))
                onDismiss()
            }) { Text("Save Config") }
        }
    )
}
