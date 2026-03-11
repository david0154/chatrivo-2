/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging.holders

import android.content.Context
import android.view.View
import com.aghajari.emojiview.view.AXEmojiTextView
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.adapters.messaging.holders.base.BaseSentHolder
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager


// sent message with type text
class SentTextHolder(context: Context, itemView: View) : BaseSentHolder(context, itemView) {
    private var tvMessageContent: AXEmojiTextView = itemView.findViewById(R.id.tv_message_content)


    override fun bind(message: Message, user: User) {
        super.bind(message, user)
        tvMessageContent.text = message.content

        //set font size
        val fontSize =  SharedPreferencesManager.getFontSize()
        tvMessageContent.textSize = fontSize.toFloat()
        tvMessageContent.setEmojiSize(fontSize*3)


//        if (text.length<3){
//            tvMessageContent.setEmojiSize(fontSize*5)
//        }else{
//            tvMessageContent.textSize = fontSize.toFloat()
//            tvMessageContent.width=(fontSize*3)
//            tvMessageContent.height=(fontSize*3)
//            tvMessageContent.setEmojiSize(fontSize*3)
//        }
    }

}



