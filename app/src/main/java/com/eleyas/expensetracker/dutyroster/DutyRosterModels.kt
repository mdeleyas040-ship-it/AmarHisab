package com.eleyas.expensetracker.dutyroster

data class DutyRoster(
    val uploadedAt: Long,
    val sourceName: String,
    val myName: String,
    val notificationHour: Int,
    val notificationMinute: Int,
    val days: List<RosterDay>
)

data class RosterDay(
    val dateIso: String,
    val displayDate: String,
    val duties: List<DutyEntry>
)

data class DutyEntry(
    val person: String,
    val duty: String
)
