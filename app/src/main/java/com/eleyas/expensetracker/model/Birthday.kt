package com.eleyas.expensetracker.model

data class Birthday(
    val id: String = "",
    val name: String = "",
    val birthDate: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)