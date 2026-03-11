/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.activities.main.MainActivity
import com.teamxdevelopers.SuperChat.services.CompleteSetupService
import com.teamxdevelopers.SuperChat.services.SetupServiceEvent
import com.teamxdevelopers.SuperChat.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.teamxdevelopers.SuperChat.databinding.ActivitySetupUserBinding

class SetupUserActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySetupUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra(IntentUtils.EXTRA_USERNAME) ?: " "
        val pickedPhotoLocalPath = intent.getStringExtra(IntentUtils.EXTRA_PICKED_PHOTO)
        val pickedBackupFile = intent.getStringExtra(IntentUtils.EXTRA_PICKED_BACKUP)
        val pickedDbUri = intent.getStringExtra(IntentUtils.EXTRA_DB_FILE_URI)



        if (!CompleteSetupService.isAcitve) {
            CompleteSetupService.startService(
                this,
                username,
                pickedPhotoLocalPath,
                pickedBackupFile,
                pickedDbUri
            )
        }

        CompleteSetupService.progressLiveData.observe(this) { progressPer ->
            progressPer?.let {
                binding.progress.progress = it
            }
        }

        CompleteSetupService.event.observe(this) { event ->
            if (event == null) {
                return@observe
            }

            when (event) {
                is SetupServiceEvent.SetupStarted -> {
                    binding.tvRestoreInProgress.text = getString(R.string.initializing)
                }

                is SetupServiceEvent.RestoreStarted -> {
                    binding.tvRestoreInProgress.text = getString(R.string.initializing)
                    binding.progress.isVisible = true
                }

                is SetupServiceEvent.SetupFinalizing -> {
                    binding.tvRestoreInProgress.text = getString(R.string.finalizing)
                }

                is SetupServiceEvent.SetupFinished -> {
                    startMainActivity()
                }

                is SetupServiceEvent.SetupError -> {
                    AlertDialog.Builder(this).apply {
                        setTitle(R.string.error)
                        setMessage(event.error.localizedMessage)
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
                }

                else -> {

                }
            }
        }
    }


    private fun showSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            R.string.no_internet_connection,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (SharedPreferencesManager.isCurrentUserInfoSaved()) {
            startMainActivity()
        }
    }


    companion object {
        @JvmStatic
        fun start(
            context: Context,
            userName: String,
            pickedPhotoLocalPath: String?,
            backupUri: String?,
            dbUri: String?
        ) {
            Intent(context, SetupUserActivity::class.java).apply {
                putExtra(IntentUtils.EXTRA_USERNAME, userName)
                putExtra(IntentUtils.EXTRA_PICKED_PHOTO, pickedPhotoLocalPath)
                putExtra(IntentUtils.EXTRA_PICKED_BACKUP, backupUri)
                putExtra(IntentUtils.EXTRA_DB_FILE_URI, dbUri)
                context.startActivity(this)
            }
        }
    }
}

