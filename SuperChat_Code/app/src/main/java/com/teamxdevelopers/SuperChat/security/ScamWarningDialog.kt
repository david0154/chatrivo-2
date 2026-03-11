package com.teamxdevelopers.SuperChat.security

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

/**
 * ScamWarningDialog - Visual threat warning UI for Chatrivo
 *
 * Shows context-appropriate dialogs based on threat level:
 *  - HIGH  → Red blocking dialog with auto-removal confirmation
 *  - MEDIUM → Orange warning with "Proceed at your own risk" option
 *  - LOW   → Yellow caution info with "open anyway" option
 *
 * Usage:
 *   ScamWarningDialog.show(context, scanResult,
 *       onBlock = { /* link blocked */ },
 *       onProceed = { /* user chose to open anyway */ }
 *   )
 */
object ScamWarningDialog {

    fun show(
        context: Context,
        result: ScamDetector.ScanResult,
        onBlock: () -> Unit = {},
        onProceed: () -> Unit = {},
        onReport: () -> Unit = {}
    ) {
        when (result.threatLevel) {
            ScamDetector.ThreatLevel.HIGH -> showHighThreat(context, result, onBlock, onReport)
            ScamDetector.ThreatLevel.MEDIUM -> showMediumThreat(context, result, onBlock, onProceed, onReport)
            ScamDetector.ThreatLevel.LOW -> showLowThreat(context, result, onProceed)
            ScamDetector.ThreatLevel.SAFE -> onProceed() // open directly
        }
    }

    private fun showHighThreat(
        context: Context,
        result: ScamDetector.ScanResult,
        onBlock: () -> Unit,
        onReport: () -> Unit
    ) {
        val reasonText = result.reasons.joinToString("\n• ", prefix = "• ")
        AlertDialog.Builder(context)
            .setTitle("🚫 DANGEROUS LINK BLOCKED")
            .setMessage(
                "This link has been blocked for your safety.\n\n" +
                "URL: ${result.url}\n\n" +
                "Threat: HIGH RISK\n\n" +
                "Reasons:\n$reasonText\n\n" +
                "⚠️ This may be a phishing or scam attempt designed to steal your personal information or money."
            )
            .setPositiveButton("OK, I'm Safe") { _, _ -> onBlock() }
            .setNeutralButton("Report Scam") { _, _ ->
                ScamDetector.reportScam(result.url)
                onReport()
                onBlock()
            }
            .setCancelable(false)
            .show()
    }

    private fun showMediumThreat(
        context: Context,
        result: ScamDetector.ScanResult,
        onBlock: () -> Unit,
        onProceed: () -> Unit,
        onReport: () -> Unit
    ) {
        val reasonText = result.reasons.joinToString("\n• ", prefix = "• ")
        AlertDialog.Builder(context)
            .setTitle("⚠️ Suspicious Link Warning")
            .setMessage(
                "This link appears suspicious.\n\n" +
                "URL: ${result.url}\n\n" +
                "Threat: MEDIUM RISK\n\n" +
                "Reasons:\n$reasonText\n\n" +
                "Do you want to open this link?"
            )
            .setPositiveButton("Block This Link") { _, _ -> onBlock() }
            .setNegativeButton("Open Anyway (Risk)") { _, _ -> onProceed() }
            .setNeutralButton("Report") { _, _ ->
                ScamDetector.reportScam(result.url)
                onReport()
                onBlock()
            }
            .show()
    }

    private fun showLowThreat(
        context: Context,
        result: ScamDetector.ScanResult,
        onProceed: () -> Unit
    ) {
        val reasonText = result.reasons.joinToString("\n• ", prefix = "• ")
        AlertDialog.Builder(context)
            .setTitle("ℹ️ Caution")
            .setMessage(
                "This link has a minor concern.\n\n" +
                "URL: ${result.url}\n\n" +
                "Reason: $reasonText\n\n" +
                "You can still open it, but proceed carefully."
            )
            .setPositiveButton("Open Carefully") { _, _ -> onProceed() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Show a spam detection alert at the top of the chat.
     */
    fun showSpamBanner(
        context: Context,
        spamScore: Int,
        onDismiss: () -> Unit = {},
        onMoveToSpam: () -> Unit = {}
    ) {
        AlertDialog.Builder(context)
            .setTitle("📥 Spam Detected")
            .setMessage(
                "This message was flagged as potential spam.\n\n" +
                "Spam confidence: $spamScore%\n\n" +
                "Would you like to move it to your Spam Folder?"
            )
            .setPositiveButton("Move to Spam") { _, _ -> onMoveToSpam() }
            .setNegativeButton("Keep") { _, _ -> onDismiss() }
            .show()
    }
}
