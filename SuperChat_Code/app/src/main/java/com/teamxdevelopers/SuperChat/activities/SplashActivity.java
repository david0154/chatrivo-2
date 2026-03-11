/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.authentication.AuthenticationActivity;
import com.teamxdevelopers.SuperChat.activities.main.MainActivity;
import com.teamxdevelopers.SuperChat.activities.setup.EnterUsernameActivity;
import com.teamxdevelopers.SuperChat.activities.setup.SetupUserActivity;
import com.teamxdevelopers.SuperChat.model.constants.EncryptionType;
import com.teamxdevelopers.SuperChat.utils.DetachableClickListener;
import com.teamxdevelopers.SuperChat.utils.MyApp;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;
import com.teamxdevelopers.SuperChat.utils.PermissionsUtil;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;

import java.util.Objects;

//this is the First Activity that launched when user starts the App
public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 451;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);//revert back to default theme after loading splash image
        setContentView(R.layout.activity_splash);

        String DarkMode = SharedPreferencesManager.getDarkMode();

        if(DarkMode.matches("night")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
       // Toast.makeText(SplashActivity.this, DarkMode, Toast.LENGTH_SHORT).show();

        SubscribeToAllUsersNotification();

        if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
            startPrivacyPolicyActivity();
            //check if user isLoggedIn
        } else if (!FireManager.isLoggedIn()) {
            startLoginActivity();
            //request permissions if there are no permissions granted
        } else if (MyApp.context().getString(R.string.encryption_type).equalsIgnoreCase(EncryptionType.E2E) && !SharedPreferencesManager.isE2ESaved()) {
            startSaveE2EActivity();
        } else if (FireManager.isLoggedIn() && !PermissionsUtil.hasPermissions(this)) {
            requestPermissions();
        } else {
            startNextActivity();
        }

    }

    private void SubscribeToAllUsersNotification() {
        if (!SharedPreferencesManager.isAllUsersNotificationSubscribe()){
            FirebaseMessaging.getInstance().subscribeToTopic("allUsers").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()){
                        Log.d("NotificationSubscribe","Successfully");
                        SharedPreferencesManager.setAllUsersNotificationSubscribe(true);
                    }
                }
            });
        }
    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions(), PERMISSION_REQUEST_CODE);
    }


    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startSaveE2EActivity() {
        Intent intent = new Intent(this, SaveE2EActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startPrivacyPolicyActivity() {
        Intent intent = new Intent(this, AgreePrivacyPolicyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startNextActivity() {
        if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
            startPrivacyPolicyActivity();
        } else if (SharedPreferencesManager.isUserInfoSaved()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (!SharedPreferencesManager.hasEnteredUsername()) {
            Intent intent = new Intent(this, EnterUsernameActivity.class);
            startActivity(intent);
            finish();
        } else {
            String username = SharedPreferencesManager.getUserName();
            String localPhotoPath = SharedPreferencesManager.getLocalPhotoPathSetup();
            String backupUri = SharedPreferencesManager.getLocalBackupPath();
            String dbUri = SharedPreferencesManager.getDbFileUri();
            SetupUserActivity.start(this, username, localPhotoPath, backupUri,dbUri);
            finish();
        }

    }


    private void showAlertDialog() {

        DetachableClickListener positiveClickListener = DetachableClickListener.wrap(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions();

            }
        });

        DetachableClickListener negativeClickListener = DetachableClickListener.wrap(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });


        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions)
                .setMessage(R.string.you_have_to_grant_permissions)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
                .create();

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder);
        negativeClickListener.clearOnDetach(builder);
        builder.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!FireManager.isLoggedIn())
                startLoginActivity();
            else
                startNextActivity();
        } else
            showAlertDialog();
    }

}



