package com.xquty.aimassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.DisplayMetrics
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
    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var onStatus: ((String) -> Unit)? = null

    private val screenShareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                beginCapture(result.resultCode, result.data!!)
            } else {
                onStatus?.invoke("Screen sharing permission was not granted.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var started by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Ready. Press Start to reveal screen sharing.") }
            onStatus = { status = it }

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
                                stopCapture()
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

    private fun beginCapture(resultCode: Int, data: Intent) {
        stopCapture()
        try {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = manager.getMediaProjection(resultCode, data)
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val width = metrics.widthPixels.coerceAtLeast(1)
            val height = metrics.heightPixels.coerceAtLeast(1)
            val density = metrics.densityDpi
            imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2)
            imageReader?.setOnImageAvailableListener({ reader ->
                reader.acquireLatestImage()?.close()
            }, null)
            virtualDisplay = projection?.createVirtualDisplay(
                "ScreenSharePreview",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
            projection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    runOnUiThread {
                        releaseCapture()
                        onStatus?.invoke("Android stopped screen sharing.")
                    }
                }
            }, null)
            onStatus?.invoke("Screen sharing is active. Android's screen-sharing indicator is visible.")
        } catch (e: Exception) {
            stopCapture()
            onStatus?.invoke("Could not start screen sharing: ${e.message ?: "unknown error"}")
        }
    }

    private fun stopCapture() {
        try { projection?.stop() } catch (_: Exception) { }
        releaseCapture()
        onStatus?.invoke("Capture stopped.")
    }

    private fun releaseCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.setOnImageAvailableListener(null, null)
        imageReader?.close()
        imageReader = null
        projection = null
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }
}
