package com.eleyas.expensetracker.model

data class SplitBillGroup(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val totalAmount: Double,
    val members: List<String>,
    val paidBy: String,
    val date: String,
    val note: String = ""
)
