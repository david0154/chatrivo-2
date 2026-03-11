package com.teamxdevelopers.SuperChat.security

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ScamProtectionManager - High-level manager that integrates ScamDetector
 * into Chatrivo's chat flow.
 *
 * Use this in your chat activity/fragment:
 *
 *   // Before displaying a received message:
 *   ScamProtectionManager.processIncomingMessage(
 *       context = context,
 *       rawMessage = message.text,
 *       onSafe = { displayMessage(it) },
 *       onThreat = { result, sanitized -> showWarning(result); displayMessage(sanitized) }
 *   )
 *
 *   // Before user sends a message:
 *   ScamProtectionManager.validateOutgoingLink(
 *       context = context,
 *       url = linkUrl,
 *       onSafe = { sendMessage() },
 *       onBlocked = { showBlockedError() }
 *   )
 */
object ScamProtectionManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    // Firebase path for community shared blocklist
    private const val FIREBASE_BLOCKLIST_PATH = "chatrivo_security/blocklist"

    /**
     * Process an incoming message — scan URLs and spam score.
     * Calls onSafe if clean, onThreat if threats found.
     */
    fun processIncomingMessage(
        context: Context,
        rawMessage: String,
        onSafe: (String) -> Unit,
        onThreat: (List<ScamDetector.ScanResult>, String) -> Unit,
        onSpam: (Int) -> Unit = {}
    ) {
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                ScamDetector.scanMessage(rawMessage)
            }

            when {
                result.isSpam -> onSpam(result.spamScore)
                result.threats.any { it.threatLevel != ScamDetector.ThreatLevel.SAFE } -> {
                    onThreat(result.threats, result.sanitizedMessage)
                }
                else -> onSafe(result.sanitizedMessage)
            }
        }
    }

    /**
     * Validate a link before user taps it.
     * Shows appropriate dialog based on threat level.
     */
    fun handleLinkTap(
        context: Context,
        url: String,
        onOpenUrl: (String) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                ScamDetector.scanUrl(url)
            }
            when (result.action) {
                ScamDetector.ThreatAction.ALLOW -> onOpenUrl(url)
                ScamDetector.ThreatAction.CAUTION ->
                    ScamWarningDialog.show(context, result, onProceed = { onOpenUrl(url) })
                ScamDetector.ThreatAction.WARN ->
                    ScamWarningDialog.show(context, result,
                        onBlock = { /* blocked */ },
                        onProceed = { onOpenUrl(url) }
                    )
                ScamDetector.ThreatAction.BLOCK ->
                    ScamWarningDialog.show(context, result)
            }
        }
    }

    /**
     * Sync community blocklist from Firebase.
     * Call once on app start or chat open.
     */
    fun syncCommunityBlocklist() {
        try {
            FirebaseDatabase.getInstance()
                .getReference(FIREBASE_BLOCKLIST_PATH)
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { child ->
                        child.key?.let { domain -> ScamDetector.blockDomain(domain) }
                    }
                }
        } catch (_: Exception) {}
    }

    /**
     * Push a newly reported domain to the community Firebase blocklist.
     */
    fun pushToCommunityBlocklist(domain: String) {
        try {
            FirebaseDatabase.getInstance()
                .getReference(FIREBASE_BLOCKLIST_PATH)
                .child(domain.replace(".", "_"))
                .setValue(true)
        } catch (_: Exception) {}
    }

    /**
     * Returns a formatted threat summary string for display or AI context.
     */
    fun formatThreatSummary(results: List<ScamDetector.ScanResult>): String {
        return results.filter { it.threatLevel != ScamDetector.ThreatLevel.SAFE }
            .joinToString("\n") { result ->
                val icon = when (result.threatLevel) {
                    ScamDetector.ThreatLevel.HIGH -> "🚫"
                    ScamDetector.ThreatLevel.MEDIUM -> "⚠️"
                    ScamDetector.ThreatLevel.LOW -> "ℹ️"
                    ScamDetector.ThreatLevel.SAFE -> "✅"
                }
                "$icon ${result.threatLevel}: ${result.url}\n   ${result.reasons.firstOrNull() ?: ""}"
            }
    }
}
