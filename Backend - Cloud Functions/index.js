/**
 * Chatrivo - Firebase Cloud Functions
 * Company  : Nexuzy
 * Package  : com.nexuzy.chatrivo
 * SDK      : firebase-functions v5 (Gen 2) + firebase-admin v12
 * Node     : 20
 * Region   : asia-south1 (Mumbai) — best latency for India users
 *
 * Functions:
 *  1.  participantRemoved          - RTDB: handle group member removal / exit
 *  2.  groupAdminChanged           - RTDB: handle admin add/remove events
 *  3.  deleteMessageForGroup       - RTDB: delete a group message for everyone
 *  4.  resubscribeUserToBroadcasts - RTDB: re-subscribe user to broadcasts on new token
 *  5.  sendMessageToBroadcast      - RTDB: fan-out broadcast message + FCM
 *  6.  unsubscribeUserFromBroadcast- RTDB: unsubscribe user when removed from broadcast
 *  7.  sendUnDeliveredNotifications- HTTPS callable: flush undelivered messages on app open
 *  8.  sendNewCallNotification     - RTDB: FCM push for incoming calls
 *  9.  indexNewCall                - RTDB: log 1-to-1 call
 *  10. indexNewGroupCall           - RTDB: log group call
 *  11. indexPKToken                - RTDB: PushKit token indexing stub
 */

"use strict";

const { onValueCreated, onValueUpdated, onValueDeleted } = require("firebase-functions/v2/database");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
const sizeof = require("object-sizeof");

admin.initializeApp();

// ── Deploy everything to Mumbai for India users ───────────────────────────────
setGlobalOptions({ region: "asia-south1", maxInstances: 10 });

// ── Constants ─────────────────────────────────────────────────────────────────
const APP_NAME     = "Chatrivo";
const PACKAGE_NAME = "com.nexuzy.chatrivo";
const MAX_FCM_BYTES = 10000;
const MSG_DELETE_WINDOW_MIN = 15; // minutes

// Message types (must match Android constants)
const TYPE_TEXT     = 1;
const TYPE_IMAGE    = 2;
const TYPE_VIDEO    = 5;
const TYPE_VOICE    = 11;
const TYPE_AUDIO    = 9;
const TYPE_FILE     = 13;
const TYPE_CONTACT  = 16;
const TYPE_LOCATION = 18;

// Group event types
const EV_ADMIN_ADDED    = 1;
const EV_USER_ADDED     = 2;
const EV_USER_REMOVED   = 3;
const EV_USER_LEFT      = 4;
const EV_SETTINGS       = 5;
const EV_GROUP_CREATED  = 6;
const EV_ADMIN_REMOVED  = 7;
const EV_JOINED_LINK    = 8;

const db  = () => admin.database();
const ref = (path) => db().ref(path);

// ─────────────────────────────────────────────────────────────────────────────
// 1. PARTICIPANT REMOVED
// ─────────────────────────────────────────────────────────────────────────────
exports.participantRemoved = onValueDeleted(
    { ref: "/groups/{groupId}/users/{userId}", instance: "(default)" },
    async (event) => {
      const { groupId, userId } = event.params;
      const deletedByUid = event.auth?.uid ?? userId;

      const [userPhoneSnap, removedByPhoneSnap] = await Promise.all([
        ref(`/users/${userId}/phone`).once("value"),
        ref(`/users/${deletedByUid}/phone`).once("value"),
      ]);

      const userPhone      = userPhoneSnap.val();
      const removedByPhone = removedByPhoneSnap.val();
      const time           = Date.now();

      const isSelfExit = userId === deletedByUid;
      const eventType  = isSelfExit ? EV_USER_LEFT : EV_USER_REMOVED;

      const groupEvent = {
        contextStart: isSelfExit ? `${userPhone}` : `${removedByPhone}`,
        eventType,
        contextEnd: isSelfExit ? "null" : `${userPhone}`,
        timestamp: `${time}`,
      };

      if (!isSelfExit) {
        await ref(`/groupsDeletedUsers/${groupId}/${userId}`).set(true);
      }

      await ref(`/groupEvents/${groupId}`).push().set(groupEvent);

      // If removed user was admin, maybe assign a new one
      if (event.data.val() === true) {
        const usersSnap = await ref(`/groups/${groupId}/users/`).once("value");
        if (usersSnap.val() && !isThereAdmin(usersSnap.val())) {
          const usersArray = Object.keys(usersSnap.val());
          const newAdmin   = usersArray[Math.floor(Math.random() * usersArray.length)];
          await ref(`/groups/${groupId}/users/${newAdmin}`).set(true);
        }
      }

      return ref(`/groupsByUser/${userId}/${groupId}`).remove();
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 2. GROUP ADMIN CHANGED
// ─────────────────────────────────────────────────────────────────────────────
exports.groupAdminChanged = onValueUpdated(
    { ref: "/groups/{groupId}/users/{userId}", instance: "(default)" },
    async (event) => {
      const { groupId, userId } = event.params;
      const addedByUid  = event.auth?.uid;
      const isNowAdmin  = event.data.after.val();

      const userPhoneSnap = await ref(`/users/${userId}/phone`).once("value");
      const userPhone     = userPhoneSnap.val();
      const timestamp     = Date.now();

      if (!addedByUid) {
        // Set by Cloud Functions (no auth) — random admin assignment
        return ref(`/groupEvents/${groupId}/`).push().set({
          contextStart: "null",
          eventType: EV_ADMIN_ADDED,
          contextEnd: `${userPhone}`,
          timestamp: `${timestamp}`,
        });
      }

      const addedBySnap  = await ref(`/users/${addedByUid}/phone`).once("value");
      const addedByPhone = addedBySnap.val();

      return ref(`/groupEvents/${groupId}/`).push().set({
        contextStart: `${addedByPhone}`,
        eventType: isNowAdmin ? EV_ADMIN_ADDED : EV_ADMIN_REMOVED,
        contextEnd: `${userPhone}`,
        timestamp: `${timestamp}`,
      });
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 3. DELETE MESSAGE FOR GROUP
// ─────────────────────────────────────────────────────────────────────────────
exports.deleteMessageForGroup = onValueCreated(
    { ref: "/deleteMessageRequestsForGroup/{groupId}/{messageId}", instance: "(default)" },
    async (event) => {
      const { groupId, messageId } = event.params;
      const messageAuthorUid = event.auth?.uid;

      const msgSnap = await ref(`/groupsMessages/${groupId}/${messageId}`).once("value");
      const message = msgSnap.val();
      if (!message) return null;

      if (timePassed(message.timestamp)) return null;

      const usersSnap = await ref(`groups/${groupId}/users`).once("value");
      const users     = Object.keys(usersSnap.val() || {});

      const deletedMessage = { messageId, groupId, isGroup: true, isBroadcast: false };
      const saves = users
          .filter((uid) => uid !== messageAuthorUid)
          .map((uid) => ref(`deletedMessages/${uid}/${messageId}`).set(deletedMessage));

      await Promise.all(saves);

      const payload = buildDeletePayload(messageId);
      payload.condition = `'${groupId}' in topics && !'${messageAuthorUid}' in topics`;
      return admin.messaging().send(payload);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 4. RESUBSCRIBE USER TO BROADCASTS (new FCM token)
// ─────────────────────────────────────────────────────────────────────────────
exports.resubscribeUserToBroadcasts = onValueCreated(
    { ref: "/users/{uid}/notificationTokens/{token}", instance: "(default)" },
    async (event) => {
      const { uid, token } = event.params;
      const snap = await ref(`broadcastsByUser/${uid}`).once("value");
      const promises = [];
      snap.forEach((child) => {
        if (!child.val()) {
          promises.push(admin.messaging().subscribeToTopic(token, child.key));
        }
      });
      return Promise.all(promises);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 5. SEND MESSAGE TO BROADCAST
// ─────────────────────────────────────────────────────────────────────────────
exports.sendMessageToBroadcast = onValueCreated(
    { ref: "/broadcastsMessages/{broadcastId}/{messageId}", instance: "(default)" },
    async (event) => {
      const { broadcastId, messageId } = event.params;
      const val    = event.data.val();
      const fromId = val.fromId;
      const toId   = val.toId;

      const [senderSnap, blockedSnap] = await Promise.all([
        ref(`users/${fromId}/phone`).once("value"),
        ref(`blockedUsers/${toId}/${fromId}/`).once("value"),
      ]);

      if (blockedSnap.exists()) return null;

      const senderPhone = senderSnap.val();
      const payload     = buildMessagePayload(val, senderPhone, fromId);
      payload.topic     = broadcastId;

      const usersSnap = await ref(`broadcasts/${broadcastId}/users`).once("value");
      const users     = Object.keys(usersSnap.val() || {});

      const saves = users
          .filter((uid) => uid !== fromId)
          .map((uid) => ref(`userMessages/${uid}/${messageId}`).set(payload.data));

      await Promise.all(saves);
      payload.data = trimPayload(payload.data);
      return admin.messaging().send(payload);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 6. UNSUBSCRIBE USER FROM BROADCAST
// ─────────────────────────────────────────────────────────────────────────────
exports.unsubscribeUserFromBroadcast = onValueDeleted(
    { ref: "/broadcasts/{broadcastId}/users/{userId}", instance: "(default)" },
    async (event) => {
      const { broadcastId, userId } = event.params;
      const snap   = await ref(`users/${userId}/notificationTokens/`).once("value");
      const tokens = Object.keys(snap.val() || {});
      await ref(`broadcastsByUser/${userId}/${broadcastId}`).remove();
      return admin.messaging().unsubscribeFromTopic(tokens, broadcastId);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 7. SEND UNDELIVERED NOTIFICATIONS  (callable — called on app open)
// ─────────────────────────────────────────────────────────────────────────────
exports.sendUnDeliveredNotifications = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Login required.");
  const uid = request.auth.uid;

  // Optimistic lock to prevent duplicate delivery
  const lockRef = ref(`sendUnDeliveredNotificationsLock/${uid}`);
  const lockSnap = await lockRef.once("value");
  if (lockSnap.exists()) return null;
  await lockRef.set(true);

  try {
    const [userMsgs, deletedMsgs, newGroups, tokensSnap] = await Promise.all([
      ref(`userMessages/${uid}`).once("value"),
      ref(`deletedMessages/${uid}`).once("value"),
      ref(`newGroups/${uid}`).once("value"),
      ref(`users/${uid}/notificationTokens`).once("value"),
    ]);

    const tokens = Object.keys(tokensSnap.val() || {});
    if (tokens.length === 0) return null;

    const sends = [];

    userMsgs.forEach((snap) => {
      const msg     = snap.val();
      const payload = buildMessagePayload(msg, msg.phone, msg.fromId);
      payload.tokens = tokens;
      payload.data   = trimPayload(payload.data);
      sends.push(admin.messaging().sendEachForMulticast(payload));
    });

    deletedMsgs.forEach((snap) => {
      const payload  = buildDeletePayload(snap.val().messageId);
      payload.tokens = tokens;
      sends.push(admin.messaging().sendEachForMulticast(payload));
    });

    newGroups.forEach((snap) => {
      const { groupId, groupName } = snap.val();
      const payload  = buildNewGroupPayload(groupId, groupName);
      payload.tokens = tokens;
      sends.push(admin.messaging().sendEachForMulticast(payload));
    });

    await Promise.all(sends);
  } catch (err) {
    console.error("sendUnDeliveredNotifications error:", err);
  } finally {
    await lockRef.remove();
  }
  return null;
});

// ─────────────────────────────────────────────────────────────────────────────
// 8. SEND NEW CALL NOTIFICATION
// ─────────────────────────────────────────────────────────────────────────────
exports.sendNewCallNotification = onValueCreated(
    { ref: "/newCalls/{toId}/{fromId}/{callId}", instance: "(default)" },
    async (event) => {
      const { toId, fromId, callId } = event.params;
      const call = event.data.val();

      const [callerSnap, receiverSnap] = await Promise.all([
        ref(`users/${fromId}`).once("value"),
        ref(`users/${toId}`).once("value"),
      ]);

      const caller   = callerSnap.val();
      const receiver = receiverSnap.val();
      if (!caller || !receiver) return null;

      const tokens = getTokensList(receiver);
      if (tokens.length === 0) return null;

      const callType = call.callType || "voice";
      const label    = callType === "video" ? "Incoming Video Call" : "Incoming Voice Call";

      const payload = {
        notification: { title: caller.name || APP_NAME, body: label },
        data: {
          event: "new_call",
          callId,
          fromId,
          toId,
          callType,
          timestamp: `${call.timestamp || Date.now()}`,
          channel: call.channel || "",
        },
        android: {
          priority: "high",
          notification: {
            channelId: "chatrivo_calls",
            sound: "ringtone",
            priority: "max",
          },
        },
        tokens,
      };

      return admin.messaging().sendEachForMulticast(payload);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 9. INDEX NEW CALL  (1-to-1)
// ─────────────────────────────────────────────────────────────────────────────
exports.indexNewCall = onValueCreated(
    { ref: "/newCalls/{toId}/{fromId}/{callId}", instance: "(default)" },
    async (event) => {
      const { toId, fromId, callId } = event.params;
      const call = event.data.val();
      // Mirror to userCalls for both participants
      return Promise.all([
        ref(`userCalls/${fromId}/${callId}`).set(call),
        ref(`userCalls/${toId}/${callId}`).set(call),
      ]);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 10. INDEX NEW GROUP CALL
// ─────────────────────────────────────────────────────────────────────────────
exports.indexNewGroupCall = onValueCreated(
    { ref: "/groupCalls/{groupId}/{callId}", instance: "(default)" },
    async (event) => {
      const { groupId, callId } = event.params;
      const call      = event.data.val();
      const usersSnap = await ref(`groups/${groupId}/users`).once("value");
      const users     = Object.keys(usersSnap.val() || {});
      const saves     = users.map((uid) => ref(`userCalls/${uid}/${callId}`).set(call));
      return Promise.all(saves);
    }
);

// ─────────────────────────────────────────────────────────────────────────────
// 11. INDEX PK TOKEN  (PushKit stub — Android unused, keep for iOS future)
// ─────────────────────────────────────────────────────────────────────────────
exports.indexPKToken = onValueCreated(
    { ref: "/users/{userId}/pktoken/{token}", instance: "(default)" },
    async () => null // stub
);

// ─────────────────────────────────────────────────────────────────────────────
// HELPER FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────────

/** Build FCM payload for a chat/broadcast message */
function buildMessagePayload(msg, senderPhone, fromId) {
  const type = msg.type || TYPE_TEXT;
  const body = messageBody(type, msg);

  return {
    notification: {
      title: senderPhone || APP_NAME,
      body,
    },
    data: cleanData({
      event:     "new_message",
      type:      `${type}`,
      messageId: `${msg.messageId || ""}`,
      fromId:    `${fromId || ""}`,
      toId:      `${msg.toId || ""}`,
      isGroup:   `${msg.isGroup || false}`,
      content:   type === TYPE_TEXT ? (msg.content || msg.message || "") : "",
      thumb:     msg.thumb || "",
      timestamp: `${msg.timestamp || Date.now()}`,
      phone:     `${senderPhone || ""}`,
    }),
    android: {
      priority: "high",
      notification: {
        channelId: "chatrivo_messages",
        sound: "default",
      },
    },
  };
}

/** Build FCM payload for a deleted message event */
function buildDeletePayload(messageId) {
  return {
    data: { event: "message_deleted", messageId: `${messageId}` },
    android: { priority: "high" },
  };
}

/** Build FCM payload for being added to a new group */
function buildNewGroupPayload(groupId, groupName) {
  return {
    notification: { title: APP_NAME, body: `You were added to ${groupName}` },
    data: { event: "new_group", groupId: `${groupId}`, groupName: `${groupName}` },
    android: { priority: "high" },
  };
}

/** Human-readable body text per message type */
function messageBody(type, msg) {
  switch (type) {
    case TYPE_IMAGE:    return "\uD83D\uDCF7 Photo";
    case TYPE_VIDEO:    return "\uD83C\uDFA5 Video";
    case TYPE_VOICE:    return "\uD83C\uDFB5 Voice message";
    case TYPE_AUDIO:    return "\uD83C\uDFB5 Audio";
    case TYPE_FILE:     return "\uD83D\uDCCE File";
    case TYPE_CONTACT:  return "\uD83D\uDC64 Contact";
    case TYPE_LOCATION: return "\uD83D\uDCCD Location";
    default:            return msg.content || msg.message || "New message";
  }
}

/** Extract all FCM tokens from a user RTDB node */
function getTokensList(userNode) {
  const tokens = [];
  const obj = userNode.notificationTokens || {};
  Object.keys(obj).forEach((t) => {
    if (t && typeof t === "string") tokens.push(t);
  });
  return [...new Set(tokens)];
}

/** Remove undefined / null values from FCM data object */
function cleanData(obj) {
  const out = {};
  for (const k in obj) {
    if (obj[k] !== undefined && obj[k] !== null) out[k] = obj[k];
  }
  return out;
}

/** Trim payload if it exceeds FCM size limit */
function trimPayload(data) {
  if (sizeof(data) > MAX_FCM_BYTES) {
    data.content = "";
    data.thumb   = "";
  }
  return data;
}

/** Check if there is at least one admin in a group users object */
function isThereAdmin(users) {
  return Object.values(users).some((v) => v === true);
}

/** Return true if the message delete window has expired */
function timePassed(timestamp) {
  return Math.floor((Date.now() - timestamp) / 60000) > MSG_DELETE_WINDOW_MIN;
}
