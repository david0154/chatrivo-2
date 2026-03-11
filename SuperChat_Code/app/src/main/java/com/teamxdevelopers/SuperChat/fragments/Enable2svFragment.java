/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.teamxdevelopers.SuperChat.R;


public class Enable2svFragment extends Fragment {

    Button btn_enable;


    public Enable2svFragment() {
        // Required empty public constructor
    }


    public static Enable2svFragment newInstance(String param1, String param2) {
        Enable2svFragment fragment = new Enable2svFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enable2sv, container, false);
        btn_enable=view.findViewById(R.id.btn_enable);

        btn_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_AccountEnable2svSettingsFragment_to_AccountSet2svSettingsFragment);
            }
        });

        return view;
    }
}