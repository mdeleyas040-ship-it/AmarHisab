package com.eleyas.expensetracker.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Builds payment/return summaries and hands them to Android's messaging/share intents.
 * The app never sends a message silently; the user confirms the destination in the
 * messaging app or chooser.
 */
object MessageShareUtils {

    fun buildLoanPaymentMessage(
        personName: String,
        totalDue: Double,
        previousPaid: Double,
        paymentAmount: Double,
        paidAfter: Double,
        remainingAfter: Double,
        date: String
    ): String {
        return buildString {
            append("আসসালামু আলাইকুম $personName,\n\n")
            append("আজ আপনার ঋণের জন্য ৳${formatMoney(paymentAmount)} পরিশোধ করেছি।\n\n")
            append("মোট ঋণ: ৳${formatMoney(totalDue)}\n")
            append("আগে পরিশোধ: ৳${formatMoney(previousPaid)}\n")
            append("আজ পরিশোধ: ৳${formatMoney(paymentAmount)}\n")
            append("এ পর্যন্ত মোট পরিশোধ: ৳${formatMoney(paidAfter)}\n")
            append("বাকি: ৳${formatMoney(remainingAfter)}\n")
            append("তারিখ: ${displayLoanDate(date)}\n\n")
            append("— Amar Hisab")
        }
    }

    fun buildLendingReturnMessage(
        personName: String,
        totalLent: Double,
        previousReturned: Double,
        returnAmount: Double,
        returnedAfter: Double,
        remainingAfter: Double,
        date: String
    ): String {
        return buildString {
            append("আসসালামু আলাইকুম $personName,\n\n")
            append("আজ আপনার কাছ থেকে ৳${formatMoney(returnAmount)} ফেরত পেয়েছি।\n\n")
            append("মোট ধার: ৳${formatMoney(totalLent)}\n")
            append("আগে ফেরত: ৳${formatMoney(previousReturned)}\n")
            append("আজ ফেরত: ৳${formatMoney(returnAmount)}\n")
            append("এ পর্যন্ত মোট ফেরত: ৳${formatMoney(returnedAfter)}\n")
            append("বাকি পাওনা: ৳${formatMoney(remainingAfter)}\n")
            append("তারিখ: ${displayLoanDate(date)}\n\n")
            append("— Amar Hisab")
        }
    }

    fun shareToWhatsApp(context: Context, message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "WhatsApp পাওয়া যায়নি। অন্য অ্যাপ দিয়ে শেয়ার করুন।",
                Toast.LENGTH_SHORT
            ).show()
            share(context, message)
        }
    }

    fun share(context: Context, message: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            context.startActivity(
                Intent.createChooser(sendIntent, "মেসেজ শেয়ার করুন")
            )
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "শেয়ার করার কোনো অ্যাপ পাওয়া যায়নি।",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareToSms(context: Context, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", message)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "SMS অ্যাপ পাওয়া যায়নি।",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun copyToClipboard(context: Context, message: String) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("Amar Hisab payment message", message)
        )
    }
}
