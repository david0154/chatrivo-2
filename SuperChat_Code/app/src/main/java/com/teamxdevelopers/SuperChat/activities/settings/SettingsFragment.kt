/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.databinding.FragmentSettingsBinding
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager


class SettingsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickedItem = SharedPreferencesManager.getClickedSettingsItem()
        when(clickedItem){
            0 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_profilePreferenceFragment)
            1 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_AccountSettingsFragment)
            2 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_notificationPreferenceFragment)
            3 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_securityPreferencesFragment)
            4 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_chatSettingsPreferenceFragment2)
            5 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_customizeSettingsPreferenceFragment2)
            6 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            7 -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_aboutFragment2)

        }

        binding.tvProfile.setOnClickListener(this)
        binding.tvAccount.setOnClickListener(this)
        binding.tvPrivacy.setOnClickListener(this)
        binding.tvNotifications.setOnClickListener(this)
        binding.tvSecurity.setOnClickListener(this)
        binding.tvChat.setOnClickListener(this)
        binding.tvCustomize.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
        binding.tvAbout.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.tv_profile -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_profilePreferenceFragment)
            R.id.tv_account -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_AccountSettingsFragment)
            R.id.tv_privacy -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_privacySettingsFragment)
            R.id.tv_notifications -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_notificationPreferenceFragment)
            R.id.tv_security -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_securityPreferencesFragment)
            R.id.tv_chat -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_chatSettingsPreferenceFragment2)
            R.id.tv_customize -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_customizeSettingsPreferenceFragment2)
            R.id.tv_privacy_policy -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            R.id.tv_about -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_aboutFragment2)


        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}