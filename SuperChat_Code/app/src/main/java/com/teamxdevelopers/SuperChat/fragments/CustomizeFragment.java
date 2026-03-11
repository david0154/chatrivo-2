/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.aghajari.emojiview.view.AXEmojiTextView;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;

public class CustomizeFragment extends PreferenceFragmentCompat implements View.OnClickListener {
    private SwitchCompat s_dark,s_emoji,s_smartReply;
    private AXEmojiTextView tv_message;
    private SeekBar seekBar_font;
    private static final int progress = 14;  // Default font-size 14sp



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customize, container, false);

        initViews(view);
        setcheked();
        setSeekBarVal();

        seekBar_font.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateView(i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        s_dark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    //set night mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferencesManager.saveDarkMode("night");
                }else {
                    //set light mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferencesManager.saveDarkMode("light");
                }
            }

        });

        s_emoji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferencesManager.setIsOnEmojiRain(b);
            }
        });
        s_smartReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferencesManager.setIsOnSmartReply(b);
            }
        });

        return view;
    }

    private void setSeekBarVal() {
        int value = SharedPreferencesManager.getFontSize();
        seekBar_font.setProgress(value);



    }

    public void updateView(int fontSize) {
        tv_message.setTextSize(fontSize);
        SharedPreferencesManager.saveFontSize(fontSize);

    }

    private void setcheked() {
        String DarkMode = SharedPreferencesManager.getDarkMode();
        Boolean EmojiRain=SharedPreferencesManager.getIsOnEmojiRain();
        Boolean SmartReply= SharedPreferencesManager.getIsOnSmartReply();

        if(EmojiRain){
            s_emoji.setChecked(true);
        }else {
            s_emoji.setChecked(false);
        }

        if(SmartReply){
            s_smartReply.setChecked(true);
        }else {
            s_smartReply.setChecked(false);
        }

        if(DarkMode.matches("night")){
            s_dark.setChecked(true);
        }else {
            s_dark.setChecked(false);
        }
    }


    private void initViews(View view) {


        s_dark=view.findViewById(R.id.s_dark);
        seekBar_font=view.findViewById(R.id.seekbar_font);
        tv_message=view.findViewById(R.id.tv_message_content);
        s_emoji=view.findViewById(R.id.s_emoji);
        s_smartReply=view.findViewById(R.id.s_smart_reply);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {

    }
}