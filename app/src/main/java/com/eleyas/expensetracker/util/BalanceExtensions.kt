package com.eleyas.expensetracker.util

import com.eleyas.expensetracker.viewmodel.MainViewModel

/**
 * Same personal balance the rest of the app shows.
 */
val MainViewModel.sourceAwareBalance: Double
    get() = balance
