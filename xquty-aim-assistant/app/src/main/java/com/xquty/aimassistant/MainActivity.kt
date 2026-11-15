package com.xquty.aimassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFFFC400)
private val Panel = Color(0xFF252525)
private val Bg = Color(0xFF101010)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { XqutyApp() }
    }
}

@Composable
fun XqutyApp() {
    var angle by remember { mutableFloatStateOf(45f) }
    var rebound by remember { mutableFloatStateOf(30f) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Bg, surface = Panel,
        onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))
                Text("👑", fontSize = 42.sp)
                Text("XQUTY", color = Gold, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("AIM ASSISTANT", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                CardBlock {
                    Text("AIM TOOLS", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Manual guidance only — this app does not control Carrom Pool or automate shots.")
                    Text("Angle calculator", color = Gold, fontWeight = FontWeight.Bold)
                    Text("${angle.toInt()}°", color = Gold, fontSize = 28.sp)
                    Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..90f)
                    Text("Rebound reference angle", color = Gold, fontWeight = FontWeight.Bold)
                    Text("${rebound.toInt()}°", color = Gold, fontSize = 28.sp)
                    Slider(value = rebound, onValueChange = { rebound = it }, valueRange = 0f..90f)
                }

                Spacer(Modifier.height(24.dp))
                Text("XQUTY • LOCAL STARTER BUILD", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Gold)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
