package com.eleyas.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialTip(
    val id: Int,
    val quote: String,
    val author: String? = null,
    val category: String = "ফিন্যান্সিয়াল টিপস"
)

object DailyFinancialTips {

    private const val PREFS_NAME = "daily_tip_prefs"
    private const val KEY_ENABLED = "daily_tip_enabled"
    private const val KEY_LAST_SHOWN_DATE = "last_tip_shown_date_"

    val tipsList = listOf(
        FinancialTip(
            1,
            "সঞ্চয় করার পর যা অবশিষ্ট থাকে তা খরচ করুন, খরচ করার পর যা অবশিষ্ট থাকে তা সঞ্চয় করবেন না।",
            "ওয়ারেন বাফেট",
            "সঞ্চয় মন্ত্র"
        ),
        FinancialTip(
            2,
            "প্রয়োজনের অতিরিক্ত জিনিস কেনা এড়িয়ে চলুন। আজ যা অপ্রয়োজনীয় জিনিসে খরচ করবেন, কাল হয়তো প্রয়োজনীয় জিনিস বিক্রি করতে হবে।",
            "বেনজামিন ফ্র্যাঙ্কলিন",
            "স্মার্ট ব্যয়"
        ),
        FinancialTip(
            3,
            "একটি বাজেট আপনাকে বলে আপনার টাকা কোথায় যাচ্ছে, ভাবার বদলে যে টাকা কোথায় গেল!",
            "জন সি. ম্যাক্সওয়েল",
            "বাজেটিং"
        ),
        FinancialTip(
            4,
            "বিনিয়োগ করার সবচেয়ে সেরা সময় ছিল ২০ বছর আগে, দ্বিতীয় সেরা সময় হলো আজ।",
            "চীনা প্রবাদ",
            "বিনিয়োগ"
        ),
        FinancialTip(
            5,
            "ছোট ছোট খরচ সম্পর্কে সতর্ক থাকুন। একটি ছোট ছিদ্র যেমন বড় জাহাজ ডুবিয়ে দিতে পারে, তেমনি ছোট ছোট অপ্রয়োজনীয় খরচ আপনার বাজেট নষ্ট করতে পারে।",
            "বেনজামিন ফ্র্যাঙ্কলিন",
            "ব্যয় নিয়ন্ত্রণ"
        ),
        FinancialTip(
            6,
            "আয়ের সীমার নিচে জীবনযাপন করা কৃপণতা নয়; এটি মানসিক শান্তি ও ভবিষ্যৎ আর্থিক সুরক্ষার মূল চাবিকাঠি।",
            category = "আর্থিক স্বাধীনতা"
        ),
        FinancialTip(
            7,
            "প্রতি মাসে আয়ের পর পরই নিজের জন্য অন্তত ২০% সঞ্চয়ের অংশ আলাদা করে রাখুন (Pay Yourself First)।",
            category = "সঞ্চয় মন্ত্র"
        ),
        FinancialTip(
            8,
            "জরুরি পরিস্থিতির জন্য (Emergency Fund) অন্তত ৩ থেকে ৬ মাসের জীবনযাত্রার ব্যয় একটি নিরাপদ ফান্ডে আলাদা রাখুন।",
            category = "জরুরি তহবিল"
        ),
        FinancialTip(
            9,
            "আবেগ দিয়ে কেনাকাটা করা এড়িয়ে চলুন; কোনো কিছু কেনার আগে ২৪ ঘণ্টা অপেক্ষা করুন এবং নিজেকে প্রশ্ন করুন — এটি কি আসলেই প্রয়োজন?",
            category = "কেনাকাটা টিপস"
        ),
        FinancialTip(
            10,
            "দামি জিনিস এবং প্রয়োজনীয় জিনিসের মধ্যে পার্থক্য করতে শেখাই অর্থ ব্যবস্থাপনার প্রথম ও গুরুত্বপূর্ণ ধাপ।",
            category = "স্মার্ট ব্যয়"
        ),
        FinancialTip(
            11,
            "যে টাকা অলস পড়ে থাকে তা মূল্যস্ফীতিতে মান হারায়; সঠিক ও নিরাপদ জায়গায় বিনিয়োগ টাকা বৃদ্ধিতে সাহায্য করে।",
            category = "বিনিয়োগ"
        ),
        FinancialTip(
            12,
            "ঋণ বা লোন নেওয়ার আগে ভাবুন — এটি কি নতুন কোনো আয় বা সম্পদ সৃষ্টি করছে, নাকি কেবল দায় বাড়াচ্ছে?",
            category = "ঋণ ব্যবস্থাপনা"
        ),
        FinancialTip(
            13,
            "আয়ের একাধিক উৎস তৈরি করার চেষ্টা করুন। কেবল একটি মাত্র আয়ের ওপর নির্ভর করা ঝুঁকিপূর্ণ।",
            "ওয়ারেন বাফেট",
            "আয় বৃদ্ধি"
        ),
        FinancialTip(
            14,
            "অভিজ্ঞতায় ও নিজের দক্ষতায় বিনিয়োগ করুন। জ্ঞান ও দক্ষতা হলো এমন সম্পদ যা কেউ আপনার থেকে কেড়ে নিতে পারবে না।",
            category = "আত্ম-উন্নয়ন"
        ),
        FinancialTip(
            15,
            "টাকা উপার্জনের চেয়ে টাকা ধরে রাখা ও তা সঠিকভাবে পরিচালনা করা বেশি গুরুত্বপূর্ণ।",
            "রবার্ট কিওসাকি",
            "অর্থ ব্যবস্থাপনা"
        ),
        FinancialTip(
            16,
            "আপনার বাজেটে দৈনিক বা সাপ্তাহিক খরচের নির্দিষ্ট সীমা রাখুন এবং তা কঠোরভাবে মেনে চলুন।",
            category = "বাজেটিং"
        ),
        FinancialTip(
            17,
            "অব্যবহৃত মোবাইল অ্যাপ সাবস্ক্রিপশন ও ডিজিটাল সার্ভিস নিয়মিত চেক করুন এবং যা প্রয়োজন নেই তা বন্ধ করে দিন।",
            category = "ব্যয় নিয়ন্ত্রণ"
        ),
        FinancialTip(
            18,
            "অর্থ অপচয় বন্ধ করা নতুন আয় করার মতোই মূল্যবান।",
            "প্রবাদ",
            "সঞ্চয় মন্ত্র"
        ),
        FinancialTip(
            19,
            "আসল সম্পদ হলো যা আপনি দেখতে পান না — অর্জিত অর্থ খরচ না করে ধরে রাখার ক্ষমতাই আসল সম্পদ।",
            "মরগান হাউসেল",
            "সম্পদ ভাবনা"
        ),
        FinancialTip(
            20,
            "বিনিয়োগে বৈচিত্র্য আনুন (Diversification); আপনার সব সম্পদ এক জায়গায় না রেখে ভিন্ন ভিন্ন মাধ্যমে রাখুন।",
            "পল স্যামুয়েলসন",
            "বিনিয়োগ"
        ),
        FinancialTip(
            21,
            "উচ্চ সুদের ঋণ দ্রুত পরিশোধ করাই হলো সবচেয়ে ভালো নিশ্চিত রিটার্ন পাওয়ার বিনিয়োগ।",
            category = "ঋণ ব্যবস্থাপনা"
        ),
        FinancialTip(
            22,
            "প্রতিটি খরচের হিসাব লিখে রাখুন; নিয়মিত হিসাব রাখলে অপ্রয়োজনীয় খরচের হাত থেকে সহজে রেহাই পাওয়া যায়।",
            category = "হিসাব নিকাশ"
        ),
        FinancialTip(
            23,
            "অন্যকে দেখানোর জন্য বা লোকদেখানো জীবনযাত্রার জন্য টাকা খরচ করবেন না — এটি আপনার আর্থিক স্বাধীনতা নষ্ট করে।",
            category = "জীবনধারা"
        ),
        FinancialTip(
            24,
            "লক্ষ্যবিহীন সঞ্চয় স্থায়ী হয় না; নির্দিষ্ট আর্থিক লক্ষ্য (যেমন: বাড়ি, পড়াশোনা, অবসর) ঠিক করে সঞ্চয় শুরু করুন।",
            category = "আর্থিক লক্ষ্য"
        ),
        FinancialTip(
            25,
            "বিনিয়োগ বা সঞ্চয় শুরু করতে বড় অঙ্কের টাকার প্রয়োজন নেই, ক্ষুদ্র ক্ষুদ্র সঞ্চয় থেকেই বড় তহবিল তৈরি হয়।",
            category = "সঞ্চয় মন্ত্র"
        ),
        FinancialTip(
            26,
            "ক্রেডিট কার্ড বা বাকিতে কেনাকাটায় অত্যন্ত সতর্ক থাকুন; নির্দিষ্ট সময়ের মধ্যে বিল পরিশোধ না করলে সুদের বোঝা বাড়ে।",
            category = "স্মার্ট কেনাকাটা"
        ),
        FinancialTip(
            27,
            "কম বয়সে সঞ্চয় ও বিনিয়োগ শুরু করলে পাওয়ার অফ কম্পাউন্ডিং (Compounding)-এর জাদুকরী সুবিধা পাওয়া যায়।",
            category = "কম্পাউন্ডিং"
        ),
        FinancialTip(
            28,
            "আর্থিক জীবন সুশৃঙ্খল রাখতে প্রতি মাসের শুরুতে বাজেট তৈরি করুন এবং মাসের শেষে তা বিশ্লেষণ করুন।",
            category = "বাজেটিং"
        ),
        FinancialTip(
            29,
            "টাকা উপার্জনের দক্ষতার পাশাপাশি তা ধরে রাখার মানসিক শৃঙ্খলা গড়ে তুলুন।",
            category = "অর্থ ব্যবস্থাপনা"
        ),
        FinancialTip(
            30,
            "আর্থিক সচেতনতা ও শিক্ষা বাড়াতে নিয়মিত ফিন্যান্সিয়াল বই, বিষয়ভিত্তিক নিবন্ধ ও নিবন্ধিত টিপস পড়ুন।",
            category = "জ্ঞানার্জন"
        )
    )

    fun isDailyTipEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setDailyTipEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun shouldShowDailyTip(context: Context, userId: String): Boolean {
        if (!isDailyTipEnabled(context)) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastShown = prefs.getString(KEY_LAST_SHOWN_DATE + userId, "")
        return lastShown != todayStr
    }

    fun markDailyTipShown(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString(KEY_LAST_SHOWN_DATE + userId, todayStr).apply()
    }

    fun getTodayTip(): FinancialTip {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % tipsList.size
        return tipsList[index]
    }

    fun getRandomTip(currentIndex: Int = -1): FinancialTip {
        if (tipsList.size <= 1) return tipsList.first()
        var newIndex: Int
        do {
            newIndex = tipsList.indices.random()
        } while (newIndex == currentIndex)
        return tipsList[newIndex]
    }
}
