package com.eleyas.expensetracker.util

import android.content.Context
import com.eleyas.expensetracker.model.SplitBillGroup
import org.json.JSONArray
import org.json.JSONObject

object SplitBillStorage {

    private const val PREF_NAME = "split_bill_storage"
    private const val KEY_SPLITS = "splits"

    fun saveSplits(context: Context, userId: String, splits: List<SplitBillGroup>) {
        val prefs = context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        splits.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("totalAmount", s.totalAmount)
                put("members", JSONArray(s.members))
                put("paidBy", s.paidBy)
                put("date", s.date)
                put("note", s.note)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SPLITS, jsonArray.toString()).apply()
    }

    fun loadSplits(context: Context, userId: String): List<SplitBillGroup> {
        val prefs = context.getSharedPreferences("${PREF_NAME}_$userId", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SPLITS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val memberArr = obj.getJSONArray("members")
                val members = List(memberArr.length()) { m -> memberArr.getString(m) }
                SplitBillGroup(
                    id = obj.optLong("id"),
                    title = obj.optString("title"),
                    totalAmount = obj.optDouble("totalAmount"),
                    members = members,
                    paidBy = obj.optString("paidBy"),
                    date = obj.optString("date"),
                    note = obj.optString("note")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
