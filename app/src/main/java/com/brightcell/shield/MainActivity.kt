package com.brightcell.shield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrightcellShieldApp()
        }
    }
}

@Composable
fun BrightcellShieldApp() {

    val backgroundColor = Color(0xFF06100C)
    val cardColor = Color(0xFF0D2117)
    val greenColor = Color(0xFF40EC82)

    var isScanning by remember { mutableStateOf(false) }

    var scanResults by remember {
        mutableStateOf<List<ThreatAnalysisResult>>(emptyList())
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

                Spacer(modifier = Modifier.height(20.dp))

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

                Spacer(modifier = Modifier.height(25.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "SYSTEM SECURITY STATUS",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isScanning)
                                "Scanning..."
                            else
                                "System Protected",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScanning)
                                Color.Yellow
                            else
                                greenColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Apps scanned: $scannedCount",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )

                        Text(
                            text = "Suspicious apps: $threatCount",
                            fontSize = 13.sp,
                            color = if (threatCount > 0)
                                Color.Red
                            else
                                Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {

                        if (isScanning) return@Button

                        isScanning = true
                        scanResults = emptyList()
                        scannedCount = 0
                        threatCount = 0

                        scope.launch {

                            val results =
                                withContext(Dispatchers.Default) {

                                    val scanner =
                                        ScannerEngine(
                                            context =
                                                this@MainActivity
                                        )

                                    val analyzer =
                                        ThreatAnalyzer()

                                    val apps =
                                        scanner.scanInstalledApps()

                                    apps.map {

                                        analyzer.analyzeApp(it)

                                    }
                                }

                            scanResults = results
                            scannedCount = results.size

                            threatCount =
                                results.count {
                                    it.suspicious
                                }

                            isScanning = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor,
                        contentColor = Color(0xFF06100C)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            if (isScanning)
                                "SCANNING INSTALLED APPS..."
                            else
                                "START SECURITY SCAN",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isScanning) {

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = greenColor,
                        trackColor = Color(0xFF1A3B28)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Analyzing installed applications...",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (scanResults.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SCAN RESULTS",
                        color = greenColor,
                        fontSize = 13.sp,
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
fun ScanResultCard(
    result: ThreatAnalysisResult
) {

    val cardColor = Color(0xFF0D2117)

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
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = result.appName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.packageName,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = result.verdict,
                    fontSize = 12.sp,
                    color = riskColor
                )

                Text(
                    text = "Risk: ${result.riskScore}/100",
                    fontSize = 12.sp,
                    color = riskColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (result.reasons.isNotEmpty()) {

                Spacer(modifier = Modifier.height(8.dp))

                result.reasons.take(3).forEach {

                    Text(
                        text = "• $it",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}
