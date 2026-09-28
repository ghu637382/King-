package com.example.ai

import com.example.data.ChatMessageEntity
import com.example.model.AiEngineType
import com.example.model.Language

sealed class AiResult {
    data class Success(
        val responseText: String,
        val engineSource: String,
        val isFallback: Boolean = false
    ) : AiResult()

    data class Error(
        val errorMessage: String,
        val fallbackResponse: String? = null
    ) : AiResult()
}

interface AiEngine {
    val engineType: AiEngineType
    val name: String

    suspend fun generateResponse(
        prompt: String,
        language: Language,
        history: List<ChatMessageEntity> = emptyList(),
        apiKeyOverride: String? = null,
        customEndpoint: String? = null
    ): AiResult
}
