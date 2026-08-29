package com.eleyas.expensetracker.util

import kotlin.math.pow

/**
 * Standard EMI Calculation (Reducing Balance Method)
 * Formula: [P x R x (1+R)^N]/[(1+R)^N-1]
 * P = Principal, R = Monthly Interest Rate, N = Tenure in months
 */
fun calculateEMI(principal: Double, annualRate: Double, months: Int): Double {
    if (principal <= 0 || months <= 0) return 0.0
    if (annualRate <= 0) return principal / months
    
    val monthlyRate = annualRate / (12 * 100)
    val emi = (principal * monthlyRate * (1 + monthlyRate).pow(months.toDouble())) / 
              ((1 + monthlyRate).pow(months.toDouble()) - 1)
    return emi
}

/**
 * Simple Interest Calculation (Fixed Rate Method)
 */
fun calculateSimpleEMI(principal: Double, annualRate: Double, months: Int): Double {
    if (principal <= 0 || months <= 0) return 0.0
    val totalInterest = (principal * annualRate * (months.toDouble() / 12.0)) / 100.0
    return (principal + totalInterest) / months
}

fun calculateTotalInterest(principal: Double, emi: Double, months: Int): Double {
    return (emi * months) - principal
}
