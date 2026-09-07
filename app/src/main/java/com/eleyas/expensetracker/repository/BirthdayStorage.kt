package com.eleyas.expensetracker.repository

import android.content.Context
import com.eleyas.expensetracker.model.Birthday
import org.json.JSONArray
import org.json.JSONObject

object BirthdayStorage {

    private const val PREF_NAME = "birthday_storage"
    private const val KEY_BIRTHDAYS = "birthdays"

    private fun getPreferences(
        context: Context,
        userId: String
    ) = context.getSharedPreferences(
        "${PREF_NAME}_$userId",
        Context.MODE_PRIVATE
    )

    fun save(
        context: Context,
        birthdays: List<Birthday>,
        userId: String
    ) {
        val jsonArray = JSONArray()

        birthdays.forEach { birthday ->

            val json = JSONObject().apply {

                put("id", birthday.id)

                put(
                    "name",
                    birthday.name
                )

                put(
                    "birthDate",
                    birthday.birthDate
                )

                put(
                    "note",
                    birthday.note
                )

                put(
                    "createdAt",
                    birthday.createdAt
                )
            }

            jsonArray.put(json)
        }

        getPreferences(
            context,
            userId
        )
            .edit()
            .putString(
                KEY_BIRTHDAYS,
                jsonArray.toString()
            )
            .apply()
    }

    fun load(
        context: Context,
        userId: String
    ): List<Birthday> {

        val jsonString =
            getPreferences(
                context,
                userId
            )
                .getString(
                    KEY_BIRTHDAYS,
                    null
                )
                ?: return emptyList()

        return try {

            val jsonArray =
                JSONArray(jsonString)

            List(
                jsonArray.length()
            ) { index ->

                val json =
                    jsonArray.getJSONObject(
                        index
                    )

                Birthday(

                    id = json.optString(
                        "id"
                    ),

                    name = json.optString(
                        "name"
                    ),

                    birthDate = json.optString(
                        "birthDate"
                    ),

                    note = json.optString(
                        "note"
                    ),

                    createdAt = json.optLong(
                        "createdAt",
                        System.currentTimeMillis()
                    )
                )
            }

        } catch (
            e: Exception
        ) {

            emptyList()
        }
    }

    fun clear(
        context: Context,
        userId: String
    ) {
        getPreferences(
            context,
            userId
        )
            .edit()
            .remove(KEY_BIRTHDAYS)
            .apply()
    }
}