package com.eleyas.expensetracker.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class WarningPopupData(
    val title: String,
    val message: String,
    val confirmText: String = "ঠিক আছে"
)

object WarningPopupManager {

    var currentWarning by mutableStateOf<WarningPopupData?>(null)
        private set

    fun show(
        title: String = "সতর্কতা",
        message: String,
        confirmText: String = "ঠিক আছে"
    ) {
        currentWarning = WarningPopupData(
            title = title,
            message = message,
            confirmText = confirmText
        )
    }

    fun dismiss() {
        currentWarning = null
    }
}