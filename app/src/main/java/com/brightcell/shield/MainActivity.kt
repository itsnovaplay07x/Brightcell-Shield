package com.brightcell.shield

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private var selectedFile by mutableStateOf<File?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filePicker = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {

                selectedFile = copyUriToCache(it)
            }
        }

        setContent {

            BrightcellShieldApp(
                context = this,
                selectedFile = selectedFile,
                onPickFile = {
                    filePicker.launch("*/*")
                }
            )
        }
    }

    private fun copyUriToCache(
    private fun copyUriToCache(
    uri: Uri
): File? {

    return try {

        val originalName =
            uri.lastPathSegment
                ?.substringAfterLast("/")
                ?.substringAfterLast(":")
                ?: "selected_file"

        val safeFileName =
            originalName.replace(
                Regex("[^a-zA-Z0-9._-]"),
                "_"
            )

        val fileName =
            "scan_" +
                    System.currentTimeMillis() +
                    "_" +
                    safeFileName

        val tempFile = File(
            cacheDir,
            fileName
        )

        contentResolver.openInputStream(uri)
            ?.use { input ->

                tempFile.outputStream()
                    .use { output ->

                        input.copyTo(output)
                    }
            }

        if (tempFile.exists() && tempFile.length() > 0) {

            tempFile

        } else {

            tempFile.delete()

            null
        }

    } catch (_: Exception) {

        null
    }
    }

@Composable
fun BrightcellShieldApp(
    context: Context,
    selectedFile: File?,
    onPickFile: () -> Unit
) {

    val backgroundColor = Color(0xFF06100C)
    val greenColor = Color(0xFF40EC82)

    var isScanningApps by remember {
        mutableStateOf(false)
    }

    var isScanningFile by remember {
        mutableStateOf(false)
    }

    var scanResults by remember {
        mutableStateOf<List<ThreatAnalysisResult>>(emptyList())
    }

    var fileResult by remember {
        mutableStateOf<FileScanResult?>(null)
    }

    var scannedCount by remember {
        mutableIntStateOf(0)
    }

    var threatCount by remember {
        mutableIntStateOf(0)
    }

    val scope = rememberCoroutineScope()

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "🛡 Brightcell Shield",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "NATIVE ANDROID ANTIVIRUS FOUNDATION",
                    fontSize = 10.sp,
                    color = greenColor,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                SecurityStatusCard(
                    scannedCount = scannedCount,
                    threatCount = threatCount,
                    isScanning =
                        isScanningApps || isScanningFile
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {

                        if (isScanningApps) return@Button

                        isScanningApps = true
                        scanResults = emptyList()
                        scannedCount = 0
                        threatCount = 0

                        scope.launch {

                            val results =
                                withContext(Dispatchers.Default) {

                                    val scanner =
                                        ScannerEngine(context)

                                    val analyzer =
                                        ThreatAnalyzer()

                                    scanner
                                        .scanInstalledApps()
                                        .map { app ->

                                            analyzer
                                                .analyzeApp(app)
                                        }
                                }

                            scanResults = results

                            scannedCount =
                                results.size

                            threatCount =
                                results.count {
                                    it.suspicious
                                }

                            isScanningApps = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isScanningApps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            if (isScanningApps)
                                "SCANNING INSTALLED APPS..."
                            else
                                "SCAN INSTALLED APPS",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Button(
                    onClick = onPickFile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF173524)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        "SELECT FILE / APK"
                    )
                }

                if (selectedFile != null) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Selected: ${selectedFile.name}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Button(
                    onClick = {

                        if (
                            selectedFile != null &&
                            !isScanningFile
                        ) {

                            isScanningFile = true
                            fileResult = null

                            scope.launch {

                                val result =
                                    withContext(
                                        Dispatchers.Default
                                    ) {

                                        FileScanner(context)
                                            .scanFile(
                                                selectedFile
                                            )
                                    }

                                fileResult = result

                                isScanningFile = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled =
                        selectedFile != null &&
                                !isScanningFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2A5C3C)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            if (isScanningFile)
                                "SCANNING FILE..."
                            else
                                "START FILE SCAN"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (fileResult != null) {

                    FileResultCard(
                        result = fileResult!!
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }

                if (scanResults.isNotEmpty()) {

                    Text(
                        text = "INSTALLED APP RESULTS",
                        color = greenColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(scanResults) { result ->

                            ScanResultCard(
                                result
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityStatusCard(
    scannedCount: Int,
    threatCount: Int,
    isScanning: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D2117)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text =
                    if (isScanning)
                        "SECURITY SCAN IN PROGRESS"
                    else
                        "SYSTEM SECURITY STATUS",
                color = Color.Gray,
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    if (isScanning)
                        "Scanning..."
                    else
                        "Protection Ready",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Apps scanned: $scannedCount",
                color = Color.LightGray
            )

            Text(
                text =
                    "Suspicious apps: $threatCount",
                color =
                    if (threatCount > 0)
                        Color.Red
                    else
                        Color.LightGray
            )
        }
    }
}

@Composable
fun FileResultCard(
    result: FileScanResult
) {

    val riskColor =
        when (result.riskLevel) {

            "HIGH" -> Color.Red
            "MEDIUM" -> Color.Yellow
            "LOW" -> Color(0xFFFFA500)
            "SAFE" -> Color(0xFF40EC82)
            else -> Color.Gray
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D2117)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "FILE SCAN RESULT",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = result.fileName,
                color = Color.LightGray
            )

            Text(
                text = result.verdict,
                color = riskColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Risk Score: ${result.riskScore}/100",
                color = riskColor
            )

            result.sha256?.let {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "SHA-256:",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Text(
                    text =
                        it.take(40) + "...",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            result.errorMessage?.let {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = it,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun ScanResultCard(
    result: ThreatAnalysisResult
) {

    val riskColor =
        when (result.riskLevel) {

            "HIGH" -> Color.Red
            "MEDIUM" -> Color.Yellow
            "LOW" -> Color(0xFFFFA500)
            else -> Color(0xFF40EC82)
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D2117)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = result.appName,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = result.packageName,
                color = Color.Gray,
                fontSize = 10.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "${result.verdict} • Risk ${result.riskScore}/100",
                color = riskColor,
                fontSize = 12.sp
            )
        }
    }
}
