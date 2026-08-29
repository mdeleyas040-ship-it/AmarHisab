package com.eleyas.expensetracker

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
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
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File

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

            val document = FirebaseFirestore
                .getInstance()
                .collection("config")
                .document("app_version")
                .get()
                .await()

            if (!document.exists()) {
                return null
            }

            val latestVersionCode =
                document.getLong("latestVersionCode") ?: return null

            val latestVersionName =
                document.getString("latestVersionName") ?: ""

            val updateUrl =
                document.getString("updateUrl") ?: ""

            val updateMessage =
                document.getString("updateMessage")
                    ?: "অ্যাপটির নতুন ভার্সন এসেছে।"

            val forceUpdate =
                document.getBoolean("forceUpdate") ?: false

            AppUpdateInfo(
                latestVersionCode = latestVersionCode,
                latestVersionName = latestVersionName,
                updateUrl = updateUrl,
                updateMessage = updateMessage,
                forceUpdate = forceUpdate
            )

        } catch (e: Exception) {

            android.util.Log.e(
                "AppUpdateChecker",
                "Update check failed",
                e
            )

            null
        }
    }

    fun getCurrentVersionCode(context: Context): Long {

        return try {

            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {

                packageInfo.longVersionCode

            } else {

                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

        } catch (e: Exception) {

            0L
        }
    }

    fun downloadAndInstall(
        context: Context,
        updateUrl: String,
        versionName: String
    ) {

        try {

            var downloadUrl = updateUrl

            // Google Drive share link থেকে direct download link তৈরি
            if (downloadUrl.contains("drive.google.com/file/d/")) {

                val regex =
                    Regex("drive\\.google\\.com/file/d/([^/]+)")

                val match = regex.find(downloadUrl)

                if (match != null) {

                    val fileId = match.groupValues[1]

                    downloadUrl =
                        "https://drive.google.com/uc?export=download&id=$fileId"
                }
            }

            android.util.Log.d(
                "AppUpdateChecker",
                "Download URL = $downloadUrl"
            )

            val fileName =
                "AmarHisab-$versionName.apk"

            val request =
                DownloadManager.Request(Uri.parse(downloadUrl))

            request.setTitle("Amar Hisab Update")

            request.setDescription(
                "Amar Hisab $versionName download হচ্ছে..."
            )

            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )

            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

            request.setMimeType("application/vnd.android.package-archive")

            val downloadManager =
                context.getSystemService(
                    Context.DOWNLOAD_SERVICE
                ) as DownloadManager

            val downloadId =
                downloadManager.enqueue(request)

            android.util.Log.d(
                "AppUpdateChecker",
                "Download started: $downloadId"
            )

            android.os.Handler(
                android.os.Looper.getMainLooper()
            ).postDelayed({

                try {

                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )

                    val apkFile =
                        File(downloadsDir, fileName)

                    if (!apkFile.exists()) {

                        android.util.Log.e(
                            "AppUpdateChecker",
                            "APK file not found"
                        )

                        return@postDelayed
                    }

                    val apkUri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            apkFile
                        )

                    val installIntent =
                        Intent(
                            Intent.ACTION_VIEW
                        ).apply {

                            setDataAndType(
                                apkUri,
                                "application/vnd.android.package-archive"
                            )

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }

                    context.startActivity(installIntent)

                } catch (e: Exception) {

                    android.util.Log.e(
                        "AppUpdateChecker",
                        "Install failed",
                        e
                    )
                }

            }, 8000)

        } catch (e: Exception) {

            android.util.Log.e(
                "AppUpdateChecker",
                "Download failed",
                e
            )
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

        android.util.Log.d(
            "AppUpdateChecker",
            "Firestore info = $info"
        )

        if (info != null) {

            val currentVersionCode =
                AppUpdateChecker.getCurrentVersionCode(
                    context
                )

            android.util.Log.d(
                "AppUpdateChecker",
                "Current versionCode = $currentVersionCode, Latest versionCode = ${info.latestVersionCode}"
            )

            if (info.latestVersionCode > currentVersionCode) {

                updateInfo = info

                showDialog = true
            }
        }
    }

    if (showDialog && updateInfo != null) {

        val info = updateInfo!!

        AlertDialog(

            onDismissRequest = {

                if (!info.forceUpdate) {
                    showDialog = false
                }
            },

            title = {
                Text("নতুন আপডেট পাওয়া গেছে 🎉")
            },

            text = {

                Text(
                    "Amar Hisab-এর নতুন ভার্সন ${info.latestVersionName} এসেছে।\n\n" +
                            info.updateMessage
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        AppUpdateChecker.downloadAndInstall(
                            context = context,
                            updateUrl = info.updateUrl,
                            versionName = info.latestVersionName
                        )

                        showDialog = false
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