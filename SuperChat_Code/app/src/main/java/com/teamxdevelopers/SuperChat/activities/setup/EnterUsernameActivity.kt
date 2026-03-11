/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.setup

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.canhub.cropper.CropImage
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.ActivityEnterUsernameBinding
import com.teamxdevelopers.SuperChat.exceptions.BackupFileMismatchedException
import com.teamxdevelopers.SuperChat.utils.*
import com.teamxdevelopers.SuperChat.utils.mediastore.MediaStoreFileInfo
import com.teamxdevelopers.SuperChat.utils.mediastore.MediaStoreUtil
import dalvik.system.ZipPathValidator
import io.realm.exceptions.RealmMigrationNeededException
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

class EnterUsernameActivity : AppCompatActivity() {

    companion object {
        private const val RC_PICK_BACKUP_FILE = 74
        private const val RC_PICK_DB_FILE = 75
    }

    private val viewModel: EnterUsernameViewModel by viewModels()

    private var pickedPhoto: String? = null
    private var backupFileUri: String? = null
    private var dbFileUri: String? = null

    private lateinit var binding: ActivityEnterUsernameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeObservers()

        viewModel.fetchUserImage()


        if (!BuildVerUtil.isApi29OrAbove() && RealmBackupRestore.isBackupFileExists) {
            binding.checkTextViewNumber.visibility = View.VISIBLE
        } else {
            binding.checkTextViewNumber.visibility = View.GONE
        }

        binding.userImgSetup.setOnClickListener { pickImage() }

        //On Done Keyboard Button Click
        binding.etUsernameSetup.setOnEditorActionListener(
            TextView.OnEditorActionListener
            { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    completeSetup()
                    return@OnEditorActionListener true
                }
                false
            })

        binding.fabSetupUser.setOnClickListener {
            completeSetup()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (BuildVerUtil.isApi29OrAbove()) {
            MenuInflater(this).inflate(R.menu.menu_enter_username, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_choose_backup) {
            pickBackupFile()
        } else if (item.itemId == R.id.menu_item_restore_db) {
            pickDbFile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun pickImage() {
        CropImageRequest.getCropImageRequest().start(this)
    }

    private fun completeSetup() {
        //check if user not entered his username
        val userName = binding.etUsernameSetup.text.toString()
        if (TextUtils.isEmpty(userName)) {
            binding.etUsernameSetup.error = getString(R.string.username_is_empty)
        } else {

            if (binding.checkTextViewNumber.visibility == View.VISIBLE && binding.checkTextViewNumber.isChecked) {
                try {
                    //Try to restore backup if it's Below API 29
                    RealmBackupRestore().restore(false)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: RealmMigrationNeededException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: BackupFileMismatchedException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.backup_file_mismatched, Toast.LENGTH_SHORT).show()
                }

            }

            SharedPreferencesManager.saveSetupInfo(userName, pickedPhoto, backupFileUri, dbFileUri)
            SharedPreferencesManager.setHasEnteredUsername(true)
            SetupUserActivity.start(this, userName, pickedPhoto, backupFileUri, dbFileUri)
            finish()

        }
    }

    private fun subscribeObservers() {
        viewModel.loadUserImageLiveData.observe(this) { photoUrl ->
            loadUserPhoto(photoUrl)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result?.uri ?: return


                val file = DirManager.getMyPhotoPath()
                try {
                    //copy image to the App Folder
                    val bitmap = MediaStoreUtil.getBitmapByUri(this, resultUri)

                    if (bitmap == null) {
                        Toast.makeText(this, "could not get file", Toast.LENGTH_SHORT).show()
                        return
                    }


                    BitmapUtils.convertBitmapToJpeg(bitmap, file)

                    Glide.with(this).load(file).into(binding.userImgSetup)
                    pickedPhoto = file.path
                    binding.progressBar.visibility = View.GONE
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.could_not_get_this_image, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == RC_PICK_BACKUP_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                dbFileUri = null
                contentResolver.openInputStream(uri)?.use {
                    if (isBackupInfoExists(it)) {
                        backupFileUri = uri.toString()
                    } else {
                        Util.showSnackbar(this, getString(R.string.invalid_backup_file))
                    }
                }
            }
        } else if (requestCode == RC_PICK_DB_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                backupFileUri = null
                if (MediaStoreFileInfo.getFileExtensionByFileName(uri) == RealmBackupRestore.BACKUP_FILE_EXTENSION) {
                    dbFileUri = uri.toString()
                } else {
                    Util.showSnackbar(this, getString(R.string.invalid_backup_file))
                }
            }
        }


    }

    private fun isBackupInfoExists(it: InputStream): Boolean {
        if(BuildVerUtil.isApi34OrAbove()){
            //opt out of ZipPathValidator
            ZipPathValidator.clearCallback()
        }
        val zipInputStream = ZipInputStream(it)
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name == "/backup-info.json") {
                return true
            }
            entry = zipInputStream.nextEntry
        }
        return false
    }

    private fun loadUserPhoto(photoUrl: String) {
        //load the image
        //we are using listener to determine when the image loading is finished
        //so we can hide the progressBar

        Glide.with(this).load(photoUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

            }).into(binding.userImgSetup)
    }

    private fun pickBackupFile() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*";
            startActivityForResult(intent, RC_PICK_BACKUP_FILE)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }

    }

    private fun pickDbFile() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*";
            startActivityForResult(intent, RC_PICK_DB_FILE)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }

    }
}
