/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.FragmentSecurityBinding
import com.teamxdevelopers.SuperChat.utils.MyApp.Companion.context
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager
import com.teamxdevelopers.SuperChat.utils.biometricks.BiometricPromptInfo
import com.teamxdevelopers.SuperChat.utils.biometricks.Biometricks
import com.teamxdevelopers.SuperChat.utils.biometricks.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SecurityPreferencesFragment : PreferenceFragmentCompat() {
    private lateinit var biometricks: Biometricks

    private var _binding : FragmentSecurityBinding? = null
    private val binding: FragmentSecurityBinding get() = _binding!!

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSecurityBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        biometricks = Biometricks.from(requireContext().applicationContext)

        //set to default

        val isFingerPrintLockEnabled = SharedPreferencesManager.isFingerprintLockEnabled()
        setLockAfterVisibility(isFingerPrintLockEnabled)


        binding.switchUnlockFingerprint.isEnabled = biometricks is Biometricks.Available
        binding.switchUnlockFingerprint.isChecked = isFingerPrintLockEnabled

        binding.switchUnlockFingerprint.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                showBiometricPrompt()
            } else {

                setLockAfterVisibility(false)

                if (compoundButton.isPressed) {
                    SharedPreferencesManager.setFingerprintLock(false)
                }
            }
        }

        setDefaultRadioGroupChecked()


        binding.radioGroupLockAfter.setOnCheckedChangeListener { radioGroup, id ->
            val lockAfter = when (id) {
                R.id.btn_radio_one_minute -> 1
                R.id.btn_radio_five_minutes -> 5
                R.id.btn_radio_thirty_minutes -> 30
                else -> 0 //Immediately
            }

            SharedPreferencesManager.setLockAfter(lockAfter)
        }


    }


    private fun setDefaultRadioGroupChecked() {
        val lockAfter = SharedPreferencesManager.getLockAfter()

        when (lockAfter) {
            1 -> binding.btnRadioOneMinute.isChecked = true
            5 -> binding.btnRadioFiveMinutes.isChecked = true
            30 -> binding.btnRadioThirtyMinutes.isChecked = true
            else -> binding.btnRadioImmediately.isChecked = true
        }


    }


    private fun showBiometricPrompt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }



        if (biometricks !is Biometricks.Available) {
            val string = getString(R.string.biometrics_not_available)
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
            return
        }

        val biometricName = when (biometricks) {
            Biometricks.Available.Face -> getString(R.string.face)
            Biometricks.Available.Fingerprint -> getString(R.string.fingerprint)
            Biometricks.Available.Iris -> getString(R.string.iris)
            Biometricks.Available.Unknown,
            Biometricks.Available.Multiple -> getString(R.string.biometric)
            else -> ""
        }




        lifecycleScope.launch {
            try {

                val cryptoObject = withContext(Dispatchers.IO) {
                    Crypto().cryptoObject()
                }

                Biometricks.showPrompt(
                    requireActivity(),
                    BiometricPromptInfo(
                        title = getString(R.string.authenticate_with, biometricName),
                        negativeButtonText = getString(R.string.cancel),
                        cryptoObject = cryptoObject
                    )
                ) { showLoading ->


                }

                SharedPreferencesManager.setFingerprintLock(true)
                setLockAfterVisibility(true)


            } catch (e: Exception) {
                binding.switchUnlockFingerprint.isChecked = false
                Toast.makeText(requireActivity(), R.string.could_not_add_fingerprint, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLockAfterVisibility(setVisible: Boolean) {
        binding.tvLockAfter.isVisible = setVisible
        binding.radioGroupLockAfter.isVisible = setVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}