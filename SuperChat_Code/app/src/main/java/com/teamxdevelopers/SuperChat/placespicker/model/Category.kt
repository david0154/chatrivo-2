/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.placespicker.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Category(
        @SerializedName("icon")
        val icon: Icon,
        @SerializedName("id")
        val id: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("pluralName")
        val pluralName: String,
        @SerializedName("primary")
        val primary: Boolean,
        @SerializedName("shortName")
        val shortName: String
)