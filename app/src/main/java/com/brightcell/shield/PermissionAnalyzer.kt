package com.brightcell.shield

data class PermissionRiskResult(
    val riskScore: Int,
    val riskLevel: String,
    val dangerousPermissions: List<String>,
    val reasons: List<String>
)

class PermissionAnalyzer {

    private val highRiskPermissions = mapOf(
        "android.permission.READ_SMS" to
                "Can read SMS messages",

        "android.permission.RECEIVE_SMS" to
                "Can receive SMS messages",

        "android.permission.SEND_SMS" to
                "Can send SMS messages",

        "android.permission.READ_CALL_LOG" to
                "Can access call history",

        "android.permission.WRITE_CALL_LOG" to
                "Can modify call history",

        "android.permission.RECORD_AUDIO" to
                "Can access microphone",

        "android.permission.CAMERA" to
                "Can access camera",

        "android.permission.ACCESS_FINE_LOCATION" to
                "Can access precise location",

        "android.permission.READ_CONTACTS" to
                "Can access contacts",

        "android.permission.WRITE_CONTACTS" to
                "Can modify contacts",

        "android.permission.REQUEST_INSTALL_PACKAGES" to
                "Can request APK installation",

        "android.permission.SYSTEM_ALERT_WINDOW" to
                "Can display over other apps"
    )

    fun analyzePermissions(
        permissions: List<String>
    ): PermissionRiskResult {

        var riskScore = 0

        val dangerousPermissions =
            mutableListOf<String>()

        val reasons =
            mutableListOf<String>()

        for (permission in permissions) {

            if (highRiskPermissions.containsKey(permission)) {

                dangerousPermissions.add(permission)

                reasons.add(
                    highRiskPermissions[permission]
                        ?: "Potentially sensitive permission"
                )

                riskScore += getRiskWeight(permission)
            }
        }

        riskScore = riskScore.coerceAtMost(100)

        val riskLevel = when {

            riskScore >= 75 -> "HIGH"

            riskScore >= 40 -> "MEDIUM"

            riskScore > 0 -> "LOW"

            else -> "SAFE"
        }

        return PermissionRiskResult(
            riskScore = riskScore,
            riskLevel = riskLevel,
            dangerousPermissions = dangerousPermissions,
            reasons = reasons
        )
    }

    private fun getRiskWeight(
        permission: String
    ): Int {

        return when (permission) {

            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS" -> 20

            "android.permission.SYSTEM_ALERT_WINDOW" -> 18

            "android.permission.REQUEST_INSTALL_PACKAGES" -> 18

            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG" -> 15

            "android.permission.RECORD_AUDIO" -> 10

            "android.permission.ACCESS_FINE_LOCATION" -> 10

            "android.permission.CAMERA" -> 8

            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS" -> 8

            else -> 5
        }
    }
}
