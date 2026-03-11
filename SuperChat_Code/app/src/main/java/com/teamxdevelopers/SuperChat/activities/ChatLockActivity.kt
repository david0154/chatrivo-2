/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2024 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.activities.main.messaging.ChatActivity
import com.teamxdevelopers.SuperChat.databinding.ActivityChatLockBinding
import com.teamxdevelopers.SuperChat.utils.IntentUtils
import com.teamxdevelopers.SuperChat.utils.biometricks.BiometricException
import com.teamxdevelopers.SuperChat.utils.biometricks.BiometricPromptInfo
import com.teamxdevelopers.SuperChat.utils.biometricks.Biometricks
import com.teamxdevelopers.SuperChat.utils.biometricks.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.InvalidAlgorithmParameterException

class ChatLockActivity : AppCompatActivity() {
    private  var userId:String? = ""
    private var isUnlockType = false;
    private lateinit var binding: ActivityChatLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isUnlockType = intent.getBooleanExtra(IntentUtils.ACTION_UNLOCK,false);
        userId = intent.getStringExtra(IntentUtils.UID)

        Log.d("LOCK", "Running Lock activity")


        if (isUnlockType){ //for unlock type
            binding.tvUnlockText.text = getString(R.string.use_your_fingerprint_to_unlock_chat);

        }else{ // for lock type
            binding.tvUnlockText.text = getString(R.string.use_your_fingerprint_to_lock_chat)
        }
        binding.btnRetry.setOnClickListener {
            showBiometricPrompt()
        }
    }

    override fun onResume() {
        super.onResume()
        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }


        val biometricks = Biometricks.from(applicationContext)

        if (biometricks !is Biometricks.Available) {
            val string = getString(R.string.biometrics_not_available)
            binding.tvUnlockText.text = string
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
            return
        }

        val biometricName = when (biometricks) {
            Biometricks.Available.Face -> getString(R.string.face)
            Biometricks.Available.Fingerprint -> getString(R.string.fingerprint)
            Biometricks.Available.Iris -> getString(R.string.iris)
            Biometricks.Available.Unknown,
            Biometricks.Available.Multiple -> getString(R.string.biometric)
        }

        binding.btnRetry.isVisible = false



        lifecycleScope.launch {
            try {

                val cryptoObject = withContext(Dispatchers.IO) {
                    Crypto().cryptoObject()
                }


                Biometricks.showPrompt(
                    this@ChatLockActivity,
                    BiometricPromptInfo(
                        title = getString(R.string.authenticate_with, biometricName),
                        negativeButtonText = getString(R.string.cancel),
                        cryptoObject = cryptoObject
                    )
                ) { showLoading ->

                    binding.progressBar.isVisible = showLoading


                }

                Log.d("LOCK", "$biometricName verified");

                if (isUnlockType){
                    val intent = Intent(this@ChatLockActivity,ChatActivity::class.java)
                    intent.putExtra(IntentUtils.UNLOCKED,true)
                    intent.putExtra(IntentUtils.UID, userId)
                    startActivity(intent)
                    finish()
                }else{
                    setResult(RESULT_OK)
                    finish()
                }

            } catch (e: Exception) {
                deviceDoseNotSupportDialog()
                Log.d("LOCK", "$biometricName can't verified");
                if (e is BiometricException) {

                    if (e.code == BiometricPrompt.ERROR_CANCELED || e.code == BiometricPrompt.ERROR_USER_CANCELED || e.code == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        binding.btnRetry.isVisible = true
                    } else {
                        binding.btnRetry.isVisible = false
                    }

                    binding.imgUnlockIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    binding.tvUnlockText.text = e.errString
                } else if (e is InvalidAlgorithmParameterException) {
                    Log.d("LOCK", "Can't use fingerprint lock in this device")


                }
            }
        }
    }

    private fun deviceDoseNotSupportDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.unable_to_set_up))
            .setMessage(getString(R.string.biometrics_not_available))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok),DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                finish()
            })

        dialog.show()
    }

//    override fun onBackPressed() {
//        //DO NOTHING AND PREVENT EXITING
//    }

}