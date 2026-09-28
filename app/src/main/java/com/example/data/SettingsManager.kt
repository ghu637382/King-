package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AiEngineType
import com.example.model.SupportedLanguages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MayaSettings(
    val languageCode: String = SupportedLanguages.URDU.code,
    val engineType: AiEngineType = AiEngineType.AUTO,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val customApiKey: String = "",
    val customApiUrl: String = ""
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("maya_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MayaSettings> = _settings.asStateFlow()

    private fun loadSettings(): MayaSettings {
        val lang = prefs.getString(KEY_LANGUAGE, SupportedLanguages.URDU.code) ?: SupportedLanguages.URDU.code
        val engineStr = prefs.getString(KEY_ENGINE, AiEngineType.AUTO.name) ?: AiEngineType.AUTO.name
        val engine = try {
            AiEngineType.valueOf(engineStr)
        } catch (_: Exception) {
            AiEngineType.AUTO
        }
        val rate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        val pitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        val autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, true)
        val apiKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        val apiUrl = prefs.getString(KEY_CUSTOM_API_URL, "") ?: ""

        return MayaSettings(
            languageCode = lang,
            engineType = engine,
            speechRate = rate,
            speechPitch = pitch,
            autoSpeak = autoSpeak,
            customApiKey = apiKey,
            customApiUrl = apiUrl
        )
    }

    fun setLanguage(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
        _settings.value = _settings.value.copy(languageCode = code)
    }

    fun setEngineType(engineType: AiEngineType) {
        prefs.edit().putString(KEY_ENGINE, engineType.name).apply()
        _settings.value = _settings.value.copy(engineType = engineType)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        prefs.edit().putFloat(KEY_SPEECH_RATE, clamped).apply()
        _settings.value = _settings.value.copy(speechRate = clamped)
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        prefs.edit().putFloat(KEY_SPEECH_PITCH, clamped).apply()
        _settings.value = _settings.value.copy(speechPitch = clamped)
    }

    fun setAutoSpeak(auto: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, auto).apply()
        _settings.value = _settings.value.copy(autoSpeak = auto)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key).apply()
        _settings.value = _settings.value.copy(customApiKey = key)
    }

    fun setCustomApiUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_API_URL, url).apply()
        _settings.value = _settings.value.copy(customApiUrl = url)
    }

    companion object {
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_ENGINE = "key_engine"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_SPEECH_PITCH = "key_speech_pitch"
        private const val KEY_AUTO_SPEAK = "key_auto_speak"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CUSTOM_API_URL = "key_custom_api_url"
    }
}
