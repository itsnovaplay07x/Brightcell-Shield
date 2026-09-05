package com.brightcell.shield

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class HashEngine {

    fun calculateSHA256(file: File): String {

        val digest = MessageDigest.getInstance("SHA-256")

        FileInputStream(file).use { input ->

            val buffer = ByteArray(8192)

            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {

                digest.update(
                    buffer,
                    0,
                    bytesRead
                )
            }
        }

        return digest.digest()
            .joinToString("") { byte ->
                "%02x".format(byte)
            }
    }

    fun isValidSHA256(hash: String): Boolean {

        return hash.matches(
            Regex("^[a-fA-F0-9]{64}$")
        )
    }
}
