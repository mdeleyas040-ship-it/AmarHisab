package com.eleyas.expensetracker.model

data class Vehicle(
    val id: String = "",
    val name: String = "",
    val model: String = "",
    val registrationNumber: String = "",
    val type: String = "Bike",
    val usageType: String = "Personal",
    val currentOdometer: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)