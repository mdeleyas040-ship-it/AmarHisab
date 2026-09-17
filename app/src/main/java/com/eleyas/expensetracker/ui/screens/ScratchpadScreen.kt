package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.ScratchNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchpadScreen(
    notes: List<ScratchNote>,
    onBack: () -> Unit,
    onSave: (ScratchNote) -> Unit,
    onDelete: (ScratchNote) -> Unit
) {
    var editingNote by remember { mutableStateOf<ScratchNote?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<ScratchNote?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("কুইক মেমো প্যাড", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingNote = null
                        showEditor = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "নতুন নোট")
                    }
                }
            )
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.NoteAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                )
                Spacer(Modifier.height(16.dp))
                Text("আপনার মেমো প্যাড খালি", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "হিসাবের ছোট নোট, রিমাইন্ডার বা যেকোনো দরকারি কথা এখানে লিখে রাখুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = { showEditor = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("প্রথম নোট লিখুন")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${notes.size}টি নোট",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                items(notes, key = { it.id }) { note ->
                    ScratchNoteCard(
                        note = note,
                        onEdit = {
                            editingNote = note
                            showEditor = true
                        },
                        onDelete = { noteToDelete = note }
                    )
                }
            }
        }
    }

    if (showEditor) {
        ScratchNoteEditor(
            note = editingNote,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("নোট মুছে ফেলবেন?") },
            text = { Text("এই নোটটি আর ফিরে পাওয়া যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(note)
                    noteToDelete = null
                }) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun ScratchNoteCard(
    note: ScratchNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(note.updatedAt) {
        SimpleDateFormat("dd MMM, yyyy • hh:mm a", Locale.getDefault()).format(Date(note.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifBlank { "শিরোনামহীন নোট" },
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "নোট সম্পাদনা")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "নোট মুছুন")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.content,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun ScratchNoteEditor(
    note: ScratchNote?,
    onDismiss: () -> Unit,
    onSave: (ScratchNote) -> Unit
) {
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var content by remember(note?.id) { mutableStateOf(note?.content.orEmpty()) }
    val canSave = content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) "নতুন কুইক মেমো" else "নোট সম্পাদনা") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("নোট লিখুন") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ScratchNote(
                            id = note?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            content = content.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
