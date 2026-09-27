package com.eleyas.expensetracker.util

import android.content.SharedPreferences
import com.eleyas.expensetracker.model.PersonProfile
import kotlin.random.Random

private const val PERSON_USER_IDS_KEY = "person_user_ids_v1"
private val AH_USER_ID_REGEX = Regex("AH-[0-9]{4}")

private fun loadUserIdMap(prefs: SharedPreferences): MutableMap<String, String> {
    val raw = prefs.getString(PERSON_USER_IDS_KEY, "") ?: ""
    if (raw.isBlank()) return mutableMapOf()
    return raw.split("|").mapNotNull { entry ->
        val parts = entry.split("=", limit = 2)
        if (parts.size == 2 && parts[0].isNotBlank() && parts[1].matches(AH_USER_ID_REGEX)) {
            parts[0] to parts[1]
        } else null
    }.toMap().toMutableMap()
}

private fun saveUserIdMap(prefs: SharedPreferences, map: Map<String, String>) {
    prefs.edit()
        .putString(
            PERSON_USER_IDS_KEY,
            map.entries.joinToString("|") { "${it.key}=${it.value}" }
        )
        .apply()
}

/** Returns the permanent AH-1234 ID for a profile, creating it once if needed. */
fun ensurePersonUserId(prefs: SharedPreferences, personId: String): String {
    require(personId.isNotBlank()) { "personId must not be blank" }

    val map = loadUserIdMap(prefs)
    map[personId]?.let { return it }

    val used = map.values.toHashSet()
    var candidate: String
    do {
        candidate = "AH-%04d".format(Random.nextInt(0, 10_000))
    } while (candidate in used)

    map[personId] = candidate
    saveUserIdMap(prefs, map)
    return candidate
}

fun userIdForPerson(prefs: SharedPreferences, person: PersonProfile): String =
    ensurePersonUserId(prefs, person.id)

fun findPersonProfileByNameOrUserId(
    prefs: SharedPreferences,
    people: List<PersonProfile>,
    query: String
): PersonProfile? {
    val q = query.trim()
    if (q.isBlank()) return null

    return people.firstOrNull {
        it.name.trim().equals(q, ignoreCase = true)
    } ?: people.firstOrNull {
        userIdForPerson(prefs, it).equals(q, ignoreCase = true)
    }
}
