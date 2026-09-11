package com.eleyas.expensetracker.ui.components

import androidx.compose.runtime.Composable
import com.eleyas.expensetracker.model.LendingAccount

/**
 * Compatibility overload for callers that calculate the remaining due amount
 * before opening the existing return dialog.
 * The caller remains responsible for enforcing the maximum return amount.
 */
@Composable
fun LendingReturnDialog(
    lending: LendingAccount,
    remainingDue: Double,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    LendingReturnDialog(
        lending = lending,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
