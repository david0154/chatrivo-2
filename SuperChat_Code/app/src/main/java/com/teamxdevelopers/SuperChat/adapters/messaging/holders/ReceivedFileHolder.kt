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
import android.widget.TextView
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.adapters.messaging.holders.base.BaseReceivedHolder
import com.teamxdevelopers.SuperChat.common.extensions.setHidden
import com.teamxdevelopers.SuperChat.model.constants.DownloadUploadStat
import com.teamxdevelopers.SuperChat.model.realms.Message
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.Util
import de.hdodenhof.circleimageview.CircleImageView

class ReceivedFileHolder(context: Context, itemView: View) : BaseReceivedHolder(context, itemView) {

    private val fileIcon: ImageView
    private val tvFileName: TextView
    private val tvFileExtension: TextView
    private val tvFileSize: TextView
    private var userImgToolbarChatAct: CircleImageView =itemView.findViewById(R.id.user_img_chat)

    override fun bind(message: Message, user: User) {
        super.bind(message, user)

        if (user.isGroupBool) (
                userImgToolbarChatAct.setVisibility(View.VISIBLE)
                )else{
            if (user.thumbImg != null)
                userImgToolbarChatAct.setVisibility(View.GONE)
        }

        //get file extension
        val fileExtension = Util.getFileExtensionFromPath(message.metadata).toUpperCase()
        tvFileExtension.text = fileExtension
        //set file name
        tvFileName.text = message.metadata

        //file size
        tvFileSize.text = message.fileSize

        fileIcon.setHidden(message.downloadUploadStat !=DownloadUploadStat.SUCCESS, true)


    }

    init {
        fileIcon = itemView.findViewById(R.id.file_icon)
        tvFileName = itemView.findViewById(R.id.tv_file_name)
        tvFileExtension = itemView.findViewById(R.id.tv_file_extension)
        tvFileSize = itemView.findViewById(R.id.tv_file_size)
    }


}

