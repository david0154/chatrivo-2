/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters.messaging.holders.base

import android.content.Context
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.ListUtil
import de.hdodenhof.circleimageview.CircleImageView

// received message holders
open class BaseReceivedHolder(context: Context, itemView: View) : BaseHolder(context, itemView) {
    var userName: TextView? = itemView.findViewById(R.id.tv_username_group)
    var userProfile: CircleImageView?= itemView.findViewById(R.id.user_img_chat)



    override fun bind(message: Message, user: User) {
        super.bind(message, user)

        if (user.isGroupBool && userName != null) {
            userName?.visibility = View.VISIBLE
            val fromId = message.fromId
            val userById = ListUtil.getUserById(fromId, user.getGroup().getUsers())
            if (userById != null) {
                val name = userById.userName
                val profile=userById.thumbImg

                if (name != null)
                    userName?.text = name
                if(profile != null){
                    Glide.with(context).load(profile).into(userProfile!!)
                }

            } else {
                userName?.text = message.fromPhone
                Glide.with(context).load(R.drawable.user_img).into(userProfile!!)
            }
        }

    }



}
