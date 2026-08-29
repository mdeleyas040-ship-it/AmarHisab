package com.eleyas.expensetracker.util

import java.util.Locale

/**
 * Note/reason text থেকে ক্যাটাগরি অনুমান করে।
 * যেমন: "বাজার করলাম" → বাজার, "বাস ভাড়া" → Transport।
 * ম্যানুয়ালি ড্রপডাউন থেকে সিলেক্ট করলে সেটাই থাকবে —
 * নতুন করে টাইপ করলে আবার auto-detect হবে।
 */
object CategoryDetector {

    // প্রথম মিলটাই জেতে — তাই specific keyword গুলো আগে রাখা হয়েছে
    private val keywordMap: Map<String, Map<String, List<String>>> = mapOf(
        "income" to mapOf(
            "Salary" to listOf("salary", "বেতন", "payroll", "মাসিক বেতন"),
            "Freelance" to listOf("freelance", "upwork", "fiverr", "ফ্রিল্যান্স"),
            "Business" to listOf("ব্যবসা", "দোকান", "বিক্রি", "business", "profit", "লাভ", "sale"),
            "Tips" to listOf("tip", "টিপস", "bakshish"),
            "Bonus" to listOf("bonus", "বোনাস")
        ),
        "expense" to mapOf(
            "Transport" to listOf("বাস", "bus", "ভাড়া", "রিকশা", "rickshaw", "cng", "uber", "pathao", "ট্রেন", "train", "launch", "টিকিট", "ticket", "petrol", "gas station", "গাড়ি"),
            "Mobile" to listOf("মোবাইল", "recharge", "recharge", "internet pack", "bundle", "sim", "মেয়াদ"),
            "Bills" to listOf("বিল", "bill", "বিদ্যুৎ", "electricity", "gas bill", "পানির বিল", "wifi", "utility"),
            "Health" to listOf("doctor", "ডাক্তার", "medicine", "ঔষধ", "ওষুধ", "হাসপাতাল", "চিকিৎসা", "hospital", "clinic"),
            "Travel" to listOf("travel", "tour", "ভ্রমণ", "ঘুরতে", "hotel", "trip"),
            "Education" to listOf("school", "স্কুল", "college", "university", "টিউশন", "tuition", "fees", "বই", "book", "admission", "পড়াশোনা"),
            "Shopping" to listOf("shopping", "কেনাকাটা", "কাপড়", "জামা", "শাড়ি", "shirt", "pant", "শু", "shoe", "mall"),
            "Food" to listOf("খাবার", "খাই", "খেয়ে", "lunch", "dinner", "breakfast", "restaurant", "রেস্টুরেন্ট", "biryani", "snack", "coffee", "চা", "কফি", "খাওয়া", "হোটেলে খাই")
        ),
        "home_expense" to mapOf(
            "বাজার" to listOf("বাজার", "market", "grocery", "groceries", "মুদি", "সবজি", "মাছ", "মাংস", "চাল", "তেল", "মুরগি"),
            "বিদ্যুৎ বিল" to listOf("বিদ্যুৎ", "electricity", "current bill", "মিটার"),
            "চিকিৎসা" to listOf("চিকিৎসা", "doctor", "ডাক্তার", "medicine", "ঔষধ", "ওষুধ", "হাসপাতাল", "hospital"),
            "বাড়ি মেরামত" to listOf("মেরামত", "repair", "কারিগর", "রং", "paint"),
            "পড়াশোনা" to listOf("পড়াশোনা", "স্কুল", "school", "কলেজ", "college", "টিউশন", "tuition", "fees", "বই", "book", "admission"),
            "পরিবার" to listOf("পরিবার", "family", "বাচ্চা", "বাসায় পাঠানো"),
            "ভ্রমণ" to listOf("ভ্রমণ", "travel", "tour", "trip", "ঘুরতে")
        )
    )

    /** Note/reason থেকে ক্যাটাগরি অনুমান; কিছু না মিললে null। */
    fun detect(type: String, text: String): String? {
        if (text.isBlank()) return null
        val normalized = text.lowercase(Locale.ROOT)
        keywordMap[type]?.forEach { (category, keywords) ->
            keywords.forEach { keyword ->
                if (normalized.contains(keyword.lowercase(Locale.ROOT))) return category
            }
        }
        return null
    }
}