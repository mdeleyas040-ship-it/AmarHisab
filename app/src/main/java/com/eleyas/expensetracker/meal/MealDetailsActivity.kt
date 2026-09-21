package com.eleyas.expensetracker.meal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.ui.theme.AmarHisabTheme

class MealDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mealType = intent.getStringExtra(MealReminderManager.EXTRA_MEAL_TYPE) ?: "breakfast"

        setContent {
            AmarHisabTheme {
                MealDetailsScreen(mealType = mealType, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealDetailsScreen(
    mealType: String,
    onBack: () -> Unit
) {
    val (title, items) = when (mealType) {
        "breakfast" -> "🍳 সকালের সুষম খাবার" to listOf(
            "🥣 ওটস/চিড়া/রুটি — একটি শস্যজাতীয় খাবার",
            "🥚 ডিম — প্রোটিনের একটি উৎস",
            "🍌 কলা/আপেল/অন্য একটি ফল",
            "🥛 দুধ/দই — চাইলে যোগ করতে পারেন",
            "💧 পর্যাপ্ত পানি"
        )
        "lunch" -> "🍱 দুপুরের সুষম খাবার" to listOf(
            "🍚 ভাত/রুটি — প্রয়োজনমতো শস্যজাতীয় খাবার",
            "🐟 মাছ/মুরগি/ডাল — প্রোটিনের একটি উৎস",
            "🥗 ১–২ ধরনের সবজি",
            "🥒 সালাদ — শসা/টমেটো/অন্যান্য সবজি",
            "🍊 একটি ফল",
            "💧 পর্যাপ্ত পানি"
        )
        else -> "🍽️ রাতের সুষম খাবার" to listOf(
            "🍚 ভাত/রুটি — প্রয়োজনমতো শস্যজাতীয় খাবার",
            "🐟 মাছ/মুরগি/ডাল/ডিম — প্রোটিনের একটি উৎস",
            "🥗 ১–২ ধরনের সবজি",
            "🍲 হালকা স্যুপ/সবজি — চাইলে",
            "🍎 একটি ফল — চাইলে",
            "💧 পর্যাপ্ত পানি"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("আজকের খাবারের তালিকা") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "সুষম খাবারে শস্যজাতীয় খাবার, প্রোটিন, সবজি/ফল এবং পর্যাপ্ত পানি রাখার চেষ্টা করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
