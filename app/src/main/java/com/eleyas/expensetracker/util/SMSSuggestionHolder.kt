package com.eleyas.expensetracker.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.eleyas.expensetracker.model.SMSSuggestion

object SMSSuggestionHolder {
    var suggestions = mutableStateOf<List<SMSSuggestion>>(emptyList())
    
    fun updateSuggestions(context: Context) {
        suggestions.value = SMSReceiver.getSavedSuggestions(context)
    }
}
