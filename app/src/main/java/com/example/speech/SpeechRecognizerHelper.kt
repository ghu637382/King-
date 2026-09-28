package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SpeechState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}

class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onFallbackIntentNeeded: (Intent) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0.0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private var activeLanguage: Language? = null

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(language: Language) {
        activeLanguage = language
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                // Trigger fallback intent for speech dialog
                val intent = createRecognizerIntent(language)
                onFallbackIntentNeeded(intent)
                return@post
            }

            try {
                destroyRecognizer()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }

                val intent = createRecognizerIntent(language)
                speechRecognizer?.startListening(intent)
                _speechState.value = SpeechState.LISTENING
                _partialText.value = ""
                _rmsDb.value = 0f
            } catch (e: Exception) {
                _speechState.value = SpeechState.ERROR
                onError("Failed to start speech recognizer: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                if (_speechState.value == SpeechState.LISTENING) {
                    _speechState.value = SpeechState.PROCESSING
                }
            } catch (e: Exception) {
                destroyRecognizer()
                _speechState.value = SpeechState.IDLE
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            destroyRecognizer()
            _speechState.value = SpeechState.IDLE
            _partialText.value = ""
            _rmsDb.value = 0f
        }
    }

    fun destroy() {
        mainHandler.post {
            destroyRecognizer()
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
        }
    }

    private fun createRecognizerIntent(language: Language): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.sttLocaleTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.sttLocaleTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language.sttLocaleTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Maya AI (${language.displayName} - ${language.nativeName})")
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechState.value = SpeechState.LISTENING
            }

            override fun onBeginningOfSpeech() {
                _speechState.value = SpeechState.LISTENING
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize 0..10
                _rmsDb.value = rmsdB.coerceIn(0f, 10f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _speechState.value = SpeechState.PROCESSING
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please wait a moment."
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap to speak again."
                    else -> "Speech recognition error code: $error"
                }

                _speechState.value = SpeechState.IDLE
                _rmsDb.value = 0f

                // If error is client or no match on certain systems, can offer fallback
                if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_NO_MATCH) {
                    onError(errorMsg)
                } else {
                    onError(errorMsg)
                }
            }

            override fun onResults(results: Bundle?) {
                _speechState.value = SpeechState.IDLE
                _rmsDb.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrBlank()) {
                    _partialText.value = recognizedText
                    onResult(recognizedText)
                } else {
                    onError("Could not understand clearly. Please speak again.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim()
                if (!text.isNullOrBlank()) {
                    _partialText.value = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
