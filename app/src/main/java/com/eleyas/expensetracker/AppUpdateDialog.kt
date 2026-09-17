package com.eleyas.expensetracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppUpdateInfo(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val updateUrl: String,
    val updateMessage: String,
    val forceUpdate: Boolean
)

object AppUpdateChecker {

    suspend fun checkForUpdate(): AppUpdateInfo? {
        return try {
            val document =
                FirebaseFirestore
                    .getInstance()
                    .collection("config")
                    .document("app_version")
                    .get()
                    .await()

            if (!document.exists()) {
                return null
            }

            val latestVersionCode =
                document.getLong(
                    "latestVersionCode"
                ) ?: return null

            val latestVersionName =
                document.getString(
                    "latestVersionName"
                ) ?: ""

            val updateUrl =
                document.getString(
                    "updateUrl"
                ) ?: ""

            val updateMessage =
                document.getString(
                    "updateMessage"
                ) ?: "অ্যাপটির নতুন ভার্সন এসেছে।"

            val forceUpdate =
                document.getBoolean(
                    "forceUpdate"
                ) ?: false

            AppUpdateInfo(
                latestVersionCode =
                    latestVersionCode,
                latestVersionName =
                    latestVersionName,
                updateUrl = updateUrl,
                updateMessage =
                    updateMessage,
                forceUpdate =
                    forceUpdate
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getCurrentVersionCode(
        context: Context
    ): Long {
        return try {
            val packageInfo =
                context.packageManager
                    .getPackageInfo(
                        context.packageName,
                        0
                    )

            if (
                android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.P
            ) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (_: Exception) {
            0L
        }
    }
}

@Composable
fun AppUpdateDialog(
    context: Context
) {
    var updateInfo by remember {
        mutableStateOf<AppUpdateInfo?>(null)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val info =
            AppUpdateChecker.checkForUpdate()

        if (info != null) {
            val currentVersionCode =
                AppUpdateChecker.getCurrentVersionCode(
                    context
                )

            if (
                info.latestVersionCode >
                currentVersionCode
            ) {
                updateInfo = info
                showDialog = true
            }
        }
    }

    if (
        showDialog &&
        updateInfo != null
    ) {
        val info = updateInfo!!

        AlertDialog(
            onDismissRequest = {
                if (!info.forceUpdate) {
                    showDialog = false
                }
            },
            title = {
                Text(
                    "নতুন আপডেট পাওয়া গেছে 🎉"
                )
            },
            text = {
                Text(
                    "Amar Hisab-এর নতুন ভার্সন " +
                            "${info.latestVersionName} এসেছে।\n\n" +
                            info.updateMessage
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        info.updateUrl
                                    )
                                )

                            context.startActivity(
                                intent
                            )
                        } catch (_: Exception) {
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                if (!info.forceUpdate) {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("পরে করব")
                    }
                }
            }
        )
    }
}
