package com.eleyas.expensetracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PremiumSettingsScreen(
    modifier: Modifier,
    currentUserId: String,
    usdToBdt: Double,
    usdToMvr: Double,
    rateLoading: Boolean,
    rateError: String,
    subView: String?,
    profilePhotoUri: String?,
    isAdminUnlocked: Boolean,
    onPhotoClick: () -> Unit,
    onUnlockAdmin: () -> Unit,
    onSubViewChange: (String?) -> Unit,
    onRefreshRate: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onCopy: () -> Unit,
    onBudget: () -> Unit,
    onReset: () -> Unit,
    onEditName: () -> Unit,
    onCheckUpdate: () -> Unit,
    onAdminConsole: () -> Unit,
    onPushUpdate: () -> Unit,
    onLogout: () -> Unit,
    onDailyTipClick: () -> Unit = {}
) {
    PremiumSettingsPolish(
        modifier = modifier,
        currentUserId = currentUserId,
        usdToBdt = usdToBdt,
        usdToMvr = usdToMvr,
        rateLoading = rateLoading,
        rateError = rateError,
        subView = subView,
        profilePhotoUri = profilePhotoUri,
        isAdminUnlocked = isAdminUnlocked,
        onPhotoClick = onPhotoClick,
        onUnlockAdmin = onUnlockAdmin,
        onSubViewChange = onSubViewChange,
        onRefreshRate = onRefreshRate,
        onBackup = onBackup,
        onRestore = onRestore,
        onExport = onExport,
        onImport = onImport,
        onExportPdf = onExportPdf,
        onExportCsv = onExportCsv,
        onCopy = onCopy,
        onBudget = onBudget,
        onReset = onReset,
        onEditName = onEditName,
        onCheckUpdate = onCheckUpdate,
        onAdminConsole = onAdminConsole,
        onPushUpdate = onPushUpdate,
        onLogout = onLogout,
        onDailyTipClick = onDailyTipClick
    )
}
