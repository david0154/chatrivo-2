/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.activities.authentication.AuthenticationActivity
import com.teamxdevelopers.SuperChat.databinding.ActivityAgreePrivacyPolicyBinding
import com.teamxdevelopers.SuperChat.utils.DetachableClickListener
import com.teamxdevelopers.SuperChat.utils.PermissionsUtil
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager
import com.teamxdevelopers.SuperChat.utils.network.FireManager


class AgreePrivacyPolicyActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 451
    private lateinit var binding: ActivityAgreePrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreePrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvWelcome.text = "Welcome to ${resources.getString(R.string.app_name)}"

        binding.btnAgree.setOnClickListener {

            showContactsConfirmationDialog()

        }


    }
    private fun showContactsConfirmationDialog() {



        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_bottomsheet_agree_privacy_policy)

        val tvAgreement = dialog.findViewById<TextView>(R.id.tv_agreement)
        val btnAgree = dialog.findViewById<Button>(R.id.btn_agree)

        getHtml4(tvAgreement)

        btnAgree.setOnClickListener(View.OnClickListener {
            SharedPreferencesManager.setAgreedToPrivacyPolicy(true)
            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        })


        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)


    }


    private fun showContactsConfirmationDialogOld() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Agreement")
        dialog.setCancelable(false)


        val view = LayoutInflater.from(this).inflate(R.layout.privacy_policy_dialog, null, false)
        dialog.setView(view)

        val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)

        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text = "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"


        getHtml4(tv)

        dialog.setNegativeButton("DECLINE", null)

        dialog.setPositiveButton("AGREE") { dialog, which ->
            SharedPreferencesManager.setAgreedToPrivacyPolicy(true)

            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        }

        val mDialog = dialog.show()
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->

            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked

        }

    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions(), PERMISSION_REQUEST_CODE)
    }

    private fun startPrivacyPolicyActivity() {
        val intent = Intent(this, AgreePrivacyPolicyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }


    private fun getHtml4(textView: TextView) {

        val html = resources.getString(R.string.privacy_policy_html)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        } else {
            textView.setText(Html.fromHtml(html))
        }

    }

    private fun startLoginActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun startNextActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        } else
            showAlertDialog()
    }

    private fun showAlertDialog() {

        val positiveClickListener = DetachableClickListener.wrap { dialogInterface, i -> requestPermissions() }

        val negativeClickListener = DetachableClickListener.wrap { dialogInterface, i -> finish() }


        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions)
                .setMessage(R.string.you_have_to_grant_permissions)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
                .create()

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder)
        negativeClickListener.clearOnDetach(builder)
        builder.show()
    }
}
