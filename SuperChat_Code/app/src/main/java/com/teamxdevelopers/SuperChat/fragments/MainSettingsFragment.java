/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.QrActivity;
import com.teamxdevelopers.SuperChat.activities.settings.SettingsActivity;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainSettingsFragment extends Fragment {

    private CircleImageView imageViewUserProfile;
    private TextView tvUsername;
    private TextView tvStatus;


    public MainSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_settings, container, false);

        imageViewUserProfile = view.findViewById(R.id.image_view_user_profile);
        tvUsername = view.findViewById(R.id.tv_username);
        tvStatus = view.findViewById(R.id.tv_status);

        String userName = SharedPreferencesManager.getUserName();
        String status = SharedPreferencesManager.getStatus();
        final String myPhoto = SharedPreferencesManager.getMyPhoto();

        tvStatus.setText(status);
        tvUsername.setText(userName);
        Glide.with(getActivity()).load(new File(myPhoto))
                .into(imageViewUserProfile);
        
        view.findViewById(R.id.iv_qr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(getContext(), QrActivity.class));
            }
        });

        view.findViewById(R.id.ll_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(0);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(1);
                startSettingsActivity();
            }
        });
        
        view.findViewById(R.id.ll_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(1);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(2);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(3);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_chats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(4);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_customize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(5);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(6);
                startSettingsActivity();
            }
        });

        view.findViewById(R.id.ll_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager.setClickedSettingsItem(7);
                startSettingsActivity();
            }
        });




        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String userName = SharedPreferencesManager.getUserName();
        String status = SharedPreferencesManager.getStatus();
        final String myPhoto = SharedPreferencesManager.getMyPhoto();

        tvStatus.setText(status);
        tvUsername.setText(userName);
        Glide.with(getActivity()).load(new File(myPhoto))
                .into(imageViewUserProfile);
    }

    private void startSettingsActivity() {
        startActivity(new Intent(getContext(),SettingsActivity.class));
    }


}