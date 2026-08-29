package com.eleyas.expensetracker.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object SoundHapticHelper {

    // ToneGenerator বারবার create/release করলে native audio resource leak হতে পারে,
    // তাই একটাই instance রেখে ব্যবহার করা হয়। Creation ব্যর্থ হলে null থাকে।
    private var toneGenerator: ToneGenerator? = null

    // --------------------------------------------------
    // Sound & Haptic on/off setting (Settings screen toggle)
    // --------------------------------------------------
    private const val PREFS_NAME = "sound_haptic_prefs"
    private const val KEY_ENABLED = "sound_haptic_enabled"

    /** Sound/Vibration feedback চালু আছে কিনা (default: চালু) */
    fun isSoundHapticEnabled(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, true)
        } catch (_: Exception) { true }
    }

    fun setSoundHapticEnabled(context: Context, enabled: Boolean) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLED, enabled).apply()
        } catch (_: Exception) {}
    }

    fun playSuccessFeedback(context: Context, view: View? = null) {
        try {
            // Haptic Feedback / Vibration
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Satisfying short vibration pattern: 2 quick ticks
                    val timings = longArrayOf(0, 40, 50, 40)
                    val amplitudes = intArrayOf(0, 150, 0, 200)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }

            // View Click Feedback if available
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}
    }

    /**
     * নতুন লেনদেন (আয়/খরচ) সফলভাবে সেভ হলে feedback:
     * ছোট একটা satisfying confirmation tone + মৃদু vibration (existing 2-tick pattern)।
     *
     * Sound বা vibration যেকোনো কারণে ব্যর্থ হলেও app কখনো crash করবে না —
     * সবকিছু try-catch দিয়ে নিরাপদে মোড়া।
     */
    fun playTransactionSavedFeedback(context: Context) {
        if (!isSoundHapticEnabled(context)) return

        // মৃদু vibration
        playSuccessFeedback(context)

        // ছোট confirmation tone (ডিভাইসের media volume অনুযায়ী বাজবে)
        playTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    /**
     * লেনদেন সফলভাবে এডিট/update হলে হালকা feedback:
     * ১টা ছোট tick vibration + সংক্ষিপ্ত soft tone।
     */
    fun playTransactionUpdatedFeedback(context: Context) {
        if (!isSoundHapticEnabled(context)) return

        vibratePattern(context, longArrayOf(0, 30), intArrayOf(0, 120))
        playTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }

    /**
     * লেনদেন delete হলে কড়া feedback:
     * লম্বা শক্তিশালী vibration + নিচু "নেতিবাচক" tone — মুছে ফেলা হয়েছে বোঝায়।
     */
    fun playTransactionDeletedFeedback(context: Context) {
        if (!isSoundHapticEnabled(context)) return

        vibratePattern(context, longArrayOf(0, 120), intArrayOf(0, 255), fallbackMs = 150)
        playTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    // --------------------------------------------------
    // Internal helpers
    // --------------------------------------------------

    /** ছোট confirmation tone। ব্যর্থ হলে নীরবে fail করে, app crash করে না। */
    private fun playTone(tone: Int, durationMs: Int) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            }
            toneGenerator?.startTone(tone, durationMs)
        } catch (_: Exception) {
            // পরের বার আবার create করার সুযোগ রাখতে instance রিসেট করা হয়
            toneGenerator = null
        }
    }

    private fun vibratePattern(context: Context, timings: LongArray, amplitudes: IntArray, fallbackMs: Long = 100) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(fallbackMs)
                }
            }
        } catch (_: Exception) {}
    }
}
