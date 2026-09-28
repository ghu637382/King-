package com.example.model

data class Language(
    val code: String,          // e.g., "ur"
    val displayName: String,   // e.g., "Urdu"
    val nativeName: String,    // e.g., "اردو"
    val flagEmoji: String,     // e.g., "🇵🇰"
    val sttLocaleTag: String,  // e.g., "ur-PK"
    val ttsLocaleLanguage: String, // e.g., "ur"
    val ttsLocaleCountry: String,  // e.g., "PK"
    val greeting: String,
    val suggestions: List<String>
) {
    val fullLabel: String
        get() = "$flagEmoji $displayName ($nativeName)"
}

object SupportedLanguages {
    val URDU = Language(
        code = "ur",
        displayName = "Urdu",
        nativeName = "اردو",
        flagEmoji = "🇵🇰",
        sttLocaleTag = "ur-PK",
        ttsLocaleLanguage = "ur",
        ttsLocaleCountry = "PK",
        greeting = "السلام علیکم! میں مایا ہوں، آپ کی ذاتی صوتی معاون۔ میں آپ کی کیا مدد کر سکتی ہوں؟",
        suggestions = listOf(
            "کیا وقت ہوا ہے؟",
            "ایک اچھا سا لطیفہ سنائیں",
            "آپ کیا کر سکتی ہیں؟",
            "آج کی تاریخ کیا ہے؟",
            "مجھے کوئی اچھی نصیحت کریں",
            "25 ضرب 8 کتنا ہوتا ہے؟"
        )
    )

    val ENGLISH = Language(
        code = "en",
        displayName = "English",
        nativeName = "English",
        flagEmoji = "🇺🇸",
        sttLocaleTag = "en-US",
        ttsLocaleLanguage = "en",
        ttsLocaleCountry = "US",
        greeting = "Hello! I am Maya, your AI voice assistant. How can I help you today?",
        suggestions = listOf(
            "What time is it?",
            "Tell me a funny joke",
            "What can you do?",
            "Give me today's motivation",
            "Calculate 125 times 4",
            "Translate 'Hello' to Urdu"
        )
    )

    val HINDI = Language(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        flagEmoji = "🇮🇳",
        sttLocaleTag = "hi-IN",
        ttsLocaleLanguage = "hi",
        ttsLocaleCountry = "IN",
        greeting = "नमस्ते! मैं माया हूँ, आपकी वॉयس AI सहायक। मैं आज आपकी क्या मदद कर सकती हूँ?",
        suggestions = listOf(
            "क्या समय हुआ है?",
            "एक मज़ेदार चुटकुला सुनाओ",
            "आप क्या कर सकती हैं?",
            "आज की तारीख क्या है?",
            "प्रेरणादायक विचार बताओ"
        )
    )

    val PUNJABI = Language(
        code = "pa",
        displayName = "Punjabi",
        nativeName = "پنجابی / ਪੰਜਾਬੀ",
        flagEmoji = "🇵🇰",
        sttLocaleTag = "pa-PK",
        ttsLocaleLanguage = "pa",
        ttsLocaleCountry = "PK",
        greeting = "ست شری اکال! میں مایا ہاں، تہاڈی وائس اسسٹنٹ۔ دسو کی خدمت کراں؟",
        suggestions = listOf(
            "کی ویلا ہویا اے؟",
            "اک سوہنا لطیفہ سناؤ",
            "تسی کی کر سکدے او؟",
            "اج دی تریخ کی اے؟"
        )
    )

    val ARABIC = Language(
        code = "ar",
        displayName = "Arabic",
        nativeName = "العربية",
        flagEmoji = "🇸🇦",
        sttLocaleTag = "ar-SA",
        ttsLocaleLanguage = "ar",
        ttsLocaleCountry = "SA",
        greeting = "مرحباً! أنا مايا، مساعدتك الصوتية الذكية. كيف يمكنني مساعدتك اليوم؟",
        suggestions = listOf(
            "كم الساعة الآن؟",
            "أخبرني نكتة مضحكة",
            "ماذا يمكنك أن تفعلي؟",
            "ما هو تاريخ اليوم؟",
            "أعطني حكمة اليوم"
        )
    )

    val SPANISH = Language(
        code = "es",
        displayName = "Spanish",
        nativeName = "Español",
        flagEmoji = "🇪🇸",
        sttLocaleTag = "es-ES",
        ttsLocaleLanguage = "es",
        ttsLocaleCountry = "ES",
        greeting = "¡Hola! Soy Maya, tu asistente de voz con IA. ¿En qué puedo ayudarte hoy?",
        suggestions = listOf(
            "¿Qué hora es?",
            "Cuéntame un chiste",
            "¿Qué puedes hacer?",
            "Dime una frase motivadora",
            "¿Cuánto es 45 por 6?"
        )
    )

    val FRENCH = Language(
        code = "fr",
        displayName = "French",
        nativeName = "Français",
        flagEmoji = "🇫🇷",
        sttLocaleTag = "fr-FR",
        ttsLocaleLanguage = "fr",
        ttsLocaleCountry = "FR",
        greeting = "Bonjour! Je suis Maya, votre assistante vocale IA. Comment puis-je vous aider?",
        suggestions = listOf(
            "Quelle heure est-il?",
            "Raconte-moi une blague",
            "Que peux-tu faire?",
            "Donne-moi une citation inspirante"
        )
    )

    val GERMAN = Language(
        code = "de",
        displayName = "German",
        nativeName = "Deutsch",
        flagEmoji = "🇩🇪",
        sttLocaleTag = "de-DE",
        ttsLocaleLanguage = "de",
        ttsLocaleCountry = "DE",
        greeting = "Hallo! Ich bin Maya, deine KI-Sprachassistentin. Wie kann ich dir helfen?",
        suggestions = listOf(
            "Wie spät ist es?",
            "Erzähl mir einen Witz",
            "Was kannst du tun?",
            "Gib mir ein Zitat für heute"
        )
    )

    val BENGALI = Language(
        code = "bn",
        displayName = "Bengali",
        nativeName = "বাংলা",
        flagEmoji = "🇧🇩",
        sttLocaleTag = "bn-BD",
        ttsLocaleLanguage = "bn",
        ttsLocaleCountry = "BD",
        greeting = "নমস্কার! আমি মায়া, আপনার ভয়েস এআই সহকারী। আমি আপনাকে কীভাবে সাহায্য করতে পারি?",
        suggestions = listOf(
            "এখন কয়টা বাজে?",
            "আমাকে একটি কৌতুক শোনাও",
            "তুমি কি করতে পারো?",
            "আজকের তারিখ কি?"
        )
    )

    val TURKISH = Language(
        code = "tr",
        displayName = "Turkish",
        nativeName = "Türkçe",
        flagEmoji = "🇹🇷",
        sttLocaleTag = "tr-TR",
        ttsLocaleLanguage = "tr",
        ttsLocaleCountry = "TR",
        greeting = "Merhaba! Ben Maya, sesli yapay zeka asistanınız. Size nasıl yardımcı olabilirim?",
        suggestions = listOf(
            "Saat kaç?",
            "Bana bir fıkra anlat",
            "Neler yapabilirsin?",
            "Bugünün tarihi nedir?"
        )
    )

    val list: List<Language> = listOf(
        URDU,
        ENGLISH,
        HINDI,
        PUNJABI,
        ARABIC,
        SPANISH,
        FRENCH,
        GERMAN,
        BENGALI,
        TURKISH
    )

    fun getByCode(code: String): Language {
        return list.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: URDU
    }
}
