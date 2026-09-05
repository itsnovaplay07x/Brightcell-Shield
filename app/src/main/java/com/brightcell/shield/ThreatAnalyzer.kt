package com.brightcell.shield

data class ThreatAnalysisResult(
    val appName: String,
    val packageName: String,
    val riskScore: Int,
    val riskLevel: String,
    val verdict: String,
    val reasons: List<String>,
    val suspicious: Boolean
)

class ThreatAnalyzer {

    private val permissionAnalyzer = PermissionAnalyzer()

    fun analyzeApp(
        scannedApp: ScannedApp
    ): ThreatAnalysisResult {

        val permissionResult =
            permissionAnalyzer.analyzePermissions(
                scannedApp.permissions
            )

        var totalRiskScore =
            permissionResult.riskScore

        val reasons =
            permissionResult.reasons.toMutableList()

        /*
         * Additional heuristic checks
         */

        if (!scannedApp.isSystemApp) {

            totalRiskScore += 5

            reasons.add(
                "Third-party application detected"
            )
        }

        if (
            scannedApp.packageName.contains(
                "test",
                ignoreCase = true
            )
        ) {

            totalRiskScore += 5

            reasons.add(
                "Package name contains unusual test indicator"
            )
        }

        /*
         * Risk score limit
         */

        totalRiskScore =
            totalRiskScore.coerceAtMost(100)

        val riskLevel =
            when {

                totalRiskScore >= 75 -> "HIGH"

                totalRiskScore >= 40 -> "MEDIUM"

                totalRiskScore > 10 -> "LOW"

                else -> "SAFE"
            }

        val verdict =
            when (riskLevel) {

                "HIGH" ->
                    "Potentially Suspicious"

                "MEDIUM" ->
                    "Security Review Recommended"

                "LOW" ->
                    "Low Risk"

                else ->
                    "No Immediate Threat Detected"
            }

        val suspicious =
            totalRiskScore >= 40

        return ThreatAnalysisResult(
            appName = scannedApp.appName,
            packageName = scannedApp.packageName,
            riskScore = totalRiskScore,
            riskLevel = riskLevel,
            verdict = verdict,
            reasons = reasons,
            suspicious = suspicious
        )
    }
}
