package com.eleyas.expensetracker.model

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val requiredMonths: Int,
    val earned: Boolean,
    val progressMonths: Int
)
