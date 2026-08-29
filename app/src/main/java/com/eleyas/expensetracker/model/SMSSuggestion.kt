package com.eleyas.expensetracker.model

data class SMSSuggestion(
    val id: Long = System.currentTimeMillis(),
    val bankName: String,
    val senderName: String,
    val amount: Double,
    val transactionType: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "অন্যান্য",
    val rawSMS: String = ""
)
