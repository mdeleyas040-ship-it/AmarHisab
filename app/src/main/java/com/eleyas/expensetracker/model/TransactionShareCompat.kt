package com.eleyas.expensetracker.model

/**
 * Legacy share UI referenced a note field that is not part of the current
 * Transaction model. Keep the compatibility surface without changing the
 * persisted transaction schema.
 */
val Transaction.note: String
    get() = ""
