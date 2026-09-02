package com.eleyas.expensetracker.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Central Help registry.
 *
 * Every new feature should register its HelpTopic here (or from its own
 * feature module) when the feature is added. HelpScreen reads this registry,
 * so Help content no longer lives inside the screen UI itself.
 */
data class HelpTopic(
    val title: String,
    val icon: ImageVector,
    val what: String,
    val how: String,
    val note: String
)

object HelpRegistry {
    private val topics = linkedMapOf<String, HelpTopic>()

    init {
        registerDefaults()
    }

    fun register(topic: HelpTopic) {
        topics[topic.title] = topic
    }

    fun all(): List<HelpTopic> = topics.values.toList()

    private fun registerDefaults() {
        register(HelpTopic("Home", Icons.Default.Home, "আপনার আয়, খরচ, ব্যালেন্স ও গুরুত্বপূর্ণ financial summary এক জায়গায় দেখায়।", "Home খুলে summary cards, wallet balance এবং reminder card দেখুন। প্রয়োজন অনুযায়ী Income, Expense বা অন্য feature-এ যান।", "Home হলো app-এর মূল dashboard।"))
        register(HelpTopic("Income", Icons.Default.AttachMoney, "আপনার পাওয়া টাকা বা আয় record করার জায়গা।", "Income tab খুলে + চাপুন, amount, category, date ও note দিন, তারপর save করুন। পুরোনো entry edit বা delete করা যায়।", "ভুল amount বা date হলে entry edit করুন।"))
        register(HelpTopic("Expense", Icons.Default.Category, "আপনার খরচের হিসাব রাখে।", "Expense tab থেকে নতুন expense যোগ করুন এবং amount, category, date ও note দিন। প্রয়োজন হলে edit/delete করুন।", "নিয়মিত খরচ record করলে report আরও নির্ভুল হবে।"))
        register(HelpTopic("Calendar", Icons.Default.CalendarMonth, "তারিখ অনুযায়ী পুরোনো transaction খুঁজে দেখার সহজ উপায়।", "Menu → ক্যালেন্ডার খুলে একটি তারিখ নির্বাচন করুন এবং সেই দিনের transaction দেখুন।", "নির্দিষ্ট দিনের history যাচাই করতে ব্যবহার করুন।"))
        register(HelpTopic("Reports & Charts", Icons.Default.PieChart, "আপনার আয়-খরচের pattern ও summary বুঝতে সাহায্য করে।", "Reports tab খুলে summary এবং charts দেখুন। category ও সময় অনুযায়ী খরচের তুলনা করুন।", "Chart সিদ্ধান্ত নেওয়ার সহায়ক; মূল transaction list-ও যাচাই করুন।"))
        register(HelpTopic("Bank Loan", Icons.Default.AccountBalance, "Bank থেকে নেওয়া loan এবং payment history পরিচালনা করে।", "Loans tab থেকে Bank Loan যোগ করুন। loan details, payment এবং interest information প্রয়োজন অনুযায়ী update করুন।", "Payment record করলে remaining amount সঠিকভাবে বোঝা সহজ হয়।"))
        register(HelpTopic("Personal Loan", Icons.Default.VolunteerActivism, "ব্যক্তিগতভাবে ধার দেওয়া বা ধার নেওয়ার হিসাব রাখে।", "Loans section-এ person ও amount record করুন। পরে return/payment যোগ করে কত টাকা বাকি আছে দেখুন।", "ধার দেওয়ার ও ফেরত পাওয়ার record আলাদা করে রাখুন।"))
        register(HelpTopic("Vehicle & Fuel", Icons.Default.CarRental, "Vehicle এবং fuel-related হিসাব পরিচালনা করতে সাহায্য করে।", "Vehicle feature খুলে vehicle যোগ করুন। এরপর fuel entry এবং fuel history ব্যবহার করে খরচ record করুন।", "Fuel history নিয়মিত update করলে vehicle cost বুঝতে সুবিধা হবে।"))
        register(HelpTopic("Calculator", Icons.Default.Calculate, "দ্রুত হিসাব করার জন্য built-in calculator।", "Menu বা Loans-এর calculator option থেকে calculator খুলুন এবং প্রয়োজনীয় calculation করুন।", "এটি হিসাবের সহায়ক tool; transaction save করতে আলাদা entry তৈরি করতে হবে।"))
        register(HelpTopic("Notifications", Icons.Default.Notifications, "Daily reminder, recap এবং গুরুত্বপূর্ণ financial notification দেখায়।", "উপরের notification icon চাপুন। কোনো supported notification খুললে সংশ্লিষ্ট বিস্তারিত screen দেখা যাবে।", "Android notification permission বন্ধ থাকলে notification নাও আসতে পারে।"))
        register(HelpTopic("Smart Reminder", Icons.Default.Notifications, "গুরুত্বপূর্ণ transaction মনে করিয়ে দেওয়ার smart reminder system।", "Settings → Smart Reminder থেকে feature-এর available options দেখুন। Reminder notification এলে সেটি খুলে প্রয়োজনীয় তথ্য দেখুন।", "Notification permission এবং reminder setting ঠিক থাকা প্রয়োজন।"))
        register(HelpTopic("On This Day", Icons.Default.CalendarMonth, "আগের বছরের একই তারিখে করা transaction-এর স্মৃতি দেখায়।", "Home-এর ‘এই দিনে’ card অথবা On This Day notification খুলুন। সেখানে বছর অনুযায়ী পুরোনো transaction দেখা যাবে।", "এটি month-wise নয়; একই calendar day-এর পুরোনো record দেখায়।"))
        register(HelpTopic("Tips & Insights", Icons.Default.Lightbulb, "আপনার হিসাব থেকে useful financial tips ও insights দেখায়।", "Home-এর ticker বা Settings-এর Daily Financial Tips section থেকে tips দেখুন।", "Tip ও insight আপনার record-এর উপর ভিত্তি করে সহায়ক তথ্য দেয়।"))
        register(HelpTopic("Currency & Exchange Rate", Icons.Default.CurrencyExchange, "বিভিন্ন currency-এর হিসাব এবং exchange-rate related information পরিচালনা করে।", "Settings থেকে currency/rate options ব্যবহার করুন এবং প্রয়োজন হলে rate refresh করুন।", "Live rate-এর জন্য internet connection প্রয়োজন হতে পারে।"))
        register(HelpTopic("Backup & Restore", Icons.Default.Backup, "আপনার financial data নিরাপদে backup ও restore করতে সাহায্য করে।", "Settings → Backup/Restore থেকে backup export বা available backup restore করুন।", "গুরুত্বপূর্ণ data-এর backup নিয়মিত রাখা ভালো।"))
        register(HelpTopic("PDF & CSV Export", Icons.Default.Description, "আপনার হিসাব report হিসেবে PDF বা CSV file-এ বের করতে দেয়।", "Settings-এর export options থেকে PDF বা CSV নির্বাচন করে file save করুন।", "Export করার সময় একটি সহজে খুঁজে পাওয়া filename ব্যবহার করুন।"))
        register(HelpTopic("Account", Icons.Default.AccountCircle, "আপনার Amar Hisab account ও login state পরিচালনা করে।", "Account/Settings থেকে profile information দেখুন এবং প্রয়োজন হলে logout করুন।", "Logout করার আগে প্রয়োজনীয় data backup রাখা ভালো।"))
        register(HelpTopic("SMS Transaction", Icons.Default.Sms, "Supported bank/payment SMS থেকে transaction suggestion দিতে পারে।", "SMS permission দেওয়া থাকলে incoming supported SMS থেকে suggestion আসতে পারে। Suggestion যাচাই করে প্রয়োজন হলে transaction হিসেবে add করুন।", "SMS access-এর জন্য Android permission প্রয়োজন।"))
        register(HelpTopic("App Update", Icons.Default.SystemUpdate, "নতুন version available হলে app update করতে সাহায্য করে।", "Settings থেকে update check করুন। নতুন version থাকলে update option ব্যবহার করুন।", "Update করার আগে গুরুত্বপূর্ণ data backup রাখা নিরাপদ।"))
        register(HelpTopic("Troubleshooting", Icons.Default.HelpOutline, "কোনো feature কাজ না করলে প্রাথমিকভাবে কী কী যাচাই করবেন তার guide।", "প্রথমে permission, internet connection, feature setting এবং app restart যাচাই করুন। Backup/restore সমস্যা হলে file ও account ঠিক আছে কি না দেখুন।", "সমস্যা থাকলে একই action বারবার করার আগে data backup রাখুন।"))
    }
}
