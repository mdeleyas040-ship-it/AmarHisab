package com.eleyas.expensetracker.model

data class ShoppingItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val amount: Double = 0.0,
    val currency: String = "BDT",
    val category: String = "অন্যান্য",
    val note: String = "",
    val checked: Boolean = false,
    val addedToExpense: Boolean = false
)
