package com.eleyas.expensetracker.model

data class PartEntry(
    val id: String = "",
    val vehicleId: String = "",
    val date: String = "",
    val partName: String = "",
    val category: String = "",
    val quantity: Double = 1.0,
    val cost: Double = 0.0,
    val workshop: String = "",
    val odometer: Double = 0.0,
    val nextChangeOdometer: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)