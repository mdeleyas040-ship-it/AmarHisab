package com.eleyas.expensetracker

data class NotificationItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: String = "budget",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)