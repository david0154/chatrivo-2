/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.compose.ui.unit.Constraints
import com.teamxdevelopers.SuperChat.R
//import io.agora.agorauikit_android.AgoraConnectionData
//import io.agora.agorauikit_android.AgoraVideoViewer
//import io.agora.rtc2.Constants

class GroupCallViewActivity : AppCompatActivity() {
//    var agView:AgoraVideoViewer? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_call_view)
//        agView= AgoraVideoViewer(this, AgoraConnectionData(appId = resources.getString(R.string.agora_app_id)))

        val set = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
//
//        this.addContentView(agView,set)
//
//        agView!!.join(channel = intent.getStringExtra("chanel")!!, role = Constants.CLIENT_ROLE_BROADCASTER, token = null)
    }
}