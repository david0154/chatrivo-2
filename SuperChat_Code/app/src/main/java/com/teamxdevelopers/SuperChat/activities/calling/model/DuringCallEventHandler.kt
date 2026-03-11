/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



/*
 * Created by teamxdevelopers on 2020
 */

package com.teamxdevelopers.SuperChat.activities.calling.model

interface DuringCallEventHandler :
    AGEventHandler {
    fun onUserJoined(uid: Int)
    fun onDecodingRemoteVideo(uid: Int,  elapsed: Int)
    fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)
    fun onUserOffline(uid: Int, reason: Int)
    fun onExtraCallback(type: Int,  data: Array<Any?>)
}