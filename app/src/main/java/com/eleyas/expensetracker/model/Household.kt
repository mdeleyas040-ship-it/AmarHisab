package com.eleyas.expensetracker.model

data class HouseholdMember(
    val uid: String,
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val joinedAt: Long = System.currentTimeMillis()
)

data class Household(
    val id: String,
    val name: String,
    val code: String,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<HouseholdMember> = emptyList()
)