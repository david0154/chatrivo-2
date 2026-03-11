package com.teamxdevelopers.SuperChat.ai

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamxdevelopers.SuperChat.BuildConfig
// import com.teamxdevelopers.SuperChat.databinding.ActivityAiChatBinding

/**
 * AIChatActivity - Full-screen AI chat experience in Chatrivo
 *
 * Features:
 *  - Chat with Sarvam AI (sarvam-m model)
 *  - Real-time weather integration
 *  - Indian news summaries
 *  - Multi-language translation
 *  - Voice message transcription (ASR)
 *  - Text-to-Speech replies
 *
 * Layout: activity_ai_chat.xml  (see res/layout)
 * Triggered from: MainActivity bottom bar or a contact chat menu option
 *
 * Setup:
 *  1. Add to AndroidManifest.xml:
 *     <activity android:name=".ai.AIChatActivity" />
 *  2. Add Sarvam API key in build.gradle:
 *     resValue 'string', "sarvam_api_key", "YOUR_KEY_HERE"
 */
class AIChatActivity : AppCompatActivity() {

    // private lateinit var binding: ActivityAiChatBinding
    private val chatHistory = mutableListOf<Pair<String, Boolean>>() // message, isUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding = ActivityAiChatBinding.inflate(layoutInflater)
        // setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        // Toolbar
        supportActionBar?.apply {
            title = "Chatrivo AI"
            setDisplayHomeAsUpEnabled(true)
        }

        // Send button click — wire to your actual UI
        // binding.btnSend.setOnClickListener { sendMessage() }
        // binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
        //     if (actionId == EditorInfo.IME_ACTION_SEND) sendMessage()
        //     true
        // }
    }

    private fun sendMessage() {
        // val text = binding.etMessage.text.toString().trim()
        val text = "" // Replace with actual EditText reference
        if (text.isEmpty()) return

        val apiKey = getString(resources.getIdentifier("sarvam_api_key", "string", packageName))
        // binding.etMessage.setText("")

        // Show typing indicator
        // binding.typingIndicator.visibility = View.VISIBLE

        ChatrivoAIBot.processMessage(
            context = this,
            apiKey = apiKey,
            userMessage = text,
            onTyping = {
                // binding.typingIndicator.visibility = View.VISIBLE
            },
            onReply = { reply ->
                // binding.typingIndicator.visibility = View.GONE
                // Add reply to chat adapter
                chatHistory.add(Pair(reply, false))
                // adapter.notifyItemInserted(chatHistory.size - 1)
                // binding.rvChat.scrollToPosition(chatHistory.size - 1)
            },
            onError = { error ->
                // binding.typingIndicator.visibility = View.GONE
                Toast.makeText(this, "AI Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
