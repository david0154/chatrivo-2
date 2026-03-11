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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.utils.FireConstants;

public class MaintenanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        FireConstants.appSettings.child("maintenanceMode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && (Boolean.FALSE.equals(snapshot.getValue(Boolean.class)))){
                    startActivity(new Intent(MaintenanceActivity.this,SplashActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}