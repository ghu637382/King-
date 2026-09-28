package com.example.ai

import com.example.data.ChatMessageEntity
import com.example.model.AiEngineType
import com.example.model.Language
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class OfflineRuleAiEngine : AiEngine {
    override val engineType: AiEngineType = AiEngineType.OFFLINE_RULE
    override val name: String = "Maya Offline Intelligence"

    override suspend fun generateResponse(
        prompt: String,
        language: Language,
        history: List<ChatMessageEntity>,
        apiKeyOverride: String?,
        customEndpoint: String?
    ): AiResult {
        val cleanPrompt = prompt.trim()
        val lowerPrompt = cleanPrompt.lowercase()

        // 1. Math Evaluation check
        val mathResult = tryEvaluateMath(cleanPrompt, language)
        if (mathResult != null) {
            return AiResult.Success(mathResult, name)
        }

        // 2. Time check
        if (isTimeQuery(lowerPrompt)) {
            val timeText = formatCurrentTime(language)
            return AiResult.Success(timeText, name)
        }

        // 3. Date check
        if (isDateQuery(lowerPrompt)) {
            val dateText = formatCurrentDate(language)
            return AiResult.Success(dateText, name)
        }

        // 4. Identity / Who are you check
        if (isIdentityQuery(lowerPrompt)) {
            val identityText = getIdentityResponse(language)
            return AiResult.Success(identityText, name)
        }

        // 5. Capabilities / What can you do check
        if (isCapabilitiesQuery(lowerPrompt)) {
            val capabilitiesText = getCapabilitiesResponse(language)
            return AiResult.Success(capabilitiesText, name)
        }

        // 6. Greetings / How are you
        if (isGreetingQuery(lowerPrompt)) {
            val greetingText = getGreetingResponse(language)
            return AiResult.Success(greetingText, name)
        }

        // 7. Jokes / Humor
        if (isJokeQuery(lowerPrompt)) {
            val jokeText = getJokeResponse(language)
            return AiResult.Success(jokeText, name)
        }

        // 8. Motivation / Wisdom
        if (isMotivationQuery(lowerPrompt)) {
            val motivationText = getMotivationResponse(language)
            return AiResult.Success(motivationText, name)
        }

        // 9. Weather inquiry
        if (isWeatherQuery(lowerPrompt)) {
            val weatherText = getWeatherResponse(language)
            return AiResult.Success(weatherText, name)
        }

        // 10. General offline conversational fallback matching language
        val fallback = getConversationalFallback(cleanPrompt, language)
        return AiResult.Success(fallback, name)
    }

    private fun isTimeQuery(prompt: String): Boolean {
        return prompt.contains("وقت") || prompt.contains("ٹائم") || prompt.contains("time") ||
                prompt.contains("समय") || prompt.contains("ساعة") || prompt.contains("hora") ||
                prompt.contains("heure") || prompt.contains("uhr") || prompt.contains("ঘড়ি") ||
                prompt.contains("saat") || prompt.contains("ویلا")
    }

    private fun isDateQuery(prompt: String): Boolean {
        return prompt.contains("تاریخ") || prompt.contains("date") || prompt.contains("today") ||
                prompt.contains("तारीख") || prompt.contains("تاريخ") || prompt.contains("fecha") ||
                prompt.contains("datum") || prompt.contains("আজকের দিন") || prompt.contains("تریخ")
    }

    private fun isIdentityQuery(prompt: String): Boolean {
        return prompt.contains("نام کیا") || prompt.contains("کون ہو") || prompt.contains("who are you") ||
                prompt.contains("what is your name") || prompt.contains("naam") || prompt.contains("तुम्हारा नाम") ||
                prompt.contains("من أنت") || prompt.contains("quién eres") || prompt.contains("qui es-tu") ||
                prompt.contains("wer bist du") || prompt.contains("kimsinsin") || prompt.contains("تسی کون")
    }

    private fun isCapabilitiesQuery(prompt: String): Boolean {
        return prompt.contains("کیا کر سکتی") || prompt.contains("what can you do") || prompt.contains("help me") ||
                prompt.contains("capabilities") || prompt.contains("کیا کام") || prompt.contains("क्या कर सकती") ||
                prompt.contains("ماذا تستطيع") || prompt.contains("qué puedes hacer") || prompt.contains("que sais-tu faire")
    }

    private fun isGreetingQuery(prompt: String): Boolean {
        return prompt.contains("سلام") || prompt.contains("hello") || prompt.contains("hi") ||
                prompt.contains("hey") || prompt.contains("کیسے ہو") || prompt.contains("کیا حال") ||
                prompt.contains("how are you") || prompt.contains("नमस्ते") || prompt.contains("مرحبا") ||
                prompt.contains("hola") || prompt.contains("bonjour") || prompt.contains("hallo") ||
                prompt.contains("merhaba") || prompt.contains("کی حال")
    }

    private fun isJokeQuery(prompt: String): Boolean {
        return prompt.contains("لطیفہ") || prompt.contains("joke") || prompt.contains("funny") ||
                prompt.contains("چٹکلا") || prompt.contains("نکتة") || prompt.contains("chiste") ||
                prompt.contains("blague") || prompt.contains("witz") || prompt.contains("fıkra")
    }

    private fun isMotivationQuery(prompt: String): Boolean {
        return prompt.contains("نصیحت") || prompt.contains("حکمت") || prompt.contains("motivation") ||
                prompt.contains("quote") || prompt.contains("inspire") || prompt.contains("پریشان") ||
                prompt.contains("شاعری") || prompt.contains("thought") || prompt.contains("frase")
    }

    private fun isWeatherQuery(prompt: String): Boolean {
        return prompt.contains("موسم") || prompt.contains("weather") || prompt.contains("rain") ||
                prompt.contains("بارش") || prompt.contains("دھوپ") || prompt.contains("clima") ||
                prompt.contains("meteo") || prompt.contains("हवामान")
    }

    private fun formatCurrentTime(lang: Language): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formatted = sdf.format(Date())
        return when (lang.code) {
            "ur" -> "اس وقت گھڑی پر $formatted بج رہے ہیں۔"
            "hi" -> "इस समय $formatted बज रहे हैं।"
            "ar" -> "الوقت الحالي هو $formatted."
            "es" -> "La hora actual es $formatted."
            "fr" -> "Il est actuellement $formatted."
            "de" -> "Es ist jetzt $formatted."
            "pa" -> "اس ویلے $formatted بجے نیں۔"
            "bn" -> "এখন সময় $formatted."
            "tr" -> "Şu an saat $formatted."
            else -> "The current time is $formatted."
        }
    }

    private fun formatCurrentDate(lang: Language): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val formatted = sdf.format(Date())
        return when (lang.code) {
            "ur" -> "آج کی تاریخ $formatted ہے۔"
            "hi" -> "आज की तारीख $formatted है।"
            "ar" -> "تاريخ اليوم هو $formatted."
            "es" -> "La fecha de hoy es $formatted."
            "fr" -> "Aujourd'hui, nous sommes le $formatted."
            "de" -> "Heute ist $formatted."
            "pa" -> "اج دی تریخ $formatted ہے۔"
            "bn" -> "আজকের তারিখ $formatted।"
            "tr" -> "Bugünün tarihi $formatted."
            else -> "Today is $formatted."
        }
    }

    private fun getIdentityResponse(lang: Language): String {
        return when (lang.code) {
            "ur" -> "میرا نام مایا (Maya AI) ہے۔ میں آپ کی جدید صوتی معاون ہوں جو اردو، انگلش اور دیگر زبانوں میں آپ کی رہنمائی کرتی ہوں۔"
            "hi" -> "मेरा नाम माया (Maya AI) है। मैं आपकी बुद्धिमान वॉयस असिस्टेंट हूँ जो हिंदी, उर्दू और अंग्रेजी में आपकी मदद करती हूँ।"
            "ar" -> "اسمي مايا (Maya AI)، أنا مساعدتك الصوتية الذكية المتعددة اللغات، مستعدة دائماً لمساعدتك."
            "es" -> "Soy Maya AI, tu asistente de voz inteligente y multilingüe. ¡Estoy aquí para ayudarte!"
            "fr" -> "Je suis Maya AI, votre assistante vocale intelligente et polyglotte."
            "de" -> "Ich bin Maya AI, deine intelligente mehrsprachige Sprachassistentin."
            "pa" -> "میرا ناں مایا ہے! میں تہاڈی وائس اسسٹنٹ ہاں، جیہڑی تہاڈی بولی وچ گل بات کر سکدی اے۔"
            "bn" -> "আমার নাম মায়া। আমি আপনার বুদ্ধিমান বহুভাষিক ভয়েস সহকারী।"
            "tr" -> "Benim adım Maya AI. Size birden fazla dilde yardımcı olan akıllı ses asistanınızım."
            else -> "I am Maya AI, your intelligent multilingual voice assistant. I am designed to assist you in Urdu, English, and local languages."
        }
    }

    private fun getCapabilitiesResponse(lang: Language): String {
        return when (lang.code) {
            "ur" -> "میں آپ کے سوالات کے جوابات دے سکتی ہوں، حساب کتاب کر سکتی ہوں، وقت اور تاریخ بتا سکتی ہوں، لطیفے سنا سکتی ہوں اور آواز کے ذریعے گفتگو کر سکتی ہوں!"
            "hi" -> "मैं सवालों के जवाब दे सकती हूँ, गणना कर सकती हूँ, समय और तारीख बता सकती हूँ, और आवाज़ से बातचीत कर सकती हूँ!"
            "ar" -> "يمكنني الإجابة على أسئلتك، وإجراء العمليات الحسابية، وإخبارك بالوقت والتاريخ، والمحادثة الصوتية التفاعلية."
            "es" -> "Puedo responder tus preguntas, hacer cálculos matemáticos, decirte la hora, contarte chistes y conversar por voz."
            "fr" -> "Je peux répondre à vos questions, faire des calculs, donner l'heure, raconter des histoires et converser avec vous."
            "de" -> "Ich kann deine Fragen beantworten, mathematische Berechnungen durchführen, die Uhrzeit sagen und Witze erzählen."
            "pa" -> "میں تہاڈے سوالاں دے جواب دے سکدی ہاں، حساب کر سکدی ہاں، تے مٹھی بولی وچ گلاں کر سکدی ہاں!"
            "bn" -> "আমি আপনার প্রশ্নের উত্তর দিতে পারি, গণিত সমাধান করতে পারি এবং আপনার সাথে কথা বলতে পারি।"
            "tr" -> "Sorularınızı yanıtlayabilir, hesaplamalar yapabilir, saat ve tarihi söyleyebilir ve sesli sohbet edebilirim."
            else -> "I can answer questions, perform math calculations, tell you the time and date, share jokes, and engage in real-time voice conversations!"
        }
    }

    private fun getGreetingResponse(lang: Language): String {
        return when (lang.code) {
            "ur" -> "وعلیکم السلام! میں بالکل ٹھیک ہوں اور آپ کی مدد کے لیے تیار ہوں۔ فرمائیے، میں آپ کے لیے کیا کر سکتی ہوں؟"
            "hi" -> "नमस्ते! मैं बिल्कुल ठीक हूँ। आप कैसे हैं? आज मैं आपकी क्या मदद करूँ?"
            "ar" -> "وعليكم السلام ورحمة الله! أنا بخير وسعيدة بمحادثتك. كيف أستطيع مساعدتك اليوم؟"
            "es" -> "¡Hola! Estoy excelente y lista para ayudarte. ¿Qué te gustaría saber hoy?"
            "fr" -> "Bonjour! Je vais très bien, merci. Comment puis-je vous être utile aujourd'hui?"
            "de" -> "Hallo! Mir geht es super. Wie kann ich dir heute behilflich sein?"
            "pa" -> "سلام جی! میں بالکل ٹھیک ٹھاک ہاں۔ دسو کی حال چال اے؟"
            "bn" -> "নমস্কার! আমি খুব ভালো আছি। আজ আপনাকে কিভাবে সাহায্য করতে পারি?"
            "tr" -> "Merhaba! Ben çok iyiyim, teşekkürler. Bugün size nasıl yardımcı olabilirim?"
            else -> "Hello! I am doing great and ready to assist you. What can I do for you today?"
        }
    }

    private fun getJokeResponse(lang: Language): String {
        val urduJokes = listOf(
            "ایک آدمی ڈاکٹر کے پاس گیا اور بولا: 'ڈاکٹر صاحب، مجھے رات کو خواب میں فٹ بال کھیلتے ہوئے گدھے نظر آتے ہیں!' ڈاکٹر: 'یہ دوا رات کو سونے سے پہلے کھا لینا۔' آدمی: 'کل سے کھا لوں؟ آج رات فائنل میچ ہے!'",
            "استاد نے شاگرد سے پوچھا: 'بتاؤ، ہم پانی کیوں پیتے ہیں؟' شاگرد: 'کیونکہ استاد جی، پانی کو چبا نہیں سکتے!'",
            "ایک شخص دکان پر گیا: 'بھائی مجھے موبائل کا ایسا کور دکھائیں جو پانی سے محفوظ رکھے۔' دکاندار نے لفافہ پکڑا دیا!"
        )

        val englishJokes = listOf(
            "Why don't scientists trust atoms? Because they make up everything!",
            "Why did the computer go to the doctor? Because it caught a virus!",
            "Why do we tell actors to 'break a leg'? Because every play has a cast!"
        )

        val hindiJokes = listOf(
            "अध्यापक: 'बताओ संजू, बिजली कहाँ से आती है?' संजू: 'सर, हमारे मामा के घर से!' अध्यापक: 'वो कैसे?' संजू: 'जब भी बिजली जाती है, पापा कहते हैं - फिर काट दी सालों ने!'",
            "डॉक्टर: 'आपका वजन कैसे बढ़ गया?' मरीज: 'डॉक्टर साहब, आपने ही तो कहा था कि जो भी खाओ, मन भर के खाओ!'"
        )

        return when (lang.code) {
            "ur" -> urduJokes.random()
            "hi" -> hindiJokes.random()
            "ar" -> "سأل المعلم التلميذ: ماذا فعل الرومان بعد عبور البحر الأبيض المتوسط؟ أجاب التلميذ: جففوا ملابسهم يا أستاذ!"
            "es" -> "¿Qué le dice una impresora a otra? ¿Esa hoja es tuya o es impresión mía?"
            "fr" -> "Que dit un escargot quand il croise une limace? 'Oh, un nudiste!'"
            "de" -> "Treffen sich zwei Fische. Sagt der eine: 'Hi!' Sagt der andere: 'Wo?!'"
            "pa" -> "اک بندہ موٹر سائیکل تے جا رہیا سی، پولیس والے نے روکیا: 'ہیلمٹ کیوں نئیں پایا؟' اوہ کہن لگا: 'جناب سر گھر ای بھل آیا سی!'"
            else -> englishJokes.random()
        }
    }

    private fun getMotivationResponse(lang: Language): String {
        return when (lang.code) {
            "ur" -> "ہمیشہ یاد رکھیں: 'منزل انہی کو ملتی ہے جن کے خوابوں میں جان ہوتی ہے، پروں سے کچھ نہیں ہوتا، حوصلوں سے اڑان ہوتی ہے!'"
            "hi" -> "कोशिश करने वालों की कभी हार नहीं होती, लहरों से डरकर नौका पार नहीं होती। हमेशा सकारात्मक रहें!"
            "ar" -> "لا تيأس أبداً، فبداية كل نجاح عظيم كانت خطوة شجاعة وصبر جميل."
            "es" -> "El éxito no es el final, el fracaso no es fatal: lo que cuenta es el valor para continuar."
            "fr" -> "Chaque jour est une nouvelle chance d'apprendre, de grandir et de réussir."
            "de" -> "Der beste Weg, die Zukunft vorauszusagen, ist, sie selbst zu gestalten."
            "pa" -> "ہمت نہ ہارو، محنت کرن والیاں دی رب ہمیشہ مدد کردا اے۔"
            else -> "Believe in yourself! Every accomplishment starts with the decision to try. Keep moving forward!"
        }
    }

    private fun getWeatherResponse(lang: Language): String {
        return when (lang.code) {
            "ur" -> "آف لائن موڈ میں تفصیلی موسم کی براہ راست جانچ کے لیے انٹرنیٹ درکار ہوتا ہے، لیکن یاد رکھیں کہ باہر نکلتے وقت موسم کے مطابق لباس اور پانی کا خیال رکھیں۔"
            "hi" -> "विस्तृत मौसम जानकारी के लिए इंटरनेट की आवश्यकता होती है, लेकिन आज के दिन खुद को हाइड्रेटेड और तैयार रखें!"
            "ar" -> "للحصول على تفاصيل الطقس الحية بدقة، يُفضل الاتصال بالإنترنت. تذكر دائماً أخذ الاحتياطات اللازمة للطقس اليوم."
            "es" -> "Para obtener información meteorológica en tiempo real, conéctate a internet. ¡Disfruta de tu día al máximo!"
            else -> "Real-time weather reports require a cloud connection. Remember to check local forecasts and dress comfortably!"
        }
    }

    private fun tryEvaluateMath(prompt: String, lang: Language): String? {
        val sanitized = prompt.replace("ضرب", "*")
            .replace("تقسیم", "/")
            .replace("جمع", "+")
            .replace("منفی", "-")
            .replace("minus", "-")
            .replace("plus", "+")
            .replace("times", "*")
            .replace("into", "*")
            .replace("multiplied by", "*")
            .replace("divided by", "/")
            .replace("x", "*")
            .replace("X", "*")

        val pattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)")
        val matcher = pattern.matcher(sanitized)
        if (matcher.find()) {
            val num1 = matcher.group(1)?.toDoubleOrNull() ?: return null
            val op = matcher.group(2) ?: return null
            val num2 = matcher.group(3)?.toDoubleOrNull() ?: return null

            val result = when (op) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> return null
            }

            if (result.isNaN()) {
                return if (lang.code == "ur") "صفر پر تقسیم ممکن نہیں ہے۔" else "Division by zero is not possible."
            }

            val formattedResult = if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format(Locale.US, "%.2f", result)
            }

            return when (lang.code) {
                "ur" -> "جواب ہے: $formattedResult ($num1 $op $num2)"
                "hi" -> "उत्तर है: $formattedResult"
                "ar" -> "النتيجة هي: $formattedResult"
                "es" -> "El resultado es: $formattedResult"
                else -> "The answer is: $formattedResult"
            }
        }
        return null
    }

    private fun getConversationalFallback(prompt: String, lang: Language): String {
        return when (lang.code) {
            "ur" -> "میں نے آپ کی بات سمجھ لی ہے: '$prompt'۔ مایا آف لائن انٹیلی جنس موڈ میں فعال ہے۔ مکمل اور وسیع معلومات کے لیے آپ ترتیبات (Settings) میں کلاؤڈ AI بھی منتخب کر سکتے ہیں۔"
            "hi" -> "मैंने आपकी बात सुनी: '$prompt'। मैं ऑफलाइन मोड में भी आपकी सेवा में तैयार हूँ!"
            "ar" -> "سمعتك جيداً بخصوص: '$prompt'. أنا أعمل حالياً بالوضع غير المتصل بالإنترنت بكفاءة."
            "es" -> "He entendido tu mensaje: '$prompt'. Maya está lista para ayudarte en modo sin conexión."
            "fr" -> "J'ai bien reçu votre message: '$prompt'. Maya est prête à vous assister."
            "de" -> "Ich habe Ihre Anfrage verstanden: '$prompt'. Maya steht Ihnen zur Seite."
            "pa" -> "میں تہاڈی گل سن لئی اے: '$prompt'۔ مایا تہاڈے نال ہر ویلے حاضر اے!"
            "bn" -> "আমি আপনার বার্তা পেয়েছি: '$prompt'।"
            "tr" -> "Mesajınızı aldım: '$prompt'. Maya her zaman yanınızda."
            else -> "I heard your request: '$prompt'. Maya AI is operating in offline mode and ready to help. You can enable Cloud AI in settings for expanded queries."
        }
    }
}
