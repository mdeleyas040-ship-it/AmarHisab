package com.eleyas.expensetracker.model

data class FuelEntry(
    val id: String = "",
    val vehicleId: String = "",
    val date: String = "",
    val liters: Double = 0.0,
    val pricePerLiter: Double = 0.0,
    val totalCost: Double = 0.0,
    val odometer: Double = 0.0,
    val fuelStation: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)