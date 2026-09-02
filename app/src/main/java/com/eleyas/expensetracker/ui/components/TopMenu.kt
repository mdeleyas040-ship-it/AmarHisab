package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.ui.screens.HelpScreen

@Composable
fun AmarHisabTopMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onFamilyShare: () -> Unit,
    onCalendar: () -> Unit,
    onShoppingList: () -> Unit,
    onSettings: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Groups, contentDescription = "পরিবার শেয়ার", modifier = Modifier.size(22.dp))
            },
            text = { Text("পরিবার শেয়ার") },
            onClick = { onDismiss(); onFamilyShare() }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = "ক্যালেন্ডার", modifier = Modifier.size(22.dp))
            },
            text = { Text("ক্যালেন্ডার") },
            onClick = { onDismiss(); onCalendar() }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.ShoppingCart, contentDescription = "বাজারের ফর্দ", modifier = Modifier.size(22.dp))
            },
            text = { Text("বাজারের ফর্দ") },
            onClick = { onDismiss(); onShoppingList() }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.HelpOutline, contentDescription = "হেল্প ও গাইড", modifier = Modifier.size(22.dp))
            },
            text = { Text("হেল্প ও গাইড") },
            onClick = {
                onDismiss()
                showHelp = true
            }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Settings, contentDescription = "সেটিংস", modifier = Modifier.size(22.dp))
            },
            text = { Text("সেটিংস") },
            onClick = { onDismiss(); onSettings() }
        )
    }

    if (showHelp) {
        HelpScreen(onBack = { showHelp = false })
    }
}
