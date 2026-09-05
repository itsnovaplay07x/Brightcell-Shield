package com.brightcell.shield

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

data class ScannedApp(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val permissions: List<String>,
    val isSystemApp: Boolean
)

class ScannerEngine(
    private val context: Context
) {

    private val packageManager: PackageManager =
        context.packageManager

    fun scanInstalledApps(): List<ScannedApp> {

        val scannedApps = mutableListOf<ScannedApp>()

        val packages = getInstalledPackages()

        for (packageInfo in packages) {

            val applicationInfo =
                packageInfo.applicationInfo ?: continue

            val appName =
                packageManager
                    .getApplicationLabel(applicationInfo)
                    .toString()

            val packageName =
                packageInfo.packageName

            val versionName =
                packageInfo.versionName ?: "Unknown"

            val permissions =
                packageInfo.requestedPermissions?.toList()
                    ?: emptyList()

            val isSystemApp =
                (applicationInfo.flags and
                        ApplicationInfo.FLAG_SYSTEM) != 0

            scannedApps.add(
                ScannedApp(
                    appName = appName,
                    packageName = packageName,
                    versionName = versionName,
                    permissions = permissions,
                    isSystemApp = isSystemApp
                )
            )
        }

        return scannedApps
    }

    @Suppress("DEPRECATION")
    private fun getInstalledPackages(): List<PackageInfo> {

        return packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS
        )
    }
}
