package com.teamxdevelopers.SuperChat.security

import java.net.URI

/**
 * ScamDetector - Core fraud detection engine for Chatrivo
 *
 * Detection methods:
 *  1. Phishing domain matching (known fake banking/payment sites)
 *  2. Typosquatting via Levenshtein distance against popular domains
 *  3. Homograph attack detection (lookalike Unicode chars)
 *  4. Suspicious TLD blocking (.tk .ml .xyz .gq .cf etc.)
 *  5. URL shortener detection (bit.ly, t.co, tinyurl etc.)
 *  6. Direct IP address link detection
 *  7. Spam keyword scoring
 *  8. Excessive subdomain detection
 *  9. Context-based scam pattern matching
 */
object ScamDetector {

    // -----------------------------------------------------------------------
    // ENUMS & DATA CLASSES
    // -----------------------------------------------------------------------

    enum class ThreatLevel { HIGH, MEDIUM, LOW, SAFE }

    data class ScanResult(
        val url: String,
        val threatLevel: ThreatLevel,
        val reasons: List<String>,
        val action: ThreatAction,
        val safeUrl: String? = null          // cleaned/replaced version if blocked
    )

    data class MessageScanResult(
        val originalMessage: String,
        val sanitizedMessage: String,        // dangerous links replaced with warning
        val threats: List<ScanResult>,
        val spamScore: Int,                  // 0-100
        val isSpam: Boolean
    )

    enum class ThreatAction {
        BLOCK,          // Remove link entirely, replace with warning
        WARN,           // Show warning dialog, let user decide
        CAUTION,        // Show small caution badge on link
        ALLOW           // Safe — no action
    }

    // -----------------------------------------------------------------------
    // KNOWN PHISHING / MALICIOUS DOMAINS
    // -----------------------------------------------------------------------

    private val knownPhishingDomains = setOf(
        "paypa1.com", "paypai.com", "paypa-l.com", "pay-pal.support",
        "amazonsupport.com", "amazon-verify.com", "amaz0n.com",
        "secure-bankofamerica.com", "bankofamerica-secure.com",
        "google-account-verify.com", "accounts-google.com",
        "apple-id-support.com", "appleid-verify.com",
        "sbi-online-secure.com", "sbibank-verify.com", "sbionline-alert.com",
        "hdfcbank-secure.com", "hdfc-netbanking-alert.com",
        "iciciveri.com", "icici-alert-secure.com",
        "paytm-offer.com", "paytm-kyc-verify.com",
        "phonepe-reward.com", "phonepe-verify.com",
        "upi-payment-verify.com", "npci-kyc.com",
        "winner-prize.com", "claim-prize-now.com",
        "freegiftcard.net", "winprize2024.com",
        "covid-relief-fund.com", "pm-relief-fund.com"
    )

    // -----------------------------------------------------------------------
    // POPULAR LEGITIMATE DOMAINS (for typosquatting check)
    // -----------------------------------------------------------------------

    private val popularDomains = listOf(
        "google.com", "facebook.com", "instagram.com", "twitter.com",
        "amazon.com", "flipkart.com", "snapdeal.com", "meesho.com",
        "paypal.com", "paytm.com", "phonepe.com", "gpay.com",
        "sbi.co.in", "hdfcbank.com", "icicibank.com", "axisbank.com",
        "kotakbank.com", "pnbindia.in", "bankofbaroda.in",
        "apple.com", "microsoft.com", "yahoo.com", "gmail.com",
        "youtube.com", "whatsapp.com", "telegram.org",
        "irctc.co.in", "npci.org.in", "upi.npci.org.in"
    )

    // -----------------------------------------------------------------------
    // SUSPICIOUS TLDs
    // -----------------------------------------------------------------------

    private val suspiciousTlds = setOf(
        ".tk", ".ml", ".gq", ".cf", ".xyz",
        ".top", ".click", ".loan", ".win", ".download",
        ".stream", ".racing", ".party", ".review", ".trade"
    )

    // -----------------------------------------------------------------------
    // URL SHORTENERS
    // -----------------------------------------------------------------------

    private val urlShorteners = setOf(
        "bit.ly", "t.co", "tinyurl.com", "goo.gl", "ow.ly",
        "short.ly", "buff.ly", "is.gd", "rb.gy", "cutt.ly",
        "shorte.st", "adf.ly", "bc.vc", "clk.sh"
    )

    // -----------------------------------------------------------------------
    // WHITELISTED SAFE DOMAINS
    // -----------------------------------------------------------------------

    private val whitelist = mutableSetOf(
        "google.com", "youtube.com", "facebook.com", "instagram.com",
        "twitter.com", "x.com", "linkedin.com", "github.com",
        "wikipedia.org", "stackoverflow.com",
        "amazon.com", "flipkart.com", "paytm.com", "phonepe.com",
        "sbi.co.in", "hdfcbank.com", "icicibank.com",
        "irctc.co.in", "npci.org.in",
        "apple.com", "microsoft.com", "whatsapp.com", "telegram.org"
    )

    // -----------------------------------------------------------------------
    // BLOCKED DOMAINS (user-reported + auto-detected)
    // -----------------------------------------------------------------------

    private val blockedDomains = mutableSetOf<String>()

    // -----------------------------------------------------------------------
    // SPAM KEYWORDS (weighted scores)
    // -----------------------------------------------------------------------

    private val spamKeywords = mapOf(
        // High score (obvious scam)
        "you have won" to 30, "congratulations you won" to 30,
        "click to claim" to 25, "claim your prize" to 25,
        "free iphone" to 25, "free recharge" to 20,
        "otp share" to 35, "share otp" to 35,
        "bank account verify" to 30, "kyc update" to 25, "kyc expire" to 25,
        "account blocked" to 20, "account suspended" to 20,
        "lottery winner" to 30, "lucky draw" to 20,
        "send money" to 15, "urgent transfer" to 20,
        "100% guaranteed" to 15, "act now" to 10,
        "limited time offer" to 10, "exclusive deal" to 8,
        // Medium score
        "verify your" to 12, "confirm your" to 12,
        "password expired" to 15, "update your" to 8,
        "prize money" to 20, "gift voucher" to 12,
        "earn from home" to 15, "work from home earn" to 15,
        "investment return" to 15, "double your money" to 25,
        // Low score
        "click here" to 5, "limited offer" to 5,
        "special offer" to 3, "discount" to 2
    )

    // -----------------------------------------------------------------------
    // HOMOGRAPH UNICODE MAP (lookalike chars → ASCII)
    // -----------------------------------------------------------------------

    private val homographMap = mapOf(
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c',
        'х' to 'x', 'А' to 'A', 'В' to 'B', 'Е' to 'E', 'К' to 'K',
        'М' to 'M', 'Н' to 'H', 'О' to 'O', 'Р' to 'P', 'С' to 'C',
        'Т' to 'T', 'Х' to 'X', '0' to 'o', '1' to 'l',
        // Greek lookalikes
        'α' to 'a', 'β' to 'b', 'ο' to 'o', 'ρ' to 'p',
        // Common digit substitutions in domains
        '3' to 'e', '4' to 'a', '5' to 's', '6' to 'b', '8' to 'b'
    )

    // -----------------------------------------------------------------------
    // PUBLIC API
    // -----------------------------------------------------------------------

    /**
     * Scan a single URL for threats.
     */
    fun scanUrl(url: String): ScanResult {
        val reasons = mutableListOf<String>()
        var highestThreat = ThreatLevel.SAFE

        val domain = extractDomain(url) ?: run {
            // Malformed URL — treat as suspicious
            return ScanResult(url, ThreatLevel.MEDIUM,
                listOf("Malformed or unreadable URL"), ThreatAction.WARN)
        }

        // 0. User-blocked domain
        if (blockedDomains.contains(domain)) {
            return ScanResult(url, ThreatLevel.HIGH,
                listOf("This domain was blocked by you or reported by the community"),
                ThreatAction.BLOCK, "[BLOCKED LINK — $domain]")
        }

        // 1. Whitelist check — if whitelisted, skip further analysis
        if (isWhitelisted(domain)) {
            return ScanResult(url, ThreatLevel.SAFE, listOf("Verified safe domain"), ThreatAction.ALLOW)
        }

        // 2. Known phishing domain
        if (knownPhishingDomains.contains(domain)) {
            reasons.add("Known phishing/malicious domain")
            highestThreat = ThreatLevel.HIGH
        }

        // 3. Direct IP address
        if (isDirectIp(domain)) {
            reasons.add("Direct IP address link — legitimate sites don't use raw IPs")
            highestThreat = maxThreat(highestThreat, ThreatLevel.HIGH)
        }

        // 4. Suspicious TLD
        suspiciousTlds.forEach { tld ->
            if (domain.endsWith(tld)) {
                reasons.add("Suspicious free TLD: $tld")
                highestThreat = maxThreat(highestThreat, ThreatLevel.MEDIUM)
            }
        }

        // 5. URL Shortener
        if (urlShorteners.any { domain == it || domain.endsWith(".$it") }) {
            reasons.add("URL shortener — destination is hidden")
            highestThreat = maxThreat(highestThreat, ThreatLevel.LOW)
        }

        // 6. Typosquatting (Levenshtein distance)
        val typoResult = detectTyposquatting(domain)
        if (typoResult != null) {
            reasons.add("Typosquatting: '$domain' looks like '${typoResult.second}' (${typoResult.first} char difference)")
            highestThreat = maxThreat(highestThreat, ThreatLevel.HIGH)
        }

        // 7. Homograph attack
        val normalized = normalizeHomographs(domain)
        if (normalized != domain) {
            val similar = popularDomains.firstOrNull { it == normalized }
            if (similar != null) {
                reasons.add("Homograph attack: uses lookalike Unicode characters to mimic '$similar'")
                highestThreat = maxThreat(highestThreat, ThreatLevel.HIGH)
            }
        }

        // 8. Excessive subdomains (e.g. secure.verify.bank.xyz)
        val subdomainCount = domain.count { it == '.' }
        if (subdomainCount >= 3) {
            reasons.add("Excessive subdomains ($subdomainCount levels) — common in phishing")
            highestThreat = maxThreat(highestThreat, ThreatLevel.MEDIUM)
        }

        // 9. HTTP (not HTTPS) for financial keywords
        if (url.startsWith("http://") && containsFinancialKeywords(url)) {
            reasons.add("Insecure HTTP connection with financial/banking keywords")
            highestThreat = maxThreat(highestThreat, ThreatLevel.MEDIUM)
        }

        val action = when (highestThreat) {
            ThreatLevel.HIGH -> ThreatAction.BLOCK
            ThreatLevel.MEDIUM -> ThreatAction.WARN
            ThreatLevel.LOW -> ThreatAction.CAUTION
            ThreatLevel.SAFE -> ThreatAction.ALLOW
        }

        val safeUrl = if (action == ThreatAction.BLOCK)
            "⚠️ [DANGEROUS LINK REMOVED — ${domain}]"
        else null

        return ScanResult(url, highestThreat, reasons, action, safeUrl)
    }

    /**
     * Scan an entire chat message: extract URLs, scan each, score for spam.
     */
    fun scanMessage(message: String): MessageScanResult {
        val urls = extractUrls(message)
        val threats = urls.map { scanUrl(it) }

        // Replace dangerous links in message
        var sanitized = message
        threats.forEach { result ->
            if (result.action == ThreatAction.BLOCK && result.safeUrl != null) {
                sanitized = sanitized.replace(result.url, result.safeUrl)
            }
        }

        val spamScore = calculateSpamScore(message)

        return MessageScanResult(
            originalMessage = message,
            sanitizedMessage = sanitized,
            threats = threats,
            spamScore = spamScore,
            isSpam = spamScore >= 50
        )
    }

    // -----------------------------------------------------------------------
    // WHITELIST / BLOCKLIST MANAGEMENT
    // -----------------------------------------------------------------------

    fun addToWhitelist(domain: String) {
        whitelist.add(cleanDomain(domain))
    }

    fun blockDomain(domain: String) {
        blockedDomains.add(cleanDomain(domain))
        whitelist.remove(cleanDomain(domain))
    }

    fun isWhitelisted(domain: String): Boolean {
        val clean = cleanDomain(domain)
        return whitelist.any { clean == it || clean.endsWith(".${it}") }
    }

    fun reportScam(url: String) {
        val domain = extractDomain(url) ?: return
        blockDomain(domain)
        // TODO: Push to Firebase Realtime DB shared community blocklist
        // FirebaseDatabase.getInstance().getReference("blocklist").child(domain).setValue(true)
    }

    // -----------------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------------

    private fun extractDomain(url: String): String? {
        return try {
            val uri = URI(url.trim())
            uri.host?.lowercase()?.removePrefix("www.")
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanDomain(input: String): String =
        input.lowercase().trim().removePrefix("www.").removePrefix("http://").removePrefix("https://").substringBefore("/")

    private fun isDirectIp(domain: String): Boolean {
        return domain.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))
    }

    private fun extractUrls(text: String): List<String> {
        val urlRegex = Regex("(https?://[^\\s]+|www\\.[^\\s]+)", RegexOption.IGNORE_CASE)
        return urlRegex.findAll(text).map { it.value }.toList()
    }

    private fun containsFinancialKeywords(url: String): Boolean {
        val financial = listOf("bank", "pay", "upi", "wallet", "card", "account",
            "verify", "login", "secure", "password", "otp", "kyc")
        val lower = url.lowercase()
        return financial.any { lower.contains(it) }
    }

    private fun normalizeHomographs(input: String): String {
        return input.map { homographMap[it] ?: it }.joinToString("")
    }

    /**
     * Levenshtein distance between two strings.
     */
    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    /**
     * Detect typosquatting: returns Pair(distance, similar_domain) if suspicious.
     * Threshold: edit distance of 1 or 2 for domains with length > 5.
     */
    private fun detectTyposquatting(domain: String): Pair<Int, String>? {
        // Remove TLD for comparison
        val domainBase = domain.substringBeforeLast(".")
        for (popular in popularDomains) {
            val popularBase = popular.substringBeforeLast(".")
            val distance = levenshtein(domainBase, popularBase)
            val threshold = if (popularBase.length > 7) 2 else 1
            if (distance in 1..threshold && domainBase != popularBase) {
                return Pair(distance, popular)
            }
        }
        return null
    }

    private fun calculateSpamScore(message: String): Int {
        val lower = message.lowercase()
        var score = 0
        spamKeywords.forEach { (keyword, weight) ->
            if (lower.contains(keyword)) score += weight
        }
        // Bonus score for ALL CAPS messages
        val capsRatio = message.count { it.isUpperCase() }.toFloat() / message.length.coerceAtLeast(1)
        if (capsRatio > 0.5f) score += 15
        // Excessive exclamation marks
        if (message.count { it == '!' } >= 3) score += 10
        return score.coerceIn(0, 100)
    }

    private fun maxThreat(a: ThreatLevel, b: ThreatLevel): ThreatLevel {
        val order = listOf(ThreatLevel.SAFE, ThreatLevel.LOW, ThreatLevel.MEDIUM, ThreatLevel.HIGH)
        return if (order.indexOf(a) >= order.indexOf(b)) a else b
    }
}
