package com.eleyas.expensetracker.util

import android.content.Context
import com.eleyas.expensetracker.model.ShoppingItem
import org.json.JSONArray
import org.json.JSONObject

object ShoppingListStorage {

    private const val PREF_NAME = "shopping_list_storage"
    private const val KEY_ITEMS = "items"

    fun save(context: Context, userId: String, items: List<ShoppingItem>) {
        val prefs = context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("amount", item.amount)
                put("currency", item.currency)
                put("category", item.category)
                put("note", item.note)
                put("checked", item.checked)
                put("addedToExpense", item.addedToExpense)
            })
        }
        prefs.edit().putString(KEY_ITEMS, jsonArray.toString()).apply()
    }

    fun load(context: Context, userId: String): List<ShoppingItem> {
        val prefs = context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                ShoppingItem(
                    id = obj.optLong("id"),
                    name = obj.optString("name"),
                    amount = obj.optDouble("amount"),
                    currency = obj.optString("currency", "BDT"),
                    category = obj.optString("category", "অন্যান্য"),
                    note = obj.optString("note", ""),
                    checked = obj.optBoolean("checked"),
                    addedToExpense = obj.optBoolean("addedToExpense")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
