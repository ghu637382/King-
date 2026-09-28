package com.example.model

enum class AiEngineType(val title: String, val description: String) {
    AUTO(
        title = "Auto (Smart Fallback)",
        description = "Uses Gemini Cloud AI when online; seamlessly falls back to offline engine if offline or key is missing."
    ),
    GEMINI_CLOUD(
        title = "Gemini Cloud AI",
        description = "High-intelligence neural responses powered by Google Gemini 3.5 Flash."
    ),
    OFFLINE_RULE(
        title = "Offline Smart Assistant",
        description = "100% offline, private, zero-latency natural language engine. Works anywhere."
    ),
    CUSTOM_API(
        title = "Custom AI Endpoint",
        description = "Connect your own custom OpenAI or Gemini-compatible REST server."
    )
}
