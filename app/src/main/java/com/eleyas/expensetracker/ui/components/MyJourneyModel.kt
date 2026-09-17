package com.eleyas.expensetracker.ui.components

import android.content.Context

/** Personal journey settings used by the My Journey dashboard. */
data class MyJourneySettings(
    val arrivalDate: String = "",
    val monthlySalary: Double = 0.0,
    val monthlyExpense: Double = 0.0
)

object MyJourneyStorage {
    private const val PREF_PREFIX = "my_journey_"
    private const val KEY_ARRIVAL_DATE = "arrival_date"
    private const val KEY_MONTHLY_SALARY = "monthly_salary"
    private const val KEY_MONTHLY_EXPENSE = "monthly_expense"

    private fun prefs(context: Context, userId: String) =
        context.getSharedPreferences(PREF_PREFIX + userId, Context.MODE_PRIVATE)

    fun load(context: Context, userId: String): MyJourneySettings {
        val p = prefs(context, userId)
        return MyJourneySettings(
            arrivalDate = p.getString(KEY_ARRIVAL_DATE, "") ?: "",
            monthlySalary = p.getString(KEY_MONTHLY_SALARY, "0")?.toDoubleOrNull() ?: 0.0,
            monthlyExpense = p.getString(KEY_MONTHLY_EXPENSE, "0")?.toDoubleOrNull() ?: 0.0
        )
    }

    fun save(context: Context, userId: String, settings: MyJourneySettings) {
        prefs(context, userId).edit()
            .putString(KEY_ARRIVAL_DATE, settings.arrivalDate)
            .putString(KEY_MONTHLY_SALARY, settings.monthlySalary.toString())
            .putString(KEY_MONTHLY_EXPENSE, settings.monthlyExpense.toString())
            .apply()
    }
}
