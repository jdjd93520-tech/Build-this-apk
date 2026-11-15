package com.xquty.aimassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import android.content.Context

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
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("xquty_local", Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf("login") }
    var keyInput by remember { mutableStateOf("") }
    var adminPin by remember { mutableStateOf(prefs.getString("admin_pin", "") ?: "") }
    var pinInput by remember { mutableStateOf("") }
    var loggedIn by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var keys by remember { mutableStateOf(prefs.getStringSet("keys", emptySet())?.toMutableSet() ?: mutableSetOf()) }
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

                when (page) {
                    "login" -> {
                        CardBlock {
                            Text("KEY LOGIN", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            OutlinedTextField(keyInput, { keyInput = it }, label = { Text("Enter access key") }, modifier = Modifier.fillMaxWidth())
                            ButtonGold("Activate / Login") {
                                if (keyInput in keys) {
                                    loggedIn = true; page = "home"; status = "Key accepted"
                                } else status = "Invalid key. Ask the administrator for an active key."
                            }
                            if (status.isNotBlank()) Text(status, color = Gold)
                        }
                        Spacer(Modifier.height(12.dp))
                        CardBlock {
                            Text("First-time admin setup", color = Gold, fontWeight = FontWeight.Bold)
                            Text("Set a private admin PIN on this device. This local prototype does not use a secure online server.")
                            OutlinedTextField(pinInput, { pinInput = it }, label = { Text("Create / enter admin PIN") }, modifier = Modifier.fillMaxWidth())
                            ButtonGold("Open Admin Panel") {
                                if (adminPin.isBlank() && pinInput.length >= 6) {
                                    adminPin = pinInput; prefs.edit().putString("admin_pin", adminPin).apply()
                                    page = "admin"; status = "Admin PIN created"
                                } else if (pinInput == adminPin && adminPin.isNotBlank()) {
                                    page = "admin"; status = ""
                                } else status = "PIN must be 6+ characters or does not match."
                            }
                            if (status.isNotBlank()) Text(status, color = Gold)
                        }
                    }
                    "home" -> {
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
                        ButtonGold("Logout") { loggedIn = false; page = "login" }
                    }
                    "admin" -> {
                        CardBlock {
                            Text("PRIVATE ADMIN PANEL", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            var newKey by remember { mutableStateOf("") }
                            OutlinedTextField(newKey, { newKey = it }, label = { Text("New key (e.g. XQUTY-7D-001)") }, modifier = Modifier.fillMaxWidth())
                            ButtonGold("Generate / Add Key") {
                                if (newKey.trim().length >= 6) {
                                    keys.add(newKey.trim()); prefs.edit().putStringSet("keys", keys).apply()
                                    newKey = ""; status = "Key added on this device."
                                } else status = "Enter at least 6 characters."
                            }
                            Text("Saved keys: ${keys.size}", color = Gold)
                            keys.toList().forEach { k ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(k, Modifier.weight(1f), color = Color.White)
                                    TextButton(onClick = {
                                        keys.remove(k); prefs.edit().putStringSet("keys", keys).apply()
                                    }) { Text("DELETE", color = Gold) }
                                }
                            }
                            Text("Note: expiry dates, remote activation and multi-device validation need a secure backend.", color = Color.LightGray, fontSize = 12.sp)
                        }
                        ButtonGold("Back to Login") { page = "login" }
                    }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ButtonGold(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
        shape = RoundedCornerShape(12.dp)) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}
