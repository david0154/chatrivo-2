/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.teamxdevelopers.SuperChat.activities.main.calls.CallsFragment;
import com.teamxdevelopers.SuperChat.activities.main.chats.FragmentChats;
import com.teamxdevelopers.SuperChat.activities.main.status.StatusFragment;
import com.teamxdevelopers.SuperChat.fragments.AccountFragment;
import com.teamxdevelopers.SuperChat.fragments.MainSettingsFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private int count = 4;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentChats();
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
            case 3:
                return new MainSettingsFragment();
            default:
                throw new IllegalStateException("Not implemented Fragment exception");
        }

    }

    @Override
    public int getCount() {
        return count;

    }



}
