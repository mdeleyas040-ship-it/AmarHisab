package com.eleyas.expensetracker.ui.screens

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ZAKAT_PREFS = "zakat_charity"
private const val CHARITY_KEY = "charity_records"
private const val ZAKAT_RATE = 0.025
private const val DEFAULT_NISAB_GRAMS = 87.48

private data class CharityRecord(
    val id: Long,
    val amount: Double,
    val recipient: String,
    val date: String
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ZakatCharityScreen(
    context: Context,
    userId: String,
    onBack: () -> Unit
) {
    val prefs = remember(userId) {
        context.getSharedPreferences("${ZAKAT_PREFS}_$userId", Context.MODE_PRIVATE)
    }
    var cashText by rememberSaveable(userId) { mutableStateOf("") }
    var goldGramsText by rememberSaveable(userId) { mutableStateOf("") }
    var goldPriceText by rememberSaveable(userId) { mutableStateOf("") }
    var debtText by rememberSaveable(userId) { mutableStateOf("") }
    var nisabText by rememberSaveable(userId) { mutableStateOf(DEFAULT_NISAB_GRAMS.toString()) }
    var charityAmountText by rememberSaveable(userId) { mutableStateOf("") }
    var charityRecipientText by rememberSaveable(userId) { mutableStateOf("") }
    var charityError by rememberSaveable(userId) { mutableStateOf<String?>(null) }
    var records by remember(userId) { mutableStateOf(loadCharityRecords(prefs)) }

    val cash = cashText.toAmount()
    val goldGrams = goldGramsText.toAmount()
    val goldPrice = goldPriceText.toAmount()
    val debt = debtText.toAmount()
    val nisabGrams = nisabText.toAmount().takeIf { it > 0 } ?: DEFAULT_NISAB_GRAMS
    val goldValue = goldGrams * goldPrice
    val netAssets = (cash + goldValue - debt).coerceAtLeast(0.0)
    val nisabValue = nisabGrams * goldPrice
    val zakat = if (goldPrice > 0 && netAssets >= nisabValue) netAssets * ZAKAT_RATE else 0.0
    val charityTotal = records.sumOf { it.amount }

    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("যাকাত ও চ্যারিটি", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumZakatHero(
                    zakat = zakat,
                    netAssets = netAssets,
                    nisabValue = nisabValue
                )
            }
            item {
                PremiumSectionCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ZakatSectionHeading(
                            icon = Icons.Default.Savings,
                            title = "বার্ষিক যাকাত হিসাব",
                            subtitle = "সম্পদের তথ্য দিন",
                            accent = PremiumGold
                        )
                        AmountField(cashText, { cashText = it }, "নগদ ও সঞ্চয় (৳)")
                        AmountField(goldGramsText, { goldGramsText = it }, "সোনার পরিমাণ (গ্রাম)")
                        AmountField(goldPriceText, { goldPriceText = it }, "সোনার দর (প্রতি গ্রাম ৳)")
                        AmountField(debtText, { debtText = it }, "বাদযোগ্য দেনা (৳)")
                        AmountField(nisabText, { nisabText = it }, "নিসাব (সোনার গ্রাম)", decimal = true)
                        Text(
                            "নিসাব পূর্ণ হলে যাকাতের হার ২.৫% প্রযোজ্য। সোনার দর না দিলে ফলাফল দেখানো হবে না।",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            item {
                PremiumSectionCard {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ZakatSectionHeading(
                            icon = Icons.Default.VolunteerActivism,
                            title = "চ্যারিটি/দান ট্র্যাক",
                            subtitle = "আপনার সদকার হিসাব গুছিয়ে রাখুন",
                            accent = MaterialTheme.colorScheme.primary
                        )
                        AmountField(
                            charityAmountText,
                            {
                                charityAmountText = it
                                charityError = null
                            },
                            "দানের পরিমাণ (৳)"
                        )
                        OutlinedTextField(
                            value = charityRecipientText,
                            onValueChange = {
                                charityRecipientText = it
                                charityError = null
                            },
                            label = { Text("প্রাপক বা উদ্দেশ্য") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                val amount = charityAmountText.toAmount()
                                if (amount > 0 && charityRecipientText.isNotBlank()) {
                                    val record = CharityRecord(
                                        id = System.currentTimeMillis(),
                                        amount = amount,
                                        recipient = charityRecipientText.trim(),
                                        date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                            .format(Date())
                                    )
                                    records = listOf(record) + records
                                    saveCharityRecords(prefs, records)
                                    charityAmountText = ""
                                    charityRecipientText = ""
                                    charityError = null
                                } else {
                                    charityError = "দানের পরিমাণ এবং প্রাপক/উদ্দেশ্য লিখুন।"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(7.dp))
                            Text("দান যোগ করুন")
                        }
                        charityError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        CharityTotal(total = charityTotal)
                    }
                }
            }
            if (records.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("দানের ইতিহাস", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${records.size}টি রেকর্ড",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    CharityEmptyState()
                }
            }
            items(records, key = { it.id }) { record ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(record.recipient, fontWeight = FontWeight.SemiBold)
                            Text(record.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            Text("৳${record.amount.asMoney()}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                records = records.filterNot { it.id == record.id }
                                saveCharityRecords(prefs, records)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "দান মুছুন", tint = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }
        }
    }
}

private val PremiumGold = Color(0xFFE8BC62)

@Composable
private fun PremiumZakatHero(
    zakat: Double,
    netAssets: Double,
    nisabValue: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF173E2A),
                            Color(0xFF101C16),
                            Color(0xFF0D100F)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Text(
                    "বার্ষিক হিসাব  •  যাকাতের হার ২.৫%",
                    color = PremiumGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("প্রদেয় যাকাত", color = Color(0xFF75E6A4), fontSize = 13.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "৳${zakat.asMoney()}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "বাংলাদেশি টাকা (BDT)",
                            color = Color.White.copy(alpha = 0.48f),
                            fontSize = 10.sp
                        )
                    }
                    Surface(
                        modifier = Modifier.height(54.dp).width(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = Color(0xFF0E6F4C).copy(alpha = 0.55f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color(0xFF00E878)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumHeroStat(
                        modifier = Modifier.weight(1f),
                        label = "নিট সম্পদ",
                        value = "৳${netAssets.asMoney()}"
                    )
                    PremiumHeroStat(
                        modifier = Modifier.weight(1f),
                        label = "নিসাব মূল্য",
                        value = "৳${nisabValue.asMoney()}"
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumHeroStat(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .padding(12.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun PremiumSectionCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun ZakatSectionHeading(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.height(40.dp).width(40.dp),
            shape = RoundedCornerShape(13.dp),
            color = accent.copy(alpha = 0.13f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent)
            }
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CharityTotal(total: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "এ পর্যন্ত মোট দান",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Text(
                "৳${total.asMoney()}",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
private fun CharityEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.VolunteerActivism,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("এখনও কোনো দানের রেকর্ড নেই", fontWeight = FontWeight.SemiBold)
                Text(
                    "প্রথম দানটি যোগ করলে এখানে দেখতে পাবেন।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

private fun String.toAmount(): Double = trim().replace(",", "").toDoubleOrNull() ?: 0.0

private fun Double.asMoney(): String =
    NumberFormat.getNumberInstance(Locale("bn", "BD")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(this)

private fun loadCharityRecords(prefs: android.content.SharedPreferences): List<CharityRecord> {
    val raw = prefs.getString(CHARITY_KEY, null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            CharityRecord(
                id = item.getLong("id"),
                amount = item.getDouble("amount"),
                recipient = item.getString("recipient"),
                date = item.getString("date")
            )
        }
    } catch (error: JSONException) {
        Log.w("ZakatCharity", "Unable to read saved charity records", error)
        emptyList()
    }
}

private fun saveCharityRecords(
    prefs: android.content.SharedPreferences,
    records: List<CharityRecord>
) {
    val array = JSONArray()
    records.forEach {
        array.put(
            org.json.JSONObject().apply {
                put("id", it.id)
                put("amount", it.amount)
                put("recipient", it.recipient)
                put("date", it.date)
            }
        )
    }
    prefs.edit().putString(CHARITY_KEY, array.toString()).apply()
}
