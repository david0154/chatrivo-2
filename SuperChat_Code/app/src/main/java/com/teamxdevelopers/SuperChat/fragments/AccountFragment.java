/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.QrActivity;


public class AccountFragment extends Fragment {

    CardView cv_account_verification,cv_delete_account;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);


        cv_delete_account=view.findViewById(R.id.cv_delete_acc);
        cv_account_verification = view.findViewById(R.id.cv_account_verification);

//        cv_qr.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//              startActivity(new Intent(getContext(), QrActivity.class));
//            }
//        });

        cv_delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_AccountSettingsFragment_to_deleteFragment);
            }
        });
//
//        cv_tow_step_verification.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Navigation.findNavController(view).navigate(R.id.action_AccountSettingsFragment_to_AccountEnable2svSettingsFragment);
//
//            }
//        });

        cv_account_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_AccountSettingsFragment_to_accountVerificationFragment);
            }
        });

        return view;
    }
}