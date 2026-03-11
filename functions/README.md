# Chatrivo - Firebase Cloud Functions

> Node.js 20 · Firebase Functions v2 · Region: `asia-south1` (Mumbai)

## Functions

| Function | Trigger | What it does |
|---|---|---|
| `sendChatNotification` | Firestore `Messages/{chatId}/Messages/{messageId}` created | Sends FCM push to receiver for new messages |
| `sendCallNotification` | Firestore `Calls/{callId}` created | Sends FCM push for incoming voice/video call |
| `deleteUserData` | Callable HTTPS | Admin-only: delete user from Auth + Firestore + RTDB |
| `onUserStatusChange` | RTDB `/presence/{userId}` write | Mirrors online status to Firestore |
| `deleteOldNotifications` | Scheduled (daily 2 AM IST) | Deletes notifications older than 30 days |

## Setup & Deploy

```bash
# 1. Install dependencies
cd functions
npm install

# 2. Login to Firebase
firebase login

# 3. Set your Firebase project
firebase use --add   # select your project

# 4. Deploy all functions
firebase deploy --only functions

# 5. Deploy single function
firebase deploy --only functions:sendChatNotification
```

## Firestore Structure Expected

```
Users/{userId}
  name: string
  image: string (URL)
  token: string  (FCM token — single device)
  tokens: map    (FCM tokens — multi device)
  online: bool
  lastSeen: timestamp
  role: string   ("admin" | "user")

Messages/{chatId}/Messages/{messageId}
  senderId: string
  receiverId: string
  type: string   ("text" | "image" | "video" | "audio" | "file" | "location" | "contact" | "sticker")
  message: string
  timestamp: timestamp

Calls/{callId}
  callerId: string
  receiverId: string
  type: string   ("voice" | "video")
  status: string ("ringing" | "accepted" | "declined" | "missed")
  timestamp: timestamp
```

## Android Notification Channels

Make sure these channels are created in your `ChatrivoApp.kt`:

| Channel ID | Usage |
|---|---|
| `chatrivo_messages` | Chat message notifications |
| `chatrivo_calls` | Call notifications (max priority) |
