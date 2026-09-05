package com.brightcell.shield

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.File

data class ApkAnalysisResult(
    val fileName: String,
    val packageName: String,
    val appName: String,
    val versionName: String,
    val permissions: List<String>,
    val isValidApk: Boolean,
    val errorMessage: String? = null
)

class ApkAnalyzer(
    private val context: Context
) {

    @Suppress("DEPRECATION")
    fun analyzeApk(
        apkFile: File
    ): ApkAnalysisResult {

        if (!apkFile.exists()) {
            return createErrorResult(
                apkFile.name,
                "APK file not found"
            )
        }

        if (!apkFile.name.endsWith(
                ".apk",
                ignoreCase = true
            )
        ) {
            return createErrorResult(
                apkFile.name,
                "Selected file is not an APK"
            )
        }

        return try {

            val packageManager =
                context.packageManager

            val packageInfo =
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.GET_PERMISSIONS
                )

            if (packageInfo == null) {
                return createErrorResult(
                    apkFile.name,
                    "Unable to read APK metadata"
                )
            }

            val applicationInfo =
                packageInfo.applicationInfo

            applicationInfo?.sourceDir =
                apkFile.absolutePath

            applicationInfo?.publicSourceDir =
                apkFile.absolutePath

            val appName =
                getApplicationName(
                    packageManager,
                    applicationInfo,
                    packageInfo.packageName
                )

            val permissions =
                packageInfo.requestedPermissions
                    ?.toList()
                    ?: emptyList()

            ApkAnalysisResult(
                fileName = apkFile.name,
                packageName = packageInfo.packageName,
                appName = appName,
                versionName =
                    packageInfo.versionName ?: "Unknown",
                permissions = permissions,
                isValidApk = true
            )

        } catch (exception: Exception) {

            createErrorResult(
                apkFile.name,
                exception.message
                    ?: "APK analysis failed"
            )
        }
    }

    private fun getApplicationName(
        packageManager: PackageManager,
        applicationInfo: ApplicationInfo?,
        fallbackName: String
    ): String {

        return try {

            applicationInfo?.let {

                packageManager
                    .getApplicationLabel(it)
                    .toString()

            } ?: fallbackName

        } catch (_: Exception) {

            fallbackName
        }
    }

    private fun createErrorResult(
        fileName: String,
        message: String
    ): ApkAnalysisResult {

        return ApkAnalysisResult(
            fileName = fileName,
            packageName = "Unknown",
            appName = "Unknown",
            versionName = "Unknown",
            permissions = emptyList(),
            isValidApk = false,
            errorMessage = message
        )
    }
}
