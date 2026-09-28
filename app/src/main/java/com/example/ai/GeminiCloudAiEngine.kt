package com.example.ai

import com.example.BuildConfig
import com.example.data.ChatMessageEntity
import com.example.model.AiEngineType
import com.example.model.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiCloudAiEngine : AiEngine {
    override val engineType: AiEngineType = AiEngineType.GEMINI_CLOUD
    override val name: String = "Gemini 3.5 Flash"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        language: Language,
        history: List<ChatMessageEntity>,
        apiKeyOverride: String?,
        customEndpoint: String?
    ): AiResult = withContext(Dispatchers.IO) {
        val effectiveApiKey = apiKeyOverride?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY

        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiResult.Error(
                "Gemini API key is not configured. Please configure it in Secrets or Settings."
            )
        }

        try {
            val endpointUrl = if (!customEndpoint.isNullOrBlank()) {
                customEndpoint
            } else {
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$effectiveApiKey"
            }

            val systemPrompt = "You are Maya AI, an intelligent, empathetic, and warm voice assistant similar to Maya AI. " +
                    "The user's current selected language is ${language.displayName} (${language.nativeName}). " +
                    "IMPORTANT: Always respond primarily in ${language.displayName} (${language.nativeName}) unless the user explicitly requests another language. " +
                    "If Urdu, use natural, grammatically correct Nastaliq/Urdu script (اردو). " +
                    "Keep your responses concise, conversational, and direct (1 to 3 sentences) because your reply will be read aloud through Text-To-Speech to the user."

            val jsonBody = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })

                // Contents history + current prompt
                val contentsArray = JSONArray()

                // Add up to 6 recent turns from history for conversation memory
                val recentHistory = history.takeLast(6)
                for (msg in recentHistory) {
                    val role = if (msg.role == "user") "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", msg.content)
                            })
                        })
                    })
                }

                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })

                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url(endpointUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext AiResult.Error("Gemini Cloud error: $errorMsg")
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val contentObj = candidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val responseText = parts?.optJSONObject(0)?.optString("text")

            if (!responseText.isNullOrBlank()) {
                AiResult.Success(
                    responseText = responseText.trim(),
                    engineSource = name,
                    isFallback = false
                )
            } else {
                AiResult.Error("Received empty response from Gemini API.")
            }
        } catch (e: Exception) {
            AiResult.Error("Network or API error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }
}
