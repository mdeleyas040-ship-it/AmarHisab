package com.eleyas.expensetracker

import android.app.PendingIntent
import android.os.Build
import android.service.quicksettings.TileService

class QuickEntryTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val intent = QuickEntryManager.entryIntent(this, QuickEntryManager.ENTRY_EXPENSE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                3,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
