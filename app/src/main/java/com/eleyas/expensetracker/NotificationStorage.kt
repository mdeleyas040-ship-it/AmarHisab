package com.eleyas.expensetracker

import android.content.Context
import com.eleyas.expensetracker.model.NotificationItem
import org.json.JSONArray
import org.json.JSONObject

object NotificationStorage {

    private const val PREF_NAME_BASE =
        "notification_storage"

    private const val KEY_NOTIFICATIONS =
        "notifications"

    fun save(
        context: Context,
        notifications: List<NotificationItem>,
        userId: String
    ) {

        val prefs =
            context.getSharedPreferences(
                "${PREF_NAME_BASE}_$userId",
                Context.MODE_PRIVATE
            )

        val jsonArray = JSONArray()

        notifications.forEach { item ->

            val json =
                JSONObject().apply {

                    put(
                        "id",
                        item.id
                    )

                    put(
                        "title",
                        item.title
                    )

                    put(
                        "message",
                        item.message
                    )

                    put(
                        "type",
                        item.type
                    )

                    put(
                        "timestamp",
                        item.timestamp
                    )

                    put(
                        "isRead",
                        item.isRead
                    )

                    // Smart / On This Day
                    // notification-এর source transaction
                    if (item.transactionId != null) {

                        put(
                            "transactionId",
                            item.transactionId
                        )
                    }
                }

            jsonArray.put(json)
        }

        prefs.edit()
            .putString(
                KEY_NOTIFICATIONS,
                jsonArray.toString()
            )
            .apply()
    }

    fun load(
        context: Context,
        userId: String
    ): List<NotificationItem> {

        val prefs =
            context.getSharedPreferences(
                "${PREF_NAME_BASE}_$userId",
                Context.MODE_PRIVATE
            )

        val jsonString =
            prefs.getString(
                KEY_NOTIFICATIONS,
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

                NotificationItem(

                    id =
                        json.optLong(
                            "id"
                        ),

                    title =
                        json.optString(
                            "title"
                        ),

                    message =
                        json.optString(
                            "message"
                        ),

                    type =
                        json.optString(
                            "type",
                            "budget"
                        ),

                    timestamp =
                        json.optLong(
                            "timestamp"
                        ),

                    isRead =
                        json.optBoolean(
                            "isRead",
                            false
                        ),

                    // পুরনো notification-এ
                    // transactionId না থাকলে null হবে
                    transactionId =
                        if (
                            json.has(
                                "transactionId"
                            )
                        ) {
                            json.optLong(
                                "transactionId"
                            )
                        } else {
                            null
                        }
                )
            }

        } catch (
            e: Exception
        ) {

            emptyList()
        }
    }
}