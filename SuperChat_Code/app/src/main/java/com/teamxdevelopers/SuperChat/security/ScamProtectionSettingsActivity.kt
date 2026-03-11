package com.teamxdevelopers.SuperChat.security

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ScamProtectionSettingsActivity - Manage scam protection settings
 *
 * Features accessible to users:
 *  - Toggle scam protection on/off
 *  - View blocked domains list
 *  - Add/remove whitelist entries
 *  - View spam folder
 *  - Report a scam URL manually
 *  - See protection statistics
 *
 * Register in AndroidManifest.xml:
 *  <activity android:name=".security.ScamProtectionSettingsActivity"
 *            android:label="Scam Protection Settings" />
 */
class ScamProtectionSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_scam_settings)   ← wire your layout
        supportActionBar?.apply {
            title = "🛡️ Scam Protection"
            setDisplayHomeAsUpEnabled(true)
        }
        setupSettings()
    }

    private fun setupSettings() {
        // Example: Whitelist a domain
        // binding.btnAddWhitelist.setOnClickListener {
        //     val domain = binding.etDomain.text.toString().trim()
        //     ScamDetector.addToWhitelist(domain)
        //     Toast.makeText(this, "$domain added to whitelist", Toast.LENGTH_SHORT).show()
        // }

        // Example: Block a domain
        // binding.btnBlockDomain.setOnClickListener {
        //     val domain = binding.etDomain.text.toString().trim()
        //     ScamDetector.blockDomain(domain)
        //     ScamProtectionManager.pushToCommunityBlocklist(domain)
        //     Toast.makeText(this, "$domain blocked", Toast.LENGTH_SHORT).show()
        // }

        // Example: Report a scam
        // binding.btnReportScam.setOnClickListener {
        //     val url = binding.etReportUrl.text.toString().trim()
        //     ScamDetector.reportScam(url)
        //     ScamProtectionManager.pushToCommunityBlocklist(
        //         url.substringAfter("//").substringBefore("/")
        //     )
        //     Toast.makeText(this, "Scam reported. Thank you for keeping the community safe!", Toast.LENGTH_LONG).show()
        // }
    }

    /**
     * Quick scan test — use from debug menu or settings
     */
    fun runQuickScan(url: String): String {
        val result = ScamDetector.scanUrl(url)
        return buildString {
            append("URL: ${result.url}\n")
            append("Threat Level: ${result.threatLevel}\n")
            append("Action: ${result.action}\n")
            if (result.reasons.isNotEmpty()) {
                append("Reasons:\n")
                result.reasons.forEach { append("  • $it\n") }
            }
        }
    }
}
