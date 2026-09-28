package com.example.ai

import com.example.data.ChatMessageEntity
import com.example.model.AiEngineType
import com.example.model.Language

class ModularAiManager {
    private val geminiEngine = GeminiCloudAiEngine()
    private val offlineEngine = OfflineRuleAiEngine()

    suspend fun processQuery(
        prompt: String,
        language: Language,
        engineType: AiEngineType,
        history: List<ChatMessageEntity> = emptyList(),
        apiKeyOverride: String? = null,
        customEndpoint: String? = null
    ): AiResult {
        return when (engineType) {
            AiEngineType.OFFLINE_RULE -> {
                offlineEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
            }

            AiEngineType.GEMINI_CLOUD -> {
                val result = geminiEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                if (result is AiResult.Error) {
                    // Fall back to offline with notice if configured
                    val offline = offlineEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                    if (offline is AiResult.Success) {
                        AiResult.Success(
                            responseText = offline.responseText,
                            engineSource = "Offline Fallback (${result.errorMessage})",
                            isFallback = true
                        )
                    } else {
                        result
                    }
                } else {
                    result
                }
            }

            AiEngineType.AUTO -> {
                // Try Gemini first
                val geminiResult = geminiEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                if (geminiResult is AiResult.Success) {
                    geminiResult
                } else {
                    // Seamless smart fallback to Offline Engine
                    val offline = offlineEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                    if (offline is AiResult.Success) {
                        AiResult.Success(
                            responseText = offline.responseText,
                            engineSource = "Offline Smart Assistant",
                            isFallback = true
                        )
                    } else {
                        geminiResult
                    }
                }
            }

            AiEngineType.CUSTOM_API -> {
                val custom = geminiEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                if (custom is AiResult.Error) {
                    offlineEngine.generateResponse(prompt, language, history, apiKeyOverride, customEndpoint)
                } else {
                    custom
                }
            }
        }
    }
}
