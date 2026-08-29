package com.eleyas.expensetracker

/**
 * Amar Hisab — Loan Interest Details
 *
 * আলাদা Interest button নয়।
 * এই model + helper গুলো প্রতিটি LoanAccount-এর detail-এর ভিতরে
 * Principal / Interest হিসাব দেখানোর জন্য ব্যবহার হবে।
 *
 * এই ফাইল MainActivity-এর existing LoanAccount বা LoanPayment
 * পরিবর্তন করে না।
 */

data class LoanInterestDetails(
    val principal: Double,
    val interestRate: Double = 0.0,
    val totalInterest: Double = 0.0,
    val totalPaid: Double = 0.0
) {
    val totalPayable: Double
        get() = principal + totalInterest

    val remaining: Double
        get() = (totalPayable - totalPaid).coerceAtLeast(0.0)
}

/**
 * Simple interest calculation.
 *
 * Example:
 * principal = 200000
 * annualRate = 12
 * months = 12
 *
 * totalInterest = 24000
 */
fun calculateSimpleLoanInterest(
    principal: Double,
    annualRatePercent: Double,
    months: Int
): Double {
    if (principal <= 0.0 || annualRatePercent <= 0.0 || months <= 0) {
        return 0.0
    }

    return principal *
            (annualRatePercent / 100.0) *
            (months / 12.0)
}

/**
 * Creates the complete loan summary.
 *
 * totalPaid এখানে principal + interest payment-এর মোট cash payment।
 * ফলে remaining = total payable - total paid.
 */
fun buildLoanInterestDetails(
    principal: Double,
    annualRatePercent: Double,
    months: Int,
    totalInterestOverride: Double? = null,
    totalPaid: Double = 0.0
): LoanInterestDetails {

    val interest = totalInterestOverride
        ?: calculateSimpleLoanInterest(
            principal = principal,
            annualRatePercent = annualRatePercent,
            months = months
        )

    return LoanInterestDetails(
        principal = principal.coerceAtLeast(0.0),
        interestRate = annualRatePercent.coerceAtLeast(0.0),
        totalInterest = interest.coerceAtLeast(0.0),
        totalPaid = totalPaid.coerceAtLeast(0.0)
    )
}

/**
 * UI-friendly labels.
 *
 * এগুলো Loan card/detail-এর ভিতরে ব্যবহার করা যাবে।
 */
fun loanInterestLabels(
    details: LoanInterestDetails
): List<Pair<String, Double>> {
    return listOf(
        "মূল ঋণ" to details.principal,
        "মোট সুদ" to details.totalInterest,
        "মোট পরিশোধযোগ্য" to details.totalPayable,
        "পরিশোধ হয়েছে" to details.totalPaid,
        "বাকি" to details.remaining
    )
}