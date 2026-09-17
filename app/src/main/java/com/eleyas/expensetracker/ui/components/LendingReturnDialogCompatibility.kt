package com.eleyas.expensetracker.ui.components

import androidx.compose.runtime.Composable
import com.eleyas.expensetracker.model.LendingAccount

/**
 * Compatibility overload for callers that also provide the currently
 * outstanding amount. The existing dialog calculates/uses its own state;
 * this parameter is accepted so older callers remain source-compatible.
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
