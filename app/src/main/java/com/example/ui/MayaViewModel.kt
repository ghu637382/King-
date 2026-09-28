package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiResult
import com.example.ai.ModularAiManager
import com.example.data.ChatMessageEntity
import com.example.data.MayaDatabase
import com.example.data.MayaSettings
import com.example.data.SettingsManager
import com.example.model.AiEngineType
import com.example.model.Language
import com.example.model.SupportedLanguages
import com.example.speech.SpeechRecognizerHelper
import com.example.speech.SpeechState
import com.example.speech.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MayaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MayaDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val settingsManager = SettingsManager(application)
    private val aiManager = ModularAiManager()

    val settings: StateFlow<MayaSettings> = settingsManager.settings

    val activeLanguage: StateFlow<Language> = settings.map { s ->
        SupportedLanguages.getByCode(s.languageCode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SupportedLanguages.URDU
    )

    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _fallbackIntentEvent = MutableSharedFlow<Intent>()
    val fallbackIntentEvent: SharedFlow<Intent> = _fallbackIntentEvent.asSharedFlow()

    // Speech Recognizer (STT)
    val speechRecognizerHelper = SpeechRecognizerHelper(
        context = application,
        onResult = { recognizedText ->
            processUserSpeech(recognizedText)
        },
        onError = { err ->
            _errorMessage.value = err
        },
        onFallbackIntentNeeded = { intent ->
            viewModelScope.launch {
                _fallbackIntentEvent.emit(intent)
            }
        }
    )

    val speechState: StateFlow<SpeechState> = speechRecognizerHelper.speechState
    val partialSpeechText: StateFlow<String> = speechRecognizerHelper.partialText
    val rmsDb: StateFlow<Float> = speechRecognizerHelper.rmsDb

    // Text To Speech (TTS)
    val ttsHelper = TextToSpeechHelper(application)
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val currentSpeakingMessageId: StateFlow<String?> = ttsHelper.currentUtteranceId

    fun toggleListening() {
        if (speechState.value == SpeechState.LISTENING) {
            speechRecognizerHelper.stopListening()
        } else {
            ttsHelper.stop()
            val currentLang = activeLanguage.value
            speechRecognizerHelper.startListening(currentLang)
        }
    }

    fun stopListening() {
        speechRecognizerHelper.stopListening()
    }

    fun handleFallbackSpeechResult(text: String) {
        if (text.isNotBlank()) {
            processUserSpeech(text)
        }
    }

    private fun processUserSpeech(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        sendMessage(clean)
    }

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return

        val currentLang = activeLanguage.value
        val currentSettings = settings.value

        viewModelScope.launch {
            // Stop TTS if speaking
            ttsHelper.stop()

            // 1. Insert user message into Room
            val userMsg = ChatMessageEntity(
                role = "user",
                content = clean,
                languageCode = currentLang.code
            )
            chatDao.insertMessage(userMsg)

            // 2. Query Modular AI
            _isAiThinking.value = true
            val recentMessages = chatDao.getRecentMessages(6).reversed()

            val aiResult = aiManager.processQuery(
                prompt = clean,
                language = currentLang,
                engineType = currentSettings.engineType,
                history = recentMessages,
                apiKeyOverride = currentSettings.customApiKey.takeIf { it.isNotBlank() },
                customEndpoint = currentSettings.customApiUrl.takeIf { it.isNotBlank() }
            )

            _isAiThinking.value = false

            when (aiResult) {
                is AiResult.Success -> {
                    val assistantMsg = ChatMessageEntity(
                        role = "assistant",
                        content = aiResult.responseText,
                        languageCode = currentLang.code,
                        engineUsed = aiResult.engineSource
                    )
                    val insertedId = chatDao.insertMessage(assistantMsg)

                    // Auto speak response if enabled
                    if (currentSettings.autoSpeak) {
                        ttsHelper.speak(
                            text = aiResult.responseText,
                            language = currentLang,
                            rate = currentSettings.speechRate,
                            pitch = currentSettings.speechPitch,
                            utteranceId = insertedId.toString()
                        )
                    }
                }
                is AiResult.Error -> {
                    _errorMessage.value = aiResult.errorMessage
                    val assistantMsg = ChatMessageEntity(
                        role = "assistant",
                        content = aiResult.fallbackResponse ?: "Sorry, I could not complete your request. Please try again.",
                        languageCode = currentLang.code,
                        engineUsed = "Error Notice"
                    )
                    chatDao.insertMessage(assistantMsg)
                }
            }
        }
    }

    fun speakMessage(message: ChatMessageEntity) {
        if (isSpeaking.value && currentSpeakingMessageId.value == message.id.toString()) {
            ttsHelper.stop()
            return
        }
        val currentSettings = settings.value
        val msgLang = SupportedLanguages.getByCode(message.languageCode)
        ttsHelper.speak(
            text = message.content,
            language = msgLang,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch,
            utteranceId = message.id.toString()
        )
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    fun testVoice() {
        val currentLang = activeLanguage.value
        val currentSettings = settings.value
        ttsHelper.speak(
            text = currentLang.greeting,
            language = currentLang,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch,
            utteranceId = "test_voice"
        )
    }

    fun setLanguage(language: Language) {
        settingsManager.setLanguage(language.code)
    }

    fun setEngine(engineType: AiEngineType) {
        settingsManager.setEngineType(engineType)
    }

    fun setSpeechRate(rate: Float) {
        settingsManager.setSpeechRate(rate)
    }

    fun setSpeechPitch(pitch: Float) {
        settingsManager.setSpeechPitch(pitch)
    }

    fun setAutoSpeak(auto: Boolean) {
        settingsManager.setAutoSpeak(auto)
    }

    fun setCustomApiKey(key: String) {
        settingsManager.setCustomApiKey(key)
    }

    fun setCustomApiUrl(url: String) {
        settingsManager.setCustomApiUrl(url)
    }

    fun toggleFavorite(message: ChatMessageEntity) {
        viewModelScope.launch {
            chatDao.updateFavorite(message.id, !message.isFavorite)
        }
    }

    fun clearAllMessages() {
        viewModelScope.launch {
            ttsHelper.stop()
            chatDao.clearAll()
        }
    }

    fun exportConversation(): String {
        val list = messages.value
        if (list.isEmpty()) return "Maya AI - No conversations recorded yet."

        val sb = StringBuilder("=== Maya AI Conversation History ===\n\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        for (m in list) {
            val dateStr = sdf.format(Date(m.timestamp))
            val sender = if (m.role == "user") "You" else "Maya AI (${m.engineUsed ?: "Assistant"})"
            sb.append("[$dateStr] $sender:\n${m.content}\n\n")
        }
        return sb.toString()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.destroy()
        ttsHelper.shutdown()
    }
}
