package com.brightcell.shield

import android.content.Context
import java.io.File

data class FileScanResult(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val sha256: String?,
    val isApk: Boolean,
    val apkAnalysis: ApkAnalysisResult?,
    val permissionRisk: PermissionRiskResult?,
    val riskScore: Int,
    val riskLevel: String,
    val verdict: String,
    val errorMessage: String? = null
)

class FileScanner(
    private val context: Context
) {

    private val hashEngine = HashEngine()
    private val permissionAnalyzer = PermissionAnalyzer()
    private val apkAnalyzer = ApkAnalyzer(context)

    fun scanFile(
        file: File
    ): FileScanResult {

        if (!file.exists()) {

            return createErrorResult(
                file = file,
                message = "File not found"
            )
        }

        if (!file.isFile) {

            return createErrorResult(
                file = file,
                message = "Selected path is not a valid file"
            )
        }

        return try {

            val sha256 =
                hashEngine.calculateSHA256(file)

            val isApk =
                file.name.endsWith(
                    ".apk",
                    ignoreCase = true
                )

            var apkAnalysis: ApkAnalysisResult? = null

            var permissionRisk: PermissionRiskResult? = null

            var riskScore = 0

            if (isApk) {

                apkAnalysis =
                    apkAnalyzer.analyzeApk(file)

                if (apkAnalysis.isValidApk) {

                    permissionRisk =
                        permissionAnalyzer.analyzePermissions(
                            apkAnalysis.permissions
                        )

                    riskScore =
                        permissionRisk.riskScore

                } else {

                    riskScore = 30
                }
            }

            riskScore =
                riskScore.coerceIn(0, 100)

            val riskLevel =
                getRiskLevel(riskScore)

            val verdict =
                getVerdict(
                    riskLevel,
                    isApk,
                    apkAnalysis
                )

            FileScanResult(
                fileName = file.name,
                filePath = file.absolutePath,
                fileSize = file.length(),
                sha256 = sha256,
                isApk = isApk,
                apkAnalysis = apkAnalysis,
                permissionRisk = permissionRisk,
                riskScore = riskScore,
                riskLevel = riskLevel,
                verdict = verdict
            )

        } catch (exception: Exception) {

            createErrorResult(
                file = file,
                message =
                    exception.message
                        ?: "File scanning failed"
            )
        }
    }

    private fun getRiskLevel(
        score: Int
    ): String {

        return when {

            score >= 75 -> "HIGH"

            score >= 40 -> "MEDIUM"

            score > 0 -> "LOW"

            else -> "SAFE"
        }
    }

    private fun getVerdict(
        riskLevel: String,
        isApk: Boolean,
        apkAnalysis: ApkAnalysisResult?
    ): String {

        if (!isApk) {

            return "File analyzed successfully"
        }

        if (apkAnalysis?.isValidApk == false) {

            return "Invalid or unreadable APK"
        }

        return when (riskLevel) {

            "HIGH" ->
                "High-risk APK detected"

            "MEDIUM" ->
                "APK requires security review"

            "LOW" ->
                "Low-risk APK"

            else ->
                "No immediate threat detected"
        }
    }

    private fun createErrorResult(
        file: File,
        message: String
    ): FileScanResult {

        return FileScanResult(
            fileName = file.name,
            filePath = file.absolutePath,
            fileSize = 0,
            sha256 = null,
            isApk = false,
            apkAnalysis = null,
            permissionRisk = null,
            riskScore = 0,
            riskLevel = "ERROR",
            verdict = "Scan failed",
            errorMessage = message
        )
    }
}
