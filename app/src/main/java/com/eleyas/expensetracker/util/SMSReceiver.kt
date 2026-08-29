package com.eleyas.expensetracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.eleyas.expensetracker.model.SMSSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SMSReceiver : BroadcastReceiver() {
    companion object {
        private const val PREFS_NAME = "sms_suggestions"
        private var smsSuggestionCallback: ((SMSSuggestion) -> Unit)? = null

        fun setSMSSuggestionCallback(callback: (SMSSuggestion) -> Unit) {
            smsSuggestionCallback = callback
        }

        fun getSavedSuggestions(context: Context): List<SMSSuggestion> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString("suggestions", "[]") ?: "[]"
            return try {
                val suggestions = mutableListOf<SMSSuggestion>()
                val array = org.json.JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    suggestions.add(SMSSuggestion(
                        id = obj.getLong("id"),
                        bankName = obj.getString("bankName"),
                        senderName = obj.getString("senderName"),
                        amount = obj.getDouble("amount"),
                        transactionType = obj.getString("transactionType"),
                        description = obj.getString("description"),
                        timestamp = obj.getLong("timestamp"),
                        category = obj.getString("category"),
                        rawSMS = obj.getString("rawSMS")
                    ))
                }
                suggestions
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun saveSuggestion(context: Context, suggestion: SMSSuggestion) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = getSavedSuggestions(context).toMutableList()
            existing.add(suggestion)
            
            val array = org.json.JSONArray()
            existing.forEach { s ->
                array.put(org.json.JSONObject().apply {
                    put("id", s.id)
                    put("bankName", s.bankName)
                    put("senderName", s.senderName)
                    put("amount", s.amount)
                    put("transactionType", s.transactionType)
                    put("description", s.description)
                    put("timestamp", s.timestamp)
                    put("category", s.category)
                    put("rawSMS", s.rawSMS)
                })
            }
            
            prefs.edit().putString("suggestions", array.toString()).apply()
        }

        fun removeSuggestion(context: Context, suggestionId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = getSavedSuggestions(context).filter { it.id != suggestionId }
            
            val array = org.json.JSONArray()
            existing.forEach { s ->
                array.put(org.json.JSONObject().apply {
                    put("id", s.id)
                    put("bankName", s.bankName)
                    put("senderName", s.senderName)
                    put("amount", s.amount)
                    put("transactionType", s.transactionType)
                    put("description", s.description)
                    put("timestamp", s.timestamp)
                    put("category", s.category)
                    put("rawSMS", s.rawSMS)
                })
            }
            
            prefs.edit().putString("suggestions", array.toString()).apply()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        
        for (message in messages) {
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody

            val suggestion = SMSParser.parseSMS(sender, body) ?: continue

            saveSuggestion(context, suggestion)
            
            CoroutineScope(Dispatchers.Main).launch {
                smsSuggestionCallback?.invoke(suggestion)
            }
        }
    }
}
