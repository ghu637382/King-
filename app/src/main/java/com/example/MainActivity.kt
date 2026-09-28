package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MayaHomeScreen
import com.example.ui.MayaViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: MayaViewModel = viewModel()

                var hasAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                // Audio Record Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                    if (isGranted) {
                        viewModel.toggleListening()
                    } else {
                        Toast.makeText(
                            this,
                            "Microphone permission is required for voice assistant speech recognition.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // Fallback Speech Recognizer Intent Launcher
                val speechIntentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            viewModel.handleFallbackSpeechResult(text)
                        }
                    }
                }

                // Collect fallback speech intent events from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.fallbackIntentEvent.collect { intent ->
                        try {
                            speechIntentLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Speech recognition is not supported on this device.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    MayaHomeScreen(
                        viewModel = viewModel,
                        hasRecordPermission = hasAudioPermission,
                        onRequestRecordPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
            }
        }
    }
}
