package com.teamxdevelopers.SuperChat.ai

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ChatrivoAIBot - The main AI orchestrator for Chatrivo
 *
 * Handles:
 *  - Natural language intent detection
 *  - Weather queries → Open-Meteo
 *  - News queries → Indian RSS feeds
 *  - General AI chat → Sarvam AI
 *  - Translation requests → Sarvam Mayura
 *  - Voice message transcription → Sarvam Saarika
 *
 * Usage:
 *   ChatrivoAIBot.processMessage(
 *       context = context,
 *       apiKey = BuildConfig.SARVAM_API_KEY,
 *       userMessage = "What is the weather in Kolkata?",
 *       onTyping = { showTypingIndicator() },
 *       onReply = { reply -> displayMessage(reply) }
 *   )
 */
object ChatrivoAIBot {

    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Process a user message and return an AI-generated reply.
     * Automatically detects intent (weather / news / translate / general).
     */
    fun processMessage(
        context: Context,
        apiKey: String,
        userMessage: String,
        preferredLanguage: String = "en-IN",
        onTyping: () -> Unit = {},
        onReply: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            onTyping()
            val reply = resolveIntent(apiKey, userMessage, preferredLanguage)
            onReply(reply)
        }
    }

    private suspend fun resolveIntent(
        apiKey: String,
        message: String,
        lang: String
    ): String {
        val lower = message.lowercase()

        // --- WEATHER INTENT ---
        if (lower.contains("weather") || lower.contains("mausam") || lower.contains("আবহাওয়া")
            || lower.contains("temperature") || lower.contains("rain")
        ) {
            val city = extractCityFromMessage(lower) ?: "kolkata"
            val weather = WeatherManager.getWeatherByCity(city)
            return if (weather != null) {
                val weatherContext = weather.toNaturalLanguage()
                var aiReply = ""
                SarvamAIManager.chat(
                    apiKey = apiKey,
                    userMessage = message,
                    systemPrompt = "You are Chatrivo AI. The user asked about weather. " +
                        "Here is the real-time data: $weatherContext. " +
                        "Answer naturally and helpfully in the user's language."
                ) { reply, _ -> aiReply = reply ?: weather.toFormattedString() }
                aiReply.ifEmpty { weather.toFormattedString() }
            } else {
                "⚠️ Sorry, I couldn't fetch weather for '$city'. Please try a major Indian city like Kolkata, Mumbai, or Delhi."
            }
        }

        // --- NEWS INTENT ---
        val newsCategory = detectNewsCategory(lower)
        if (newsCategory != null) {
            val newsItems = IndianNewsManager.fetchNews(newsCategory, limit = 5)
            return if (newsItems.isNotEmpty()) {
                val newsContext = IndianNewsManager.formatForAIContext(newsItems)
                var aiSummary = ""
                SarvamAIManager.chat(
                    apiKey = apiKey,
                    userMessage = message,
                    systemPrompt = "You are Chatrivo AI. The user wants news. " +
                        "Here are the latest headlines:\n$newsContext\n" +
                        "Summarize 3-4 top stories naturally in the user's language."
                ) { reply, _ -> aiSummary = reply ?: "" }
                if (aiSummary.isNotEmpty()) aiSummary
                else IndianNewsManager.formatForChat(newsItems, newsCategory)
            } else {
                IndianNewsManager.formatForChat(emptyList(), newsCategory)
            }
        }

        // --- TRANSLATION INTENT ---
        if (lower.contains("translate") || lower.contains("anuvad") ||
            lower.contains("in hindi") || lower.contains("in bengali") ||
            lower.contains("in tamil")
        ) {
            val targetLang = when {
                lower.contains("hindi") -> "hi-IN"
                lower.contains("bengali") -> "bn-IN"
                lower.contains("tamil") -> "ta-IN"
                lower.contains("telugu") -> "te-IN"
                lower.contains("kannada") -> "kn-IN"
                lower.contains("malayalam") -> "ml-IN"
                lower.contains("marathi") -> "mr-IN"
                lower.contains("gujarati") -> "gu-IN"
                lower.contains("punjabi") -> "pa-IN"
                lower.contains("english") -> "en-IN"
                else -> "hi-IN"
            }
            // Extract text to translate (everything after "translate")
            val textToTranslate = message
                .replace(Regex("translate|anuvad|in hindi|in bengali|in tamil|in telugu|in kannada|into", RegexOption.IGNORE_CASE), "")
                .trim()
                .ifEmpty { message }

            var translationResult = ""
            SarvamAIManager.translate(
                apiKey = apiKey,
                text = textToTranslate,
                sourceLanguage = "en-IN",
                targetLanguage = targetLang
            ) { result, error ->
                translationResult = result ?: (error ?: "Translation failed")
            }
            return "🔤 Translation:\n$translationResult"
        }

        // --- GENERAL AI CHAT (Sarvam-M) ---
        var generalReply = ""
        SarvamAIManager.chat(
            apiKey = apiKey,
            userMessage = message
        ) { reply, error ->
            generalReply = reply ?: (error ?: "I'm sorry, I couldn't process that. Please try again.")
        }
        return generalReply
    }

    private fun extractCityFromMessage(message: String): String? {
        val cities = listOf(
            "kolkata", "mumbai", "delhi", "bangalore", "bengaluru", "chennai",
            "hyderabad", "pune", "ahmedabad", "jaipur", "lucknow", "patna",
            "bhopal", "indore", "surat", "nagpur", "chandigarh", "kochi", "guwahati"
        )
        return cities.firstOrNull { message.contains(it) }
    }

    private fun detectNewsCategory(message: String): IndianNewsManager.NewsCategory? {
        return when {
            message.contains("sports") || message.contains("cricket") || message.contains("football") ->
                IndianNewsManager.NewsCategory.SPORTS
            message.contains("tech") || message.contains("technology") ->
                IndianNewsManager.NewsCategory.TECHNOLOGY
            message.contains("business") || message.contains("economy") || message.contains("stock") ->
                IndianNewsManager.NewsCategory.BUSINESS
            message.contains("bollywood") || message.contains("entertainment") || message.contains("movie") ->
                IndianNewsManager.NewsCategory.BOLLYWOOD
            message.contains("kolkata") && (message.contains("news") || message.contains("samachar")) ->
                IndianNewsManager.NewsCategory.KOLKATA
            message.contains("world") || message.contains("international") ->
                IndianNewsManager.NewsCategory.WORLD
            message.contains("science") ->
                IndianNewsManager.NewsCategory.SCIENCE
            message.contains("hindi") && message.contains("news") ->
                IndianNewsManager.NewsCategory.HINDI_TOP
            message.contains("news") || message.contains("samachar") || message.contains("khabar") ->
                IndianNewsManager.NewsCategory.TOP_INDIA
            else -> null
        }
    }
}
