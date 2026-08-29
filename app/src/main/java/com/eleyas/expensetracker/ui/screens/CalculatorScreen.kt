package com.eleyas.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.ui.theme.Green
import com.eleyas.expensetracker.util.calculateEMI
import com.eleyas.expensetracker.util.calculateSimpleEMI
import com.eleyas.expensetracker.util.calculateTotalInterest
import com.eleyas.expensetracker.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var principalText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var tenureText by remember { mutableStateOf("") }
    var isReducingBalance by remember { mutableStateOf(true) }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val annualRate = rateText.toDoubleOrNull() ?: 0.0
    val tenure = tenureText.toIntOrNull() ?: 0

    val emi = if (isReducingBalance) {
        calculateEMI(principal, annualRate, tenure)
    } else {
        calculateSimpleEMI(principal, annualRate, tenure)
    }

    val totalInterest = calculateTotalInterest(principal, emi, tenure).coerceAtLeast(0.0)
    val totalPayable = principal + totalInterest

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("কিস্তি ক্যালকুলেটর", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("লোনের তথ্য দিন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        OutlinedTextField(
                            value = principalText,
                            onValueChange = { principalText = it },
                            label = { Text("লোনের পরিমাণ (Principal)") },
                            placeholder = { Text("যেমন: 1,00,000") },
                            leadingIcon = { Text("৳", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it },
                            label = { Text("বার্ষিক সুদের হার (%)") },
                            placeholder = { Text("যেমন: 9.5") },
                            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = tenureText,
                            onValueChange = { tenureText = it },
                            label = { Text("লোনের মেয়াদ (মাস)") },
                            placeholder = { Text("যেমন: 12") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isReducingBalance,
                                onCheckedChange = { isReducingBalance = it }
                            )
                            Text(
                                "ব্যাংক পদ্ধতি (Reducing Balance)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (emi > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("মাসিক কিস্তি (EMI)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("৳${formatMoney(emi)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                            
                            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Green.copy(alpha = 0.2f))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                ResultBox("মোট সুদ", totalInterest, Color(0xFFF44336))
                                ResultBox("মোট আসল+সুদ", totalPayable, Color(0xFF1976D2))
                            }
                        }
                    }
                }
            }
            
            item {
                Text(
                    "সতর্কবার্তা: এটি একটি সাধারণ ক্যালকুলেটর। ব্যাংক ভেদে প্রসেসিং ফি বা অন্যান্য চার্জ আলাদা হতে পারে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ResultBox(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("৳${formatMoney(amount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
