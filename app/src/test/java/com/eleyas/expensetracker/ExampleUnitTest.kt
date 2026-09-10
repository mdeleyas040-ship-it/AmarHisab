package com.eleyas.expensetracker

import org.junit.Assert.*
import org.junit.Test
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.ui.components.MonthlyBalanceForecastCalculator
import com.eleyas.expensetracker.model.FinancialMilestone
import com.eleyas.expensetracker.ui.screens.FinancialMilestoneTimeline
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun monthlyForecast_projectsRemainingDaysAtCurrentNetPace() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse("15/09/2026")!!
        val transactions = listOf(
            Transaction(1, "income", 30000.0, "BDT", "", "", "01/09/2026"),
            Transaction(2, "expense", 10000.0, "BDT", "", "", "10/09/2026"),
            Transaction(3, "expense", 5000.0, "BDT", "", "", "16/09/2026")
        )

        val result = MonthlyBalanceForecastCalculator.calculate(
            balance = 50000.0,
            transactions = transactions,
            today = today
        )

        assertEquals(30000.0, result.currentMonthIncome, 0.01)
        assertEquals(10000.0, result.currentMonthExpense, 0.01)
        assertEquals(15, result.remainingDays)
        assertEquals(70000.0, result.projectedBalance, 0.01)
    }

    @Test
    fun financialMilestones_sortNewestDateFirst() {
        val milestones = listOf(
            FinancialMilestone(1, "First laptop", 50000.0, "BDT", "12/03/2024"),
            FinancialMilestone(2, "DPS complete", 100000.0, "BDT", "01/01/2026")
        )

        assertEquals(2, FinancialMilestoneTimeline.sorted(milestones).first().id)
    }

    @Test
    fun financialMilestoneDate_requiresCompleteValidDate() {
        assertTrue(FinancialMilestoneTimeline.isValidDate("01/09/2026"))
        assertFalse(FinancialMilestoneTimeline.isValidDate("31/02/2026"))
        assertFalse(FinancialMilestoneTimeline.isValidDate("01/09/2026 extra"))
    }
}