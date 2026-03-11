/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2024 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.utils.FireConstants;
import com.teamxdevelopers.SuperChat.utils.NotificationHelper;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;

public class AccountDisableActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_disable);

        FireConstants.usersRef.child(FireManager.getUid()).child("disabled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && (Boolean.FALSE.equals(snapshot.getValue(Boolean.class)))){
                    startActivity(new Intent(AccountDisableActivity.this,SplashActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startLoggedOutActivity();
                SharedPreferencesManager.clearSharedPrefs();
                FireManager.logout();
                finish();
            }
        });
    }

    @Override
    public boolean enablePresence() {
        return false;
    }
}