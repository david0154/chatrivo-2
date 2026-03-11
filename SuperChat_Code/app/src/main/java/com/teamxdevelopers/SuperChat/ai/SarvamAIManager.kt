package com.teamxdevelopers.SuperChat.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * SarvamAIManager - Integrates Sarvam AI APIs for Chatrivo
 *
 * Features:
 *  - Chat/Text generation (sarvam-m model)
 *  - Text-to-Speech in Indian languages (Hindi, Bengali, Tamil, Telugu, etc.)
 *  - Speech-to-Text (Saarika model)
 *  - Language translation (Mayura model)
 *
 * Replace SARVAM_API_KEY in build.gradle resValue or pass directly.
 */
object SarvamAIManager {

    private const val BASE_URL = "https://api.sarvam.ai"
    private val client = OkHttpClient()

    // ---------------------------------------------------------------------------
    // 1. CHAT / TEXT GENERATION
    // ---------------------------------------------------------------------------

    /**
     * Send a chat message to Sarvam AI and get a text reply.
     * @param apiKey  Your Sarvam AI API key
     * @param userMessage  The user's message
     * @param systemPrompt  Optional system prompt to shape AI personality
     * @param onResult  Callback: (reply: String?, error: String?)
     */
    suspend fun chat(
        apiKey: String,
        userMessage: String,
        systemPrompt: String = "You are Chatrivo AI, a helpful assistant built into the Chatrivo messaging app. Answer in the same language the user writes.",
        onResult: (String?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            }
            val body = JSONObject().apply {
                put("model", "sarvam-m")
                put("messages", messages)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/v1/chat/completions")
                .addHeader("api-subscription-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onResult(null, "HTTP ${response.code}: ${response.message}")
                    return@withContext
                }
                val json = JSONObject(response.body?.string() ?: "")
                val reply = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                onResult(reply, null)
            }
        } catch (e: Exception) {
            onResult(null, e.message)
        }
    }

    // ---------------------------------------------------------------------------
    // 2. TEXT-TO-SPEECH  (returns Base64 audio or writes to file)
    // ---------------------------------------------------------------------------

    /**
     * Convert text to speech using Sarvam Bulbul TTS.
     * Supported languages: hi-IN, bn-IN, ta-IN, te-IN, kn-IN, ml-IN, mr-IN, gu-IN, pa-IN, en-IN
     * @param onResult Callback: (base64Audio: String?, error: String?)
     */
    suspend fun textToSpeech(
        apiKey: String,
        text: String,
        languageCode: String = "hi-IN",
        speakerName: String = "meera",   // meera / arvind / amol / etc.
        onResult: (String?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("inputs", JSONArray().put(text))
                put("target_language_code", languageCode)
                put("speaker", speakerName)
                put("model", "bulbul:v1")
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/text-to-speech")
                .addHeader("api-subscription-key", apiKey)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onResult(null, "HTTP ${response.code}: ${response.message}")
                    return@withContext
                }
                val json = JSONObject(response.body?.string() ?: "")
                val audios = json.getJSONArray("audios")
                onResult(audios.getString(0), null)
            }
        } catch (e: Exception) {
            onResult(null, e.message)
        }
    }

    // ---------------------------------------------------------------------------
    // 3. SPEECH-TO-TEXT  (Saarika model)
    // ---------------------------------------------------------------------------

    /**
     * Transcribe audio to text.
     * @param audioFilePath Path to the WAV/MP3 audio file
     * @param languageCode  Source language, e.g. "hi-IN"
     */
    suspend fun speechToText(
        apiKey: String,
        audioFilePath: String,
        languageCode: String = "hi-IN",
        onResult: (String?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val audioFile = java.io.File(audioFilePath)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.name,
                    audioFile.readBytes().toRequestBody("audio/*".toMediaType()))
                .addFormDataPart("language_code", languageCode)
                .addFormDataPart("model", "saarika:v2")
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/speech-to-text")
                .addHeader("api-subscription-key", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onResult(null, "HTTP ${response.code}: ${response.message}")
                    return@withContext
                }
                val json = JSONObject(response.body?.string() ?: "")
                onResult(json.getString("transcript"), null)
            }
        } catch (e: Exception) {
            onResult(null, e.message)
        }
    }

    // ---------------------------------------------------------------------------
    // 4. LANGUAGE TRANSLATION  (Mayura model)
    // ---------------------------------------------------------------------------

    /**
     * Translate text between Indian languages.
     * @param sourceLanguage e.g. "en-IN"
     * @param targetLanguage e.g. "hi-IN"
     */
    suspend fun translate(
        apiKey: String,
        text: String,
        sourceLanguage: String = "en-IN",
        targetLanguage: String = "hi-IN",
        onResult: (String?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("input", text)
                put("source_language_code", sourceLanguage)
                put("target_language_code", targetLanguage)
                put("model", "mayura:v1")
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/translate")
                .addHeader("api-subscription-key", apiKey)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onResult(null, "HTTP ${response.code}: ${response.message}")
                    return@withContext
                }
                val json = JSONObject(response.body?.string() ?: "")
                onResult(json.getString("translated_text"), null)
            }
        } catch (e: Exception) {
            onResult(null, e.message)
        }
    }
}
