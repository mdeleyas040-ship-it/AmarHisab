package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var expandedTitle by remember { mutableStateOf<String?>(null) }
    val helpTopics = HelpRegistry.all()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Help & Guide", fontWeight = FontWeight.ExtraBold)
                        Text("Amar Hisab ব্যবহার নির্দেশিকা", style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(32.dp))
                        Column {
                            Text("নতুন ব্যবহারকারীর জন্য", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text("যে feature শিখতে চান সেটি খুলুন। প্রতিটি topic-এ কী, কীভাবে ব্যবহার করবেন এবং গুরুত্বপূর্ণ note দেওয়া আছে।")
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            items(helpTopics) { topic ->
                val expanded = expandedTitle == topic.title
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { expandedTitle = if (expanded) null else topic.title },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(topic.icon, contentDescription = null, modifier = Modifier.size(24.dp))
                            Text(
                                topic.title,
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(if (expanded) "−" else "+", style = MaterialTheme.typography.titleLarge)
                        }

                        if (expanded) {
                            Spacer(Modifier.height(12.dp))
                            Text("কি এটি?", fontWeight = FontWeight.Bold)
                            Text(topic.what)
                            Spacer(Modifier.height(8.dp))
                            Text("কীভাবে ব্যবহার করবেন?", fontWeight = FontWeight.Bold)
                            Text(topic.how)
                            Spacer(Modifier.height(8.dp))
                            Text("গুরুত্বপূর্ণ নোট", fontWeight = FontWeight.Bold)
                            Text(topic.note)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
