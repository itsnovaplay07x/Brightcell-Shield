package com.brightcell.shield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filePicker =
            registerForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->

                if (uri != null) {

                    val inputStream =
                        contentResolver.openInputStream(uri)

                    if (inputStream != null) {

                        val fileName =
                            uri.lastPathSegment
                                ?: "selected_file"

                        val tempFile =
                            File(
                                cacheDir,
                                fileName
                            )

                        inputStream.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        selectedFile = tempFile
                    }
                }
            }

        setContent {
            BrightcellShieldApp(
                onPickFile = {
                    filePicker.launch("*/*")
                },
                getSelectedFile = {
                    selectedFile
                }
            )
        }
    }
}

@Composable
fun BrightcellShieldApp(
    onPickFile: () -> Unit,
    getSelectedFile: () -> File?
) {

    val backgroundColor = Color(0xFF06100C)
    val cardColor = Color(0xFF0D2117)
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

                Spacer(modifier = Modifier.height(15.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                SecurityStatusCard(
                    scannedCount = scannedCount,
                    threatCount = threatCount,
                    isScanning =
                        isScanningApps || isScanningFile
                )

                Spacer(modifier = Modifier.height(15.dp))

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
                                        ScannerEngine(
                                            this@BrightcellShieldApp
                                        )

                                    emptyList<ThreatAnalysisResult>()
                                }

                            scanResults = results
                            scannedCount = results.size

                            threatCount =
                                results.count {
                                    it.suspicious
                                }

                            isScanningApps = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor,
                        contentColor = Color.Black
                    )
                ) {

                    Text("SCAN INSTALLED APPS")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onPickFile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF173524)
                    )
                ) {

                    Text("SELECT FILE / APK")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {

                        val file = getSelectedFile()

                        if (
                            file != null &&
                            !isScanningFile
                        ) {

                            isScanningFile = true
                            fileResult = null

                            scope.launch {

                                val result =
                                    withContext(Dispatchers.Default) {

                                        FileScanner(
                                            context =
                                                LocalContext.current
                                        ).scanFile(file)
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
                        getSelectedFile() != null &&
                                !isScanningFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A5C3C)
                    )
                ) {

                    Text(
                        if (isScanningFile)
                            "SCANNING FILE..."
                        else
                            "START FILE SCAN"
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                if (fileResult != null) {

                    FileResultCard(fileResult!!)
                }

                if (scanResults.isNotEmpty()) {

                    Text(
                        text = "INSTALLED APP RESULTS",
                        color = greenColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(scanResults) { result ->

                            ScanResultCard(result)
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

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Apps scanned: $scannedCount",
                color = Color.LightGray
            )

            Text(
                text = "Suspicious apps: $threatCount",
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

            Spacer(modifier = Modifier.height(8.dp))

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
                text = "Risk Score: ${result.riskScore}/100",
                color = riskColor
            )

            if (result.sha256 != null) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SHA-256:",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Text(
                    text = result.sha256.take(32) + "...",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            if (result.errorMessage != null) {

                Text(
                    text = result.errorMessage,
                    color = Color.Red
                )
            }
        }
    }
}
