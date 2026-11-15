package com.xquty.aimassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Accent = Color(0xFF62D6A5)
private val Background = Color(0xFF101820)

class MainActivity : ComponentActivity() {
    private val screenShareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                    action = ScreenCaptureService.ACTION_START
                    putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    statusUpdate?.invoke("Screen-sharing permission approved. Capture service started.")
                } catch (e: Exception) {
                    statusUpdate?.invoke("Could not start capture service: ${e.message ?: "unknown error"}")
                }
            } else {
                statusUpdate?.invoke("Screen sharing permission was not granted.")
            }
        }

    private var statusUpdate: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var started by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Ready. Press Start to reveal screen sharing.") }
            statusUpdate = { status = it }

            MaterialTheme(colorScheme = darkColorScheme(
                primary = Accent,
                background = Background,
                surface = Color(0xFF1D2935),
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                Surface(Modifier.fillMaxSize(), color = Background) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("SCREEN SHARE", style = MaterialTheme.typography.headlineMedium,
                            color = Accent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Local screen-capture preview", color = Color.LightGray)
                        Spacer(Modifier.height(28.dp))
                        Button(onClick = {
                            started = true
                            status = "Ready. Tap Share Entire Screen and approve Android's prompt."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Start")
                        }
                        if (started) {
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { requestScreenShare() },
                                modifier = Modifier.fillMaxWidth()) {
                                Text("Share Entire Screen")
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = {
                                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java))
                                started = false
                                status = "Capture stopped."
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Stop")
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2935))
                        ) {
                            Text(status, Modifier.padding(18.dp), color = Color.White)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "This build only captures the screen after your approval. It does not analyze targets, move the pointer, inject touches, or control other apps.",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    private fun requestScreenShare() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenShareLauncher.launch(manager.createScreenCaptureIntent())
    }

    override fun onDestroy() {
        statusUpdate = null
        super.onDestroy()
    }
}