package com.eleyas.expensetracker.ui.components

import com.eleyas.expensetracker.model.Transaction

/** Compatibility accessor for legacy share UI. */
val Transaction.note: String
    get() = ""
