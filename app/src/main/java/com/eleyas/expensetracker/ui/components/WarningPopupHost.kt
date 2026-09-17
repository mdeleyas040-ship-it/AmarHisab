package com.eleyas.expensetracker.ui.components

import androidx.compose.runtime.Composable

@Composable
fun WarningPopupHost() {

    val warning = WarningPopupManager.currentWarning

    if (warning != null) {

        WarningDialog(
            title = warning.title,
            message = warning.message,
            confirmText = warning.confirmText,
            dismissText = "বাতিল",
            showDismissButton = false,
            onConfirm = {
                WarningPopupManager.dismiss()
            },
            onDismiss = {
                WarningPopupManager.dismiss()
            }
        )
    }
}