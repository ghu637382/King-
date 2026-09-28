package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechHelper(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setupUtteranceListener()
                onInitComplete(true)
            } else {
                isInitialized = false
                onInitComplete(false)
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _currentUtteranceId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }
        })
    }

    fun speak(
        text: String,
        language: Language,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        utteranceId: String = System.currentTimeMillis().toString()
    ) {
        if (!isInitialized || tts == null) return

        try {
            stop()

            val locale = Locale.forLanguageTag(language.sttLocaleTag)

            val langResult = tts?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try language tag by language code only
                val fallbackLocale = Locale.forLanguageTag(language.ttsLocaleLanguage)
                val fallbackResult = tts?.setLanguage(fallbackLocale)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fall back to default locale
                    tts?.language = Locale.getDefault()
                }
            }

            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (_: Exception) {
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _currentUtteranceId.value = null
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            _isSpeaking.value = false
        } catch (_: Exception) {
        }
    }
}
