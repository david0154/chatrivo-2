/**
 * Chatrivo - Firebase Cloud Functions v2
 * Company : Nexuzy
 * Package : com.nexuzy.chatrivo
 *
 * Functions:
 *  1. sendChatNotification   - FCM push for 1-to-1 / group messages
 *  2. sendCallNotification   - FCM push for incoming voice / video calls
 *  3. onUserDelete           - Clean up Firestore data when a user is deleted
 *  4. onUserStatusChange     - Mirror online/offline to a public "presence" doc
 *  5. deleteOldNotifications - Scheduled: purge notifications older than 30 days
 */

"use strict";

const { onValueCreated } = require("firebase-functions/v2/database");
const { onDocumentCreated, onDocumentDeleted } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const rtdb = admin.database();

// ── Deploy all functions to asia-south1 (Mumbai) for India users ─────────────
setGlobalOptions({ region: "asia-south1", maxInstances: 10 });

// ─────────────────────────────────────────────────────────────────────────────
// 1. SEND CHAT NOTIFICATION
//    Triggered when a new message is written to:
//    /Messages/{chatId}/Messages/{messageId}
// ─────────────────────────────────────────────────────────────────────────────
exports.sendChatNotification = onDocumentCreated(
    "Messages/{chatId}/Messages/{messageId}",
    async (event) => {
      const message = event.data.data();
      if (!message) return null;

      const senderId = message.senderId || message.from;
      const receiverId = message.receiverId || message.to;
      const chatId = event.params.chatId;

      if (!senderId || !receiverId) return null;

      try {
        // Get sender info
        const senderSnap = await db.collection("Users").doc(senderId).get();
        const sender = senderSnap.data();
        if (!sender) return null;

        // Get receiver FCM tokens (supports multiple devices)
        const receiverSnap = await db.collection("Users").doc(receiverId).get();
        const receiver = receiverSnap.data();
        if (!receiver) return null;

        // Check if receiver has muted this chat
        const mutedRef = db
            .collection("Users")
            .doc(receiverId)
            .collection("MutedChats")
            .doc(chatId);
        const mutedSnap = await mutedRef.get();
        if (mutedSnap.exists) return null;

        const tokens = getTokens(receiver);
        if (tokens.length === 0) return null;

        const senderName = sender.name || "Chatrivo";
        const body = buildMessageBody(message);

        const payload = {
          notification: {
            title: senderName,
            body: body,
          },
          data: {
            type: "chat",
            chatId: chatId,
            senderId: senderId,
            senderName: senderName,
            senderImage: sender.image || "",
            click_action: "OPEN_CHAT_ACTIVITY",
          },
          android: {
            priority: "high",
            notification: {
              channelId: "chatrivo_messages",
              sound: "default",
              priority: "high",
              tag: chatId,
            },
          },
        };

        return sendToTokens(tokens, payload);
      } catch (err) {
        console.error("sendChatNotification error:", err);
        return null;
      }
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 2. SEND CALL NOTIFICATION
//    Triggered when a call document is created:
//    /Calls/{callId}
// ─────────────────────────────────────────────────────────────────────────────
exports.sendCallNotification = onDocumentCreated(
    "Calls/{callId}",
    async (event) => {
      const call = event.data.data();
      if (!call) return null;

      const callerId = call.callerId;
      const receiverId = call.receiverId;
      const callType = call.type || "voice"; // "voice" | "video"

      if (!callerId || !receiverId) return null;

      try {
        const callerSnap = await db.collection("Users").doc(callerId).get();
        const caller = callerSnap.data();
        if (!caller) return null;

        const receiverSnap = await db.collection("Users").doc(receiverId).get();
        const receiver = receiverSnap.data();
        if (!receiver) return null;

        const tokens = getTokens(receiver);
        if (tokens.length === 0) return null;

        const callerName = caller.name || "Chatrivo";
        const callLabel = callType === "video" ? "Incoming Video Call" : "Incoming Voice Call";

        const payload = {
          notification: {
            title: callerName,
            body: callLabel,
          },
          data: {
            type: "call",
            callId: event.params.callId,
            callType: callType,
            callerId: callerId,
            callerName: callerName,
            callerImage: caller.image || "",
            click_action: "OPEN_CALL_ACTIVITY",
          },
          android: {
            priority: "high",
            notification: {
              channelId: "chatrivo_calls",
              sound: "ringtone",
              priority: "max",
              tag: "call_" + callerId,
            },
          },
        };

        return sendToTokens(tokens, payload);
      } catch (err) {
        console.error("sendCallNotification error:", err);
        return null;
      }
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 3. ON USER DELETE  (Auth trigger via callable — call from admin panel)
//    Deletes: Firestore user doc, RTDB presence, Storage profile photo
// ─────────────────────────────────────────────────────────────────────────────
exports.deleteUserData = onCall(async (request) => {
  // Only allow authenticated users with admin role
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be authenticated.");
  }

  const callerSnap = await db.collection("Users").doc(request.auth.uid).get();
  const caller = callerSnap.data();
  if (!caller || caller.role !== "admin") {
    throw new HttpsError("permission-denied", "Admins only.");
  }

  const targetUid = request.data.uid;
  if (!targetUid) throw new HttpsError("invalid-argument", "uid required");

  try {
    // Delete Firestore user doc
    await db.collection("Users").doc(targetUid).delete();

    // Delete RTDB presence
    await rtdb.ref("presence/" + targetUid).remove();

    // Delete Auth user
    await admin.auth().deleteUser(targetUid);

    console.log(`Deleted user: ${targetUid}`);
    return { success: true };
  } catch (err) {
    console.error("deleteUserData error:", err);
    throw new HttpsError("internal", err.message);
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// 4. ON USER STATUS CHANGE  (RTDB presence → Firestore mirror)
//    RTDB path: /presence/{userId}  { online: bool, lastSeen: timestamp }
// ─────────────────────────────────────────────────────────────────────────────
exports.onUserStatusChange = onValueCreated(
    { ref: "/presence/{userId}", instance: "(default)" },
    async (event) => {
      const userId = event.params.userId;
      const status = event.data.val();
      if (!status) return null;

      try {
        await db.collection("Users").doc(userId).update({
          online: status.online || false,
          lastSeen: status.lastSeen
              ? admin.firestore.Timestamp.fromMillis(status.lastSeen)
              : admin.firestore.FieldValue.serverTimestamp(),
        });
      } catch (err) {
        console.error("onUserStatusChange error:", err);
      }
      return null;
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 5. SCHEDULED: DELETE OLD NOTIFICATIONS
//    Runs every day at 2 AM IST — removes notifications older than 30 days
// ─────────────────────────────────────────────────────────────────────────────
exports.deleteOldNotifications = onSchedule(
    { schedule: "0 20 * * *", timeZone: "Asia/Kolkata" }, // 8 PM UTC = 2 AM IST
    async () => {
      const cutoff = admin.firestore.Timestamp.fromDate(
          new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
      );

      const snap = await db
          .collectionGroup("Notifications")
          .where("timestamp", "<", cutoff)
          .limit(500)
          .get();

      if (snap.empty) {
        console.log("No old notifications to delete.");
        return;
      }

      const batch = db.batch();
      snap.docs.forEach((doc) => batch.delete(doc.ref));
      await batch.commit();
      console.log(`Deleted ${snap.size} old notifications.`);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Extract all valid FCM tokens from a user doc.
 * Supports both single `token` field and `tokens` map.
 */
function getTokens(userDoc) {
  const tokens = [];
  if (userDoc.token && typeof userDoc.token === "string") {
    tokens.push(userDoc.token);
  }
  if (userDoc.tokens && typeof userDoc.tokens === "object") {
    Object.values(userDoc.tokens).forEach((t) => {
      if (t && typeof t === "string") tokens.push(t);
    });
  }
  return [...new Set(tokens)]; // deduplicate
}

/**
 * Build a human-readable notification body from a message doc.
 */
function buildMessageBody(message) {
  const type = message.type || "text";
  switch (type) {
    case "image": return "📷 Photo";
    case "video": return "🎥 Video";
    case "audio": return "🎵 Voice message";
    case "file": return "📎 File";
    case "location": return "📍 Location";
    case "contact": return "👤 Contact";
    case "sticker": return "😄 Sticker";
    case "gif": return "🎞️ GIF";
    default:
      return message.message || message.text || message.body || "New message";
  }
}

/**
 * Send FCM to a list of tokens. Automatically removes invalid tokens
 * from the user's Firestore doc.
 */
async function sendToTokens(tokens, payload) {
  const results = await Promise.allSettled(
      tokens.map((token) =>
        admin.messaging().send({ ...payload, token })
      )
  );

  results.forEach((result, i) => {
    if (result.status === "rejected") {
      console.warn(`FCM failed for token[${i}]:`, result.reason?.message);
    }
  });

  return results;
}
