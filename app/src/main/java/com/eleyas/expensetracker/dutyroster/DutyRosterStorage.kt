package com.eleyas.expensetracker.dutyroster

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object DutyRosterStorage {
    private const val PREFS = "duty_roster"
    private const val KEY_ROSTER = "current_roster"
    private const val KEY_MY_NAME = "my_name"
    private const val KEY_HOUR = "notification_hour"
    private const val KEY_MINUTE = "notification_minute"

    fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, roster: DutyRoster) {
        val root = JSONObject().apply {
            put("uploadedAt", roster.uploadedAt)
            put("sourceName", roster.sourceName)
            put("myName", roster.myName)
            put("notificationHour", roster.notificationHour)
            put("notificationMinute", roster.notificationMinute)
            put("days", JSONArray().apply {
                roster.days.forEach { day ->
                    put(JSONObject().apply {
                        put("dateIso", day.dateIso)
                        put("displayDate", day.displayDate)
                        put("duties", JSONArray().apply {
                            day.duties.forEach { entry ->
                                put(JSONObject().apply {
                                    put("person", entry.person)
                                    put("duty", entry.duty)
                                })
                            }
                        })
                    })
                }
            })
        }

        prefs(context).edit()
            .putString(KEY_ROSTER, root.toString())
            .putString(KEY_MY_NAME, roster.myName)
            .putInt(KEY_HOUR, roster.notificationHour)
            .putInt(KEY_MINUTE, roster.notificationMinute)
            .apply()
    }

    fun load(context: Context): DutyRoster? {
        val raw = prefs(context).getString(KEY_ROSTER, null) ?: return null

        return runCatching {
            val root = JSONObject(raw)
            val daysJson = root.optJSONArray("days") ?: JSONArray()

            val days = buildList {
                for (i in 0 until daysJson.length()) {
                    val day = daysJson.getJSONObject(i)
                    val dutiesJson = day.optJSONArray("duties") ?: JSONArray()

                    val duties = buildList {
                        for (j in 0 until dutiesJson.length()) {
                            val item = dutiesJson.getJSONObject(j)
                            add(
                                DutyEntry(
                                    item.optString("person"),
                                    item.optString("duty")
                                )
                            )
                        }
                    }

                    add(
                        RosterDay(
                            day.optString("dateIso"),
                            day.optString("displayDate"),
                            duties
                        )
                    )
                }
            }

            DutyRoster(
                uploadedAt = root.optLong("uploadedAt"),
                sourceName = root.optString("sourceName"),
                myName = root.optString("myName"),
                notificationHour = root.optInt("notificationHour", 6),
                notificationMinute = root.optInt("notificationMinute", 30),
                days = days
            )
        }.getOrNull()
    }

    fun myName(context: Context): String =
        prefs(context).getString(KEY_MY_NAME, "") ?: ""

    fun setMyName(context: Context, name: String) {
        val trimmed = name.trim()
        prefs(context).edit().putString(KEY_MY_NAME, trimmed).apply()
        load(context)?.let {
            save(context, it.copy(myName = trimmed))
        }
    }

    fun notificationTime(context: Context): Pair<Int, Int> =
        prefs(context).getInt(KEY_HOUR, 6) to
                prefs(context).getInt(KEY_MINUTE, 30)

    fun setNotificationTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()

        load(context)?.let {
            save(
                context,
                it.copy(
                    notificationHour = hour,
                    notificationMinute = minute
                )
            )
        }
    }
}
