/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.adapters.messaging.holders.base.BaseReceivedHolder
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import de.hdodenhof.circleimageview.CircleImageView

class ReceivedStickerHolder(context: Context, itemView: View) : BaseReceivedHolder(context, itemView) {
    private val stickerImageView = itemView.findViewById<ImageView>(R.id.img_received_sticker)
    private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_received_sticker)
    private var userImgToolbarChatAct: CircleImageView =itemView.findViewById(R.id.user_img_chat)

    override fun bind(message: Message, user: User) {
        super.bind(message, user)

        if (user.isGroupBool) (
                userImgToolbarChatAct.setVisibility(View.VISIBLE)
                )else{
            if (user.thumbImg != null)
                userImgToolbarChatAct.setVisibility(View.GONE)
        }

        progressBar.isVisible = message.localPath.isNullOrEmpty()
        if (!message.localPath.isNullOrEmpty()) {
            Glide.with(itemView.context).load(message.localPath).into(stickerImageView)
        }
    }
}