package com.eleyas.expensetracker.util

import android.content.Context
import com.eleyas.expensetracker.model.WishlistItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object WishlistStorage {

    private const val PREF_NAME = "wishlist_storage"
    private const val KEY_ITEMS = "items"

    fun save(context: Context, userId: String, items: List<WishlistItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("price", item.price)
                put("notifiedAffordable", item.notifiedAffordable)
            })
        }
        context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, array.toString())
            .apply()
    }

    fun load(context: Context, userId: String): List<WishlistItem> {
        val savedItems = context.getSharedPreferences(
            "${PREF_NAME}_$userId",
            Context.MODE_PRIVATE
        ).getString(KEY_ITEMS, null) ?: return emptyList()

        return try {
            val array = JSONArray(savedItems)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                WishlistItem(
                    id = item.optLong("id"),
                    name = item.optString("name"),
                    price = item.optDouble("price"),
                    notifiedAffordable = item.optBoolean("notifiedAffordable")
                )
            }
        } catch (_: JSONException) {
            emptyList()
        }
    }
}
