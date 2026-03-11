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
import com.teamxdevelopers.SuperChat.adapters.messaging.holders.base.BaseReceivedHolder
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager
import de.hdodenhof.circleimageview.CircleImageView

// received message with type text
class ReceivedTextHolder(context: Context, itemView: View) : BaseReceivedHolder(context,itemView) {

    private var tvMessageContent: AXEmojiTextView = itemView.findViewById(R.id.tv_message_content)
    private var userImgToolbarChatAct: CircleImageView=itemView.findViewById(R.id.user_img_chat)



    override fun bind(message: Message, user: User) {
        super.bind(message,user)
        tvMessageContent.text = message.content


        if (user.isGroupBool) (
                userImgToolbarChatAct.setVisibility(View.VISIBLE)
                )else{
            if (user.thumbImg != null)
                userImgToolbarChatAct.setVisibility(View.GONE)
                }



        //set font size
        val fontSize =  SharedPreferencesManager.getFontSize()
        tvMessageContent.textSize = fontSize.toFloat()
        tvMessageContent.setEmojiSize(fontSize*3)

    //set shortcut
    }


}