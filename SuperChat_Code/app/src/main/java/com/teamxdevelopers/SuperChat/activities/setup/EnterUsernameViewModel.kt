/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamxdevelopers.SuperChat.common.extensions.toDeffered
import com.teamxdevelopers.SuperChat.utils.FireConstants
import com.teamxdevelopers.SuperChat.utils.network.FireManager
import kotlinx.coroutines.launch

class EnterUsernameViewModel : ViewModel() {

    private val _loadUserImage = MutableLiveData<String>()
    val loadUserImageLiveData: LiveData<String>
        get() = _loadUserImage

    fun fetchUserImage() {

        viewModelScope.launch {

            val snapshot = FireConstants.usersRef.child(FireManager.uid)
                .toDeffered().await()

            val photoUrl = snapshot.child("photo").value as? String?

            if (photoUrl != null) {
                _loadUserImage.value = photoUrl
            }

        }
    }

}