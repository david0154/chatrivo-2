# Chatrivo — Smart Indian Messaging App

> Formerly known as **SuperChat**. Renamed to **Chatrivo** in v4.0.

[![Version](https://img.shields.io/badge/version-4.0.0-blue)]()
[![Platform](https://img.shields.io/badge/platform-Android-green)]()
[![Language](https://img.shields.io/badge/language-Kotlin%20%7C%20Java-orange)]()
[![AI](https://img.shields.io/badge/AI-Sarvam%20AI-purple)]()
[![Weather](https://img.shields.io/badge/Weather-Open--Meteo-blue)]()

---

## 🚀 Features

### Core Messaging
- 💬 One-on-one & group chats with end-to-end encryption (AES)
- 📸 Share photos, videos, documents, voice messages
- 🔔 Push notifications via Firebase Cloud Messaging
- 📍 Location sharing with Google Maps
- 📖 Status / Stories with progress view
- 📹 Video & audio calls via Agora RTC
- 🔒 Biometric lock
- 🎨 Image editing engine

### 🤖 Chatrivo AI (NEW in v4.0)
- **Sarvam AI Chat** — Intelligent conversation in 10 Indian languages
- **Real-time Weather** — Ask weather in any major Indian city (Open-Meteo, no API key needed)
- **Indian News Bot** — Latest news from NDTV, Times of India, The Hindu, Aaj Tak
- **Multi-language Translation** — Translate text across Hindi, Bengali, Tamil, Telugu and more (Sarvam Mayura)
- **Voice-to-Text** — Transcribe voice messages in Indian languages (Sarvam Saarika)
- **Text-to-Speech** — AI reads replies aloud in Indian voices (Sarvam Bulbul)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin + Java |
| UI | XML Layouts, ViewBinding, Material Design |
| Backend | Firebase (Auth, Realtime DB, Storage, FCM, Functions) |
| AI | Sarvam AI (sarvam-m, bulbul:v1, saarika:v2, mayura:v1) |
| Weather | Open-Meteo API (free, no key) |
| News | RSS feeds (NDTV, TOI, The Hindu) |
| Video Calls | Agora RTC SDK |
| Encryption | AES, Virgil E3Kit (E2E option) |
| Ads | Google AdMob |

---

## 📲 Setup

1. Clone the repo
   ```bash
   git clone https://github.com/david0154/chatrivo-2.git
   ```
2. Open `SuperChat_Code/` in Android Studio
3. Add your `google-services.json` to `SuperChat_Code/app/`
4. In `SuperChat_Code/app/build.gradle`:
   ```groovy
   resValue 'string', "app_name", "Chatrivo"
   resValue 'string', "sarvam_api_key", "YOUR_SARVAM_KEY"
   resValue 'string', "agora_app_id", "YOUR_AGORA_ID"
   resValue 'string', "maps_api_key", "YOUR_MAPS_KEY"
   ```
5. Build & run:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📁 Project Structure

```
chatrivo-2/
├── SuperChat_Code/           ← Android Studio project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/.../ai/  ← NEW: Sarvam AI, Weather, News modules
│   │   │   ├── res/layout/   ← NEW: AI chat layouts
│   │   │   └── ...
│   │   └── build.gradle
│   └── ...
├── Backend - Cloud Functions/ ← Firebase Cloud Functions
├── Documentation/
│   └── CHATRIVO_AI_UPGRADE_v4.md
└── README.md
```

---

## 🤖 AI Architecture

```
User Message
    │
    ▼
ChatrivoAIBot (Intent Router)
    ├── Weather query  → Open-Meteo → Sarvam AI reply
    ├── News query     → RSS Feed   → Sarvam AI summary
    ├── Translate      → Sarvam Mayura
    └── General chat   → Sarvam-M
```

---

## 📄 Documentation

See [`Documentation/CHATRIVO_AI_UPGRADE_v4.md`](Documentation/CHATRIVO_AI_UPGRADE_v4.md) for full setup and API reference.

---

## 📜 License

This project is for personal/commercial use by David. All rights reserved.

---

*Built with ❤️ in Kolkata, India*
