package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 5.dp,
        shadowElevation = 10.dp
    ) {
        // Compact Telegram-style header.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(11.dp))

            Column {
                Text(
                    "Amar Hisab",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "আপনার ব্যক্তিগত হিসাব",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        TopMenuItem(Icons.Default.Groups, "পরিবার শেয়ার") {
            onDismiss(); onFamilyShare()
        }
        TopMenuItem(Icons.Default.CalendarMonth, "ক্যালেন্ডার") {
            onDismiss(); onCalendar()
        }
        TopMenuItem(Icons.Default.ShoppingCart, "বাজারের ফর্দ") {
            onDismiss(); onShoppingList()
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 3.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        TopMenuItem(Icons.Default.HelpOutline, "হেল্প ও গাইড") {
            onDismiss()
            showHelp = true
        }
        TopMenuItem(Icons.Default.Settings, "সেটিংস") {
            onDismiss(); onSettings()
        }
    }

    if (showHelp) {
        Dialog(
            onDismissRequest = { showHelp = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                HelpScreen(onBack = { showHelp = false })
            }
        }
    }
}

@Composable
private fun TopMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
        },
        text = {
            Text(
                title,
                fontSize = 15.sp
            )
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
