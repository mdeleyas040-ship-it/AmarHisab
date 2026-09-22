package com.eleyas.expensetracker

import android.app.Application
import com.eleyas.expensetracker.meal.MealReminderManager

class AmarHisabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MealReminderManager.createNotificationChannel(this)
        MealReminderManager.scheduleAll(this)
    }
}
