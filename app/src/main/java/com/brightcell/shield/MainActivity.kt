package com.brightcell.shield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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

                Spacer(modifier = Modifier.height(45.dp))

                Text(
                    text = "🛡",
                    fontSize = 90.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "System Protected",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Brightcell Shield security engine is ready",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(35.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "SECURITY SCORE",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "100 / 100",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = greenColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = greenColor,
                            trackColor = Color(0xFF1A3B28)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = {
                        // Scanner functionality next version mein connect hogi
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor,
                        contentColor = Color(0xFF06100C)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text = "START SECURITY SCAN",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "V11.0 • Brightcell Security",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
