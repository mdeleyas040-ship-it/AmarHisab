package com.eleyas.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import com.eleyas.expensetracker.model.Household
import com.eleyas.expensetracker.model.HouseholdMember
import org.json.JSONArray
import org.json.JSONObject

object HouseholdStorage {

    private const val PREFS_NAME = "household_prefs"

    private fun prefs(context: Context, userId: String): SharedPreferences =
        context.getSharedPreferences("${PREFS_NAME}_$userId", Context.MODE_PRIVATE)

    fun saveHouseholdId(context: Context, userId: String, householdId: String) {
        prefs(context, userId).edit().putString("household_id", householdId).apply()
    }

    fun loadHouseholdId(context: Context, userId: String): String? =
        prefs(context, userId).getString("household_id", null)?.takeIf { it.isNotBlank() }

    fun clearHousehold(context: Context, userId: String) {
        prefs(context, userId).edit().clear().apply()
    }

fun saveHouseholdCache(context: Context, userId: String, household: Household) {
    try {
        val members = JSONArray()
        household.members.forEach { m ->
            members.put(JSONObject().apply {
                put("uid", m.uid); put("name", m.name); put("email", m.email)
                put("photoUrl", m.photoUrl); put("joinedAt", m.joinedAt)
            })
        }
        val json = JSONObject().apply {
            put("id", household.id); put("name", household.name); put("code", household.code)
            put("createdBy", household.createdBy); put("createdAt", household.createdAt)
            put("members", members)
        }
        prefs(context, userId).edit().putString("household_cache", json.toString()).apply()
    } catch (_: Exception) {}
}

fun loadHouseholdCache(context: Context, userId: String): Household? = try {
    val raw = prefs(context, userId).getString("household_cache", null) ?: return null
    val o = JSONObject(raw)
    val members = mutableListOf<HouseholdMember>()
    val arr = o.optJSONArray("members")
    if (arr != null) for (i in 0 until arr.length()) {
        val m = arr.getJSONObject(i)
        members.add(
            HouseholdMember(
                uid = m.optString("uid"),
                name = m.optString("name"),
                email = m.optString("email"),
                photoUrl = m.optString("photoUrl"),
                joinedAt = m.optLong("joinedAt")
            )
        )
    }
    Household(
        id = o.getString("id"),
        name = o.getString("name"),
        code = o.getString("code"),
        createdBy = o.optString("createdBy"),
        createdAt = o.optLong("createdAt"),
        members = members
    )
} catch (_: Exception) { null }
}