package com.teamxdevelopers.SuperChat.security

import com.google.firebase.database.FirebaseDatabase

/**
 * SpamFolder - Auto-filter and manage spam messages for Chatrivo
 *
 * Stores flagged messages in Firebase under:
 *   users/{userId}/spam_messages/{messageId}
 *
 * Usage:
 *   SpamFolder.moveToSpam(userId, messageId, messageData)
 *   SpamFolder.restoreFromSpam(userId, messageId)
 *   SpamFolder.clearSpam(userId)
 */
object SpamFolder {

    private val db = FirebaseDatabase.getInstance()
    private fun spamRef(userId: String) = db.getReference("users/$userId/spam_messages")
    private fun chatsRef(userId: String) = db.getReference("users/$userId/chats")

    data class SpamMessage(
        val messageId: String = "",
        val senderId: String = "",
        val content: String = "",
        val spamScore: Int = 0,
        val reasons: List<String> = emptyList(),
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Move a message to spam folder.
     */
    fun moveToSpam(userId: String, messageId: String, message: SpamMessage) {
        spamRef(userId).child(messageId).setValue(message)
        // Remove from main chat
        chatsRef(userId).child(messageId).removeValue()
    }

    /**
     * Restore a message from spam back to chats.
     */
    fun restoreFromSpam(userId: String, messageId: String, onSuccess: () -> Unit = {}) {
        spamRef(userId).child(messageId).get().addOnSuccessListener { snapshot ->
            val message = snapshot.getValue(SpamMessage::class.java) ?: return@addOnSuccessListener
            chatsRef(userId).child(messageId).setValue(message)
            spamRef(userId).child(messageId).removeValue()
            onSuccess()
        }
    }

    /**
     * Clear all spam messages for a user.
     */
    fun clearSpam(userId: String, onSuccess: () -> Unit = {}) {
        spamRef(userId).removeValue().addOnSuccessListener { onSuccess() }
    }

    /**
     * Get spam message count for badge display.
     */
    fun getSpamCount(userId: String, onCount: (Int) -> Unit) {
        spamRef(userId).get().addOnSuccessListener { snapshot ->
            onCount(snapshot.childrenCount.toInt())
        }
    }
}
