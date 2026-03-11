const functions = require('firebase-functions');
const admin = require('firebase-admin');
const sizeof = require('object-sizeof');
const {generateVirgilJwt} =  require( './generate-virgil-jwt');

const iosEnabled = false


admin.initializeApp()

//message types
const SENT_TEXT = 1;
const SENT_IMAGE = 2;
const SENT_VIDEO = 5;
const SENT_VOICE_MESSAGE = 11;
const SENT_AUDIO = 9;
const SENT_FILE = 13;
const SENT_CONTACT = 16;
const SENT_LOCATION = 18;

//group events types
const ADMIN_ADDED = 1;
const USER_ADDED = 2;
const USER_REMOVED_BY_ADMIN = 3;
const USER_LEFT_GROUP = 4;
const GROUP_SETTINGS_CHANGED = 5;
const GROUP_CREATION = 6;
const ADMIN_REMOVED = 7;
const JOINED_VIA_LINK = 8;


const MESSAGE_TIME_LIMIT = 15;

const options = {
  priority: 'high',

  "mutable_content": true
}


const MAX_FCM_LIMIT = 10000

const packageName = "com.teamxdevelopers.superchat"



//this will trigger when a group member removed
exports.participantRemoved = functions.database.ref(`/groups/{groupId}/users/{userId}`).onDelete((snap, context) => {
  const groupId = context.params.groupId
  const userId = context.params.userId
  const deletedByUid = context.auth.uid



  //get removed user phone
  const userPhonePromise = admin.database().ref(`/users/${userId}/phone`).once('value')
  //get phone of the admin who removed this user
  const removedByPhonePromise = admin.database().ref(`/users/${deletedByUid}/phone`).once('value')

  //execute above promises
  return Promise.all([userPhonePromise, removedByPhonePromise]).then((results) => {
    const userPhone = results[0].val()
    const removedByPhone = results[1].val()
    const time = Date.now()



    var contextStart;
    var contextEnd;
    var eventType;
    //if the id is the same id of deletedById,then user exits the group by himself
    if (userId === deletedByUid) {
      eventType = USER_LEFT_GROUP
      contextStart = userPhone
      contextEnd = 'null'
    }
    //otherwise an admin removed this user
    else {
      eventType = USER_REMOVED_BY_ADMIN
      contextStart = removedByPhone
      contextEnd = userPhone
    }

    const event = {
      contextStart: `${contextStart}`,
      eventType: eventType,
      contextEnd: `${contextEnd}`,
      timestamp: `${time}`
    }

    //if user removed by admin then add his id to deleted users of the group
    //this will prevent this user from joining the group again when using Group Invitation Link


    if (eventType === USER_REMOVED_BY_ADMIN) {

      return admin.database().ref(`/groupsDeletedUsers/${groupId}/${userId}`).set(true).then(() => {


        //set group Event in database
        return admin.database().ref(`/groupEvents/${groupId}`).push().set(event).then(() => {


          //if the deleted user is an admin, then check if there are other admins,if not set a new admin randomly
          if (snap.val() === true) {
            return admin.database().ref(`/groups/${groupId}/users/`).once('value').then((snapshot) => {



              //if the group is not exists return null and do nothing
              if (snapshot.val() === null) {
                return null
              }

              const users = snapshot.val()

              //check if there is another admin,if not set a new admin randomly


              //check if there is another admin in group, if not we will generate an admin randomly
              if (!isThereAdmin(users)) {



                //get current users
                const usersArray = Object.keys(snapshot.val())

                //generate a new admin
                const newAdminUid = usersArray[Math.floor(Math.random() * usersArray.length)];




                //set new admin
                return admin.database().ref(`/groups/${groupId}/users/${newAdminUid}`).set(true).then(() => {

                  //remove the user from group 
                  return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove().then(() => {
                    // return admin.messaging().sendToTopic(groupId, payload).then(() => {
                    //   
                    // })
                  })
                })
              }
            })
          }



          //if the removed user is not admin ,just remove him from the group
          return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove()

        })

      })
    }

    //set group Event in database
    return admin.database().ref(`/groupEvents/${groupId}`).push().set(event).then(() => {


      //if the deleted user is an admin, then check if there are other admins,if not set a new admin randomly
      if (snap.val() === true) {
        return admin.database().ref(`/groups/${groupId}/users/`).once('value').then((snapshot) => {



          //if the group is not exists return null and do nothing
          if (snapshot.val() === null) {
            return null
          }

          const users = snapshot.val()

          //check if there is another admin,if not set a new admin randomly


          //check if there is another admin in group, if not we will generate an admin randomly
          if (!isThereAdmin(users)) {



            //get current users
            const usersArray = Object.keys(snapshot.val())

            //generate a new admin
            const newAdminUid = usersArray[Math.floor(Math.random() * usersArray.length)];




            //set new admin
            return admin.database().ref(`/groups/${groupId}/users/${newAdminUid}`).set(true).then(() => {

              //remove the user from group 
              return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove().then(() => {

              })
            })
          }
        })
      }



      //if the removed user is not admin ,just remove him from the group
      return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove()

    })


  })
})

//this will called when an admin changed (removed,or added)
exports.groupAdminChanged = functions.database.ref(`/groups/{groupId}/users/{userId}`).onUpdate((change, context) => {
  const groupId = context.params.groupId
  const userId = context.params.userId
  var addedById = undefined

  //check if the admin was added by another admin or has ben set Randomly by cloud functions
  if (context.auth !== undefined) {
    addedById = context.auth.uid
  }



  //check if admin added or removed
  const isNowAdmin = change.after.val()

  const userPhonePromise = admin.database().ref(`/users/${userId}/phone`).once('value')
  const addedByPhonePromise = admin.database().ref(`/users/${addedById}/phone`).once('value')

  //if there is no admin left in group ,then it will be set by functions,therefore 'addedById' will be undefined
  if (addedById === undefined) {
    return userPhonePromise.then((snap) => {
      const userPhone = snap.val()
      const timestamp = Date.now()
      const event = {
        contextStart: `null`,
        eventType: ADMIN_ADDED,
        contextEnd: `${userPhone}`,
        timestamp: `${timestamp}`
      }



      return admin.database().ref(`/groupEvents/${groupId}/`).push().set(event)
    })
  }

  //otherwise get users phone and set the event
  return Promise.all([userPhonePromise, addedByPhonePromise]).then((results) => {
    const userPhone = results[0].val()
    const addedByPhone = results[1].val()
    var eventType;

    if (isNowAdmin) {
      eventType = ADMIN_ADDED
    }
    else {
      eventType = ADMIN_REMOVED
    }

    const timestamp = Date.now()
    const event = {
      contextStart: `${addedByPhone}`,
      eventType: eventType,
      contextEnd: `${userPhone}`,
      timestamp: `${timestamp}`
    }


    return admin.database().ref(`/groupEvents/${groupId}/`).push().set(event)

  })

})



//this will delete the message for every one in the group
exports.deleteMessageForGroup = functions.database.ref(`deleteMessageRequestsForGroup/{groupId}/{messageId}`).onCreate((snap, context) => {
  const groupId = context.params.groupId
  const messageId = context.params.messageId
  const messageAuthorUid = context.auth.uid

  //get the message
  return admin.database().ref(`/groupsMessages/${groupId}/${messageId}`).once('value').then((results) => {


    const message = results.val()
    const timestamp = message.timestamp

    //check if message time has not passed 
    if (!timePassed(timestamp)) {
      //send delete message to the group


      return admin.database().ref(`groups/${groupId}/users`).once('value').then((usersSnap) => {
        const users = Object.keys(usersSnap.val())
        const messagesToSave = []

        const deletedMessage = {
          messageId: messageId,
          groupId: groupId,
          isGroup: true,
          isBroadcast: false
        }

        users.forEach(uid => {
          //in case if this message was deleted by this user
          if (uid !== messageAuthorUid) {
            messagesToSave.push(admin.database().ref(`deletedMessages/${uid}/${messageId}`).set(deletedMessage))
          }
        });

        const messageNotification = getDeleteMessagePayload(messageId)
        messageNotification.condition = getCondition(groupId, messageAuthorUid)

        return Promise.all(messagesToSave).then(() => {
          return admin.messaging().send(messageNotification)
        })



      })


    }

    return null
  })
})
//this is to resubscribe the user for broadcasts when he re-installs the app(when a new notification token generated)
exports.resubscribeUserToBroadcasts = functions.database.ref(`users/{uid}/notificationTokens/{token}`).onCreate((snap, context) => {
  const uid = context.params.uid
  const token = context.params.token
  return admin.database().ref(`broadcastsByUser/${uid}`).once('value').then((results) => {
    const promises = []
    results.forEach((snapshot) => {
      //add only the broadcasts that are not created by the user,since we don't need to subscribe him to broadcast
      if (!snapshot.val()) {
        promises.push(admin.messaging().subscribeToTopic(token, snapshot.key))
      }


    })

    return Promise.all(promises).then((results) => {

    })
  })
})

exports.sendMessageToBroadcast = functions.database.ref(`broadcastsMessages/{broadcastId}/{messageId}`).onCreate((snap, context) => {
  //get the message object
  const val = snap.val();
  //get fromId field
  const fromId = val.fromId;
  //get toId field
  const toId = val.toId;
  //get messageId
  const messageId = context.params.messageId;
  const broadcastId = context.params.broadcastId;


  //message Details
  const content = val.content;
  const metadata = val.metadata;
  const timestamp = val.timestamp;
  const type = val.type;



  //get user info
  const getSenderInfo = admin.database().ref(`users/${fromId}/phone`).once('value');

  //determine if user is blocked
  const isUserBlocked = admin.database().ref(`blockedUsers/${toId}/${fromId}/`).once('value');

  //Execute the Functions
  return Promise.all([getSenderInfo, isUserBlocked]).then(results => {
    const friendSnapshot = results[0];
    const isBlockedSnapshot = results[1];



    //check if user is blocked,if so do not send the message to him
    if (isBlockedSnapshot.exists()) {
      return
    }

    //get sender phone number
    const senderPhone = friendSnapshot.val();



    //payload contains the data to send it to receiver
    var message = getMessagePayload(type, val, senderPhone, content, timestamp, fromId, toId, undefined, messageId, metadata);
    message.topic = broadcastId

    return admin.database().ref(`broadcasts/${broadcastId}/users`).once('value').then((usersSnap) => {
      const users = Object.keys(usersSnap.val())
      const messagesToSave = []



      users.forEach(uid => {
        //in case if this message was deleted by this user
        if (uid !== fromId) {
          messagesToSave.push(admin.database().ref(`userMessages/${uid}/${messageId}`).set(message.data))
        }
      });


      return Promise.all(messagesToSave).then(() => {
        message.data = removeExtraLimitForPayload(message.data)
        return admin.messaging().send(message)
      })



    })


  });
})

exports.unsubscribeUserFromBroadcast = functions.database.ref(`broadcasts/{broadcastId}/users/{userId}`).onDelete((snap, context) => {
  const userId = context.params.userId
  const broadcastId = context.params.broadcastId


  return admin.database().ref(`users/${userId}/notificationTokens/`).once('value').then((snapshot) => {
    const tokens = Object.keys(snapshot.val());

    return admin.database().ref(`broadcastsByUser/${userId}/${broadcastId}`).remove().then(() => {
      return admin.messaging().unsubscribeFromTopic(tokens, broadcastId)
    })


  })
})







exports.sendUnDeliveredNotifications = functions.https.onCall((data, context) => {
  return admin.database().ref(`sendUnDeliveredNotificationsLock`).once('value').then((lockSnap) => {

    if (lockSnap.exists()) {

      return null
    } else {
      return admin.database().ref(`sendUnDeliveredNotificationsLock`).set(true).then(() => {

        // return new Promise((resolve, reject) => {
        //   setTimeout(function () {
        const uid = context.auth.uid

        const getUserMessages = admin.database().ref(`userMessages/${uid}`).once('value')
        const getDeletedMessages = admin.database().ref(`deletedMessages/${uid}`).once('value')
        const getNewGroups = admin.database().ref(`newGroups/${uid}`).once('value')

        return Promise.all([getUserMessages, getDeletedMessages, getNewGroups]).then((results) => {
          const userMessages = results[0]
          const deletedMessages = results[1]
          const newGroups = results[2]


          const notifications = []



          return admin.database().ref(`users/${uid}/notificationTokens`).once('value').then((tokensSnap) => {

            const tokens = Object.keys(tokensSnap.val())



            userMessages.forEach(messageSnap => {

              const message = messageSnap.val()
              const notification = getMessagePayload(message.type, message, message.phone, message.content, message.timestamp, message.fromId, message.toId, message.isGroup, message.messageId, message.metadata)
              notification.tokens = tokens
              notification.data = removeExtraLimitForPayload(message.data)
              notifications.push(messageSender.sendMulticastMessage(notification, uid))


            });



            deletedMessages.forEach(deletedMessageSnap => {
              const deletedMessage = deletedMessageSnap.val()
              const notification = getDeleteMessagePayload(deletedMessage.messageId)
              notification.tokens = tokens
              notifications.push(messageSender.sendMulticastMessage(notification, uid))

            });


            newGroups.forEach(newGroupSnap => {

              const newGroup = newGroupSnap.val()

              const notification = getNewGroupPayload(newGroup.groupId, newGroup.groupName)
              notification.tokens = tokens
              notifications.push(messageSender.sendMulticastMessage(notification, uid))

            });




            return Promise.all(notifications).then(() => {

              return admin.database().ref(`sendUnDeliveredNotificationsLock`).remove()
            }).catch(() => {
              return admin.database().ref(`sendUnDeliveredNotificationsLock`).remove()
            })
          })

        })
        // .then(resolve, reject);






        // }, 8 * 1000);
        // });



      })
    }
  })


})

exports.sendNewCallNotification = functions.database.ref(`userCalls/{uid}/{callId}`).onCreate((snap, context) => {

})

exports.indexNewCall = functions.database.ref(`newCalls/{toId}/{fromId}/{callId}/`).onCreate((snap, context) => {
})

exports.indexNewGroupCall = functions.database.ref(`groupCalls/{groupId}/{callId}/`).onCreate((snap, context) => {

})





function getDeleteMessagePayload(messageId) {


  var message = {
    data: {
      event: "message_deleted",
      messageId: `${messageId}`
    },
    "android": {
      "priority": "high"
    },

    apns: {
      headers: {
        "apns-topic": `${packageName}`
      },
      payload: {

        aps: {
          alert: {
            title: "Deleted Message",
            body: 'this Message was deleted',

          },
          "mutable-content": 1,
        },
      },
    },
  };


  return message
}

function getDeviceIdChangedPayload(deviceId) {
  var message = {
    data: {
      event: "logout",
      deviceId
    },
    "android": {
      "priority": "high"
    },

    apns: {
      headers: {
        "apns-topic": `${packageName}`
      },
      payload: {

        aps: {
          alert: {
            title: "Logged out",
            body: 'you have logged in on another device',

          },
          "mutable-content": 1,
        },
      },
    },
  };


  return message
}

function getNewGroupPayload(groupId, groupName) {
  const payload = {
    data: {
      event: 'new_group',
      groupId: `${groupId}`,
      groupName: `${groupName}`
    }
  }

  var message = {
    data: payload.data,
    "android": {
      "priority": "high"
    },
    apns: {
      headers: {
        "apns-topic": `${packageName}`
      },
      payload: {

        aps: {
          alert: {
            title: 'New Group',
            body: `you have been added to ${groupName}`,

          },
          "mutable-content": 1,
        },
      },
    },
  };

  return message
}

function getNewCallPayload(callId, fromId, toId, timestamp, callType, channel) {
  const payload = {
    data: {
      event: 'new_call',
      callId,
      fromId,
      toId,
      timestamp: `${timestamp}`,
      callType: `${callType}`,
      channel
    }
  }


  var message = {
    data: payload.data,
    "android": {
      "priority": "high"
    },
    apns: {
      headers: {
        "apns-topic": `${packageName}`
      },
      payload: {

        aps: {
          alert: {
            title: 'New Call',
            body: `new call`,

          },
          "mutable-content": 1,
        },
      },
    },
  }

  return message

}


function removeExtraLimitForPayload(data){
  let payloadSize = sizeof(data)

  if (payloadSize > MAX_FCM_LIMIT) {
    if (data.type === SENT_TEXT && data.partialText) {
      data.content = ''
    }
    data.thumb = ''
  }

  return data
}


function getMessagePayload(type, val, senderPhone, content, timestamp, fromId, toId, isGroup, messageId, metadata) {
  return "";

}



//remove undefined items from payload
function removeUndefined(obj) {
  for (var propName in obj) {
    if (typeof obj[propName] === "undefined" || obj[propName] === undefined || obj[propName] === 'undefined') {
      delete obj[propName];
    }
  }
}


//check if there is another admin in a group
function isThereAdmin(obj) {
  for (const k in obj) {
    if (obj[k] === true) {
      return true;
    }
  }
}

//check if message time not passed
function timePassed(timestamp) {
  return Math.floor((new Date() - timestamp) / 60000) > MESSAGE_TIME_LIMIT
}

function getCondition(groupId, fromId) {
  return `'${groupId}' in topics && !('${fromId}' in topics)`

}
/*
since PushKit Tokens may be generated only once for each device
we need to take an action in case if a user uninstalls the app on his device
and later on he signed in using another number, therefore we want to delete the token for old user
*/
exports.indexPKToken = functions.database.ref(`users/{userId}/pktoken/{token}`).onCreate(async (snap, context) => {

})

