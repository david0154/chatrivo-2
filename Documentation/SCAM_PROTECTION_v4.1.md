# Chatrivo v4.1 — Scam Protection & Fraud Detection

> Added in Chatrivo v4.1 | March 2026

---

## 🛡️ Overview

Chatrivo's built-in Scam Protection engine scans every incoming message and link in real-time using a multi-layer local detection system — **no external API required**, fully offline-capable.

---

## 📁 New Files

```
SuperChat_Code/app/src/main/java/.../security/
├── ScamDetector.kt                  ← Core engine (10 detection methods)
├── ScamWarningDialog.kt             ← Threat UI dialogs (HIGH/MEDIUM/LOW)
├── ScamProtectionManager.kt         ← High-level chat integration manager
├── ScamProtectionSettingsActivity.kt ← User settings for scam protection
└── SpamFolder.kt                    ← Spam message storage (Firebase)
```

---

## 🔍 Detection Methods

| Method | Class | Description |
|--------|-------|-------------|
| Phishing Domain DB | `ScamDetector` | Hardcoded list of 30+ known fake domains |
| Levenshtein Distance | `ScamDetector.detectTyposquatting()` | Edit distance ≤ 2 against 25 popular domains |
| Homograph Detection | `ScamDetector.normalizeHomographs()` | Converts lookalike Unicode → ASCII |
| Suspicious TLD | `ScamDetector` | Blocks .tk .ml .gq .xyz .top .click etc. |
| URL Shortener | `ScamDetector` | Flags bit.ly, tinyurl, goo.gl etc. |
| Direct IP Links | `ScamDetector.isDirectIp()` | Regex match for raw IP addresses |
| Spam Keyword Scoring | `ScamDetector.calculateSpamScore()` | 50+ keywords, weighted 2–35 points each |
| Excessive Subdomains | `ScamDetector` | Flags 3+ subdomain levels |
| HTTP + Financial Keywords | `ScamDetector` | Insecure banking/payment links |
| Context Pattern Matching | `ScamDetector.scanMessage()` | OTP sharing, prize claims, etc. |

---

## ⚡ Threat Levels & Actions

| Level | Condition | Action | UI |
|-------|-----------|--------|----|
| 🚫 HIGH | Phishing / typosquatting / homograph / IP | `BLOCK` | Red blocking dialog, link removed |
| ⚠️ MEDIUM | Suspicious TLD / excessive subdomains / HTTP+finance | `WARN` | Orange warning dialog |
| ℹ️ LOW | URL shortener | `CAUTION` | Yellow caution info dialog |
| ✅ SAFE | Whitelisted / no threats | `ALLOW` | Green checkmark |

---

## 🔧 Integration Guide

### 1. Scan Incoming Messages

```kotlin
// In your ChatActivity / MessageAdapter
ScamProtectionManager.processIncomingMessage(
    context = context,
    rawMessage = message.text,
    onSafe = { safeText ->
        displayMessage(safeText)
    },
    onThreat = { threats, sanitizedText ->
        displayMessage(sanitizedText)  // dangerous links already replaced
        ScamWarningDialog.show(context, threats.first())
    },
    onSpam = { score ->
        ScamWarningDialog.showSpamBanner(context, score,
            onMoveToSpam = { SpamFolder.moveToSpam(userId, messageId, spamMessage) }
        )
    }
)
```

### 2. Handle Link Taps

```kotlin
// In your message click listener (replace setOnClickListener for links)
ScamProtectionManager.handleLinkTap(
    context = context,
    url = clickedUrl,
    onOpenUrl = { url ->
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
)
```

### 3. Sync Community Blocklist (on app start)

```kotlin
// In Application.onCreate() or MainActivity.onStart()
ScamProtectionManager.syncCommunityBlocklist()
```

### 4. Register Activity in AndroidManifest.xml

```xml
<activity
    android:name=".security.ScamProtectionSettingsActivity"
    android:label="Scam Protection Settings"
    android:exported="false" />
```

### 5. Firebase Database Rules (add to your rules.json)

```json
{
  "rules": {
    "chatrivo_security": {
      "blocklist": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

---

## 🧪 Detection Examples

```
❌ http://paypa1.com/verify         → HIGH  (Typosquatting: 'paypa1' ≈ 'paypal.com', edit distance 1)
❌ http://winner.tk/claim           → HIGH  (Suspicious TLD .tk + spam keywords 'winner', 'claim')
❌ http://192.168.1.1/login        → HIGH  (Direct IP address)
⚠️ http://secure.verify.bank.xyz   → MEDIUM (Suspicious TLD .xyz + 3 subdomain levels)
⚠️ http://sbibank-verify.com       → HIGH  (Known phishing domain)
ℹ️ https://bit.ly/abc123           → LOW   (URL shortener)
✅ https://www.google.com           → SAFE  (Whitelisted)
✅ https://sbi.co.in/login         → SAFE  (Whitelisted)
```

---

## 📊 Spam Scoring Examples

| Message | Score | Result |
|---------|-------|--------|
| "You have won a free iPhone! Click to claim!" | 60 | 🔴 SPAM |
| "Share your OTP to verify your KYC" | 70 | 🔴 SPAM |
| "Limited time offer on Flipkart" | 13 | 🟡 Low |
| "Hey, are you free tomorrow?" | 0 | 🟢 Safe |

---

## 🏗️ Architecture

```
Incoming Message
      │
      ▼
ScamProtectionManager.processIncomingMessage()
      │
      ├── ScamDetector.scanMessage()
      │       ├── extractUrls()  →  ScamDetector.scanUrl() × N
      │       │       ├── knownPhishingDomains check
      │       │       ├── isDirectIp()
      │       │       ├── suspiciousTlds check
      │       │       ├── urlShorteners check
      │       │       ├── detectTyposquatting() [Levenshtein]
      │       │       ├── normalizeHomographs()
      │       │       ├── subdomains count
      │       │       └── HTTP + financial keywords
      │       └── calculateSpamScore()
      │
      ├── isSpam? → SpamWarningDialog → SpamFolder
      ├── hasThreat? → ScamWarningDialog (HIGH/MEDIUM/LOW)
      └── isSafe? → display message normally
```

---

## 📋 Version History

| Version | Feature |
|---------|---------|
| 4.0.0 | Sarvam AI, Weather, News |
| 4.1.0 | Scam Protection & Fraud Detection |
