/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging

import com.teamxdevelopers.SuperChat.model.realms.Message

interface AudibleInteraction {
    fun onSeek(message: Message, progress:Int, max:Int)
}