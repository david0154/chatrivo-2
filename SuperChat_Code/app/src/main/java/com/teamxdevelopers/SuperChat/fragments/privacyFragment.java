/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;

public class privacyFragment extends PreferenceFragmentCompat implements View.OnClickListener {
   private SwitchCompat s_blockss;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacy, container, false);


        s_blockss=view.findViewById(R.id.s_blockss);
        setcheked();



        s_blockss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.setHideOnline(isChecked);

            }
        });



        return view;
    }
    //


    private void setcheked() {
        boolean hide = SharedPreferencesManager.getHideOnline();
        s_blockss.setChecked(hide);
    }



    @Override
    public void onClick(View view) {

    }
}