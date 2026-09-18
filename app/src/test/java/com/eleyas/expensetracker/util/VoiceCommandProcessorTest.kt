package com.eleyas.expensetracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceCommandProcessorTest {

    @Test
    fun bengaliDigitsAndMarketExpenseBecomeFoodExpense() {
        val result = VoiceCommandProcessor.processCommand("১০০ টাকা বাজার খরচ")
        assertEquals(100.0, result.amount!!, 0.0)
        assertEquals("expense", result.type)
        assertEquals("Food", result.category)
    }

    @Test
    fun groupedAmountIsParsedCorrectly() {
        val result = VoiceCommandProcessor.processCommand("১,৫০০ টাকা বেতন পেলাম")
        assertEquals(1500.0, result.amount!!, 0.0)
        assertEquals("income", result.type)
        assertEquals("Salary", result.category)
    }

    @Test
    fun explicitExpenseWinsWhenMixedWithIncomeKeyword() {
        val result = VoiceCommandProcessor.processCommand("বেতন থেকে ৫০০ টাকা খরচ")
        assertEquals(500.0, result.amount!!, 0.0)
        assertEquals("expense", result.type)
    }

    @Test
    fun englishExpenseCommandIsSupported() {
        val result = VoiceCommandProcessor.processCommand("100 taka spent on shopping")
        assertEquals(100.0, result.amount!!, 0.0)
        assertEquals("expense", result.type)
        assertEquals("Shopping", result.category)
    }

    @Test
    fun missingAmountStaysNullInsteadOfBecomingZero() {
        val result = VoiceCommandProcessor.processCommand("আজ বাজার খরচ করেছি")
        assertNull(result.amount)
        assertEquals("expense", result.type)
    }
}
