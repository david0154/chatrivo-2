/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */
package com.teamxdevelopers.SuperChat.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.ActivityQrBinding
import com.teamxdevelopers.SuperChat.utils.BuildVerUtil
import com.teamxdevelopers.SuperChat.utils.DirManager
import com.teamxdevelopers.SuperChat.utils.IntentUtils
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager
import com.teamxdevelopers.SuperChat.utils.network.FireManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class QrActivity : AppCompatActivity() {

    private val QR_SCAN_REQUEST_CODE = 123 // declared globally
    private lateinit var data:QrData
    private lateinit var binding: ActivityQrBinding

    companion object {
        val QR_TYPE = "type"
        val QR_CONTENT = "content"
        val QR_GROUP_NAME = "name"
        val QR_UID ="uid"

        val TYPE_GROUP = "group"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar!!.title="QR code"
        window.statusBarColor=getColor(R.color.colorPrimary)

        val layout = window.attributes
        layout.screenBrightness = 1f
        window.attributes = layout

        val qr_type = intent.getStringExtra(QR_TYPE)
        if (qr_type.equals(TYPE_GROUP)){
            data = QrData.Url(intent.getStringExtra(QR_CONTENT)!!)
            binding.tvName.text = intent.getStringExtra(QR_GROUP_NAME)
            binding.tvAppName.text="${resources.getString(R.string.app_name)} Group"


        }else{
            val userName = SharedPreferencesManager.getUserName()
            binding.tvName.text=userName
            val phoneNumber = SharedPreferencesManager.getPhoneNumber()
            data = QrData.Url("https://" + resources.getString(R.string.account_link) + "/" + phoneNumber)
            binding.tvAppName.text="${resources.getString(R.string.app_name)} Contact"


        }






        val options = QrVectorOptions.Builder()
            .setPadding(.3f)
            .setLogo(
                QrVectorLogo(
                    drawable = ContextCompat
                        .getDrawable(this, R.mipmap.ic_launcher),
                    size = .25f,
                    padding = QrVectorLogoPadding.Natural(.1f),
                    shape = QrVectorLogoShape
                        .Circle
                )
            )
            .setBackground(
                QrVectorBackground(
                    drawable = ContextCompat
                        .getDrawable(this, R.drawable.bg_qr),
                )
            )
            .setColors(
                QrVectorColors(
                    dark = QrVectorColor
                        .Solid(Color(0xff345288)),
                    ball = QrVectorColor.Solid(
                        ContextCompat.getColor(this, R.color.colorBlack)
                    ),
                    frame = QrVectorColor.LinearGradient(
                        colors = listOf(
                            0f to Color.RED,
                            1f to Color.BLUE,
                        ),
                        orientation = QrVectorColor.LinearGradient
                            .Orientation.LeftDiagonal
                    )
                )
            )
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape
                        .RoundCorners(.5f),
                    ball = QrVectorBallShape
                        .RoundCorners(.25f),
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f),
                )
            )
            .build()

        val drawable : Drawable = QrCodeDrawable(data, options)

        binding.ivQrView.setImageDrawable(drawable)

        binding.btnScan.setOnClickListener {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
                        startScanActivity()

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                        Toast.makeText(applicationContext,resources.getString(R.string.missing_permissions),Toast.LENGTH_SHORT).show()

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) { /* ... */
                        Toast.makeText(applicationContext,resources.getString(R.string.missing_permissions),Toast.LENGTH_SHORT).show()
                    }
                }).check()
        }


        binding.btnShare.setOnClickListener {
            if (!BuildVerUtil.isApi33OrAbove()){
                Dexter.withContext(this)
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            val bitmap = takeScreenshot()
                            saveBitmap(bitmap!!)/* ... */
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest>,
                            token: PermissionToken
                        ) {
                        Toast.makeText(applicationContext,resources.getString(R.string.missing_permissions),Toast.LENGTH_SHORT).show()
                        }
                    }).check()
            }else{
                val bitmap = takeScreenshot()
                saveBitmap(bitmap!!)
            }

        }


    }
    fun startScanActivity(){
        startActivity(Intent(this, QrScanActivity::class.java))
    }
    fun takeScreenshot(): Bitmap? {
//        val rootView: View = findViewById<View>(android.R.id.content).rootView
        val rootView:View = findViewById(R.id.share_qr_view)
        rootView.isDrawingCacheEnabled = true
        return rootView.drawingCache
    }

    fun saveBitmap(bitmap: Bitmap) {
        val imagePath = DirManager.genereateQRFile(getQrName())
        val fos: FileOutputStream
        try {
            fos = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            shareIt(imagePath)
        } catch (e: FileNotFoundException) {
            Log.e("GREC", e.message, e)
        } catch (e: IOException) {
            Log.e("GREC", e.message, e)
        }
    }

    private fun getQrName(): String? {
        if(intent.getStringExtra(QR_TYPE).equals(TYPE_GROUP)){
            return intent.getStringExtra(QR_UID)
        }else
            return FireManager.uid
    }

    private fun shareIt(file:File) {
        val shareImageIntent = IntentUtils.getShareImageIntent(file.path)
        shareImageIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        shareImageIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(shareImageIntent)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}