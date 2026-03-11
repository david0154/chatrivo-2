/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging

import android.view.View
import com.teamxdevelopers.SuperChat.model.realms.Message

interface Interaction {
    fun onContainerViewClick(pos:Int,itemView:View,message: Message)
    fun onItemViewClick(pos:Int,itemView:View,message: Message)
    fun onLongClick(pos:Int,itemView:View,message: Message)
    fun onProgressButtonClick(pos:Int,itemView:View,message: Message)
    fun onQuotedMessageClick(pos:Int,itemView:View,message: Message)

}