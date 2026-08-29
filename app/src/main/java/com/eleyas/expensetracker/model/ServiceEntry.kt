package com.eleyas.expensetracker.model

data class ServiceEntry(
    val id: String = "",
    val vehicleId: String = "",
    val date: String = "",
    val serviceType: String = "",
    val odometer: Double = 0.0,
    val cost: Double = 0.0,
    val workshop: String = "",
    val partsChanged: String = "",
    val nextServiceDate: String = "",
    val nextServiceOdometer: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)