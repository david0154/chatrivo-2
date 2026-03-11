/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.main.messaging.ChatActivity;
import com.teamxdevelopers.SuperChat.model.realms.User;
import com.teamxdevelopers.SuperChat.utils.IntentUtils;
import com.teamxdevelopers.SuperChat.utils.NetworkHelper;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;
import com.teamxdevelopers.SuperChat.utils.SnackbarUtil;

import java.util.List;

public class AccountLink extends BaseActivity {

    AlertDialog.Builder dialogBuilder;
    AlertDialog b;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Uri data = getIntent().getData();
        String scheme = data.getScheme();
        String host = data.getHost();
        List<String> params = data.getPathSegments();
        String first = params.get(0);

        searchUser(first);



 //       Toast.makeText(AccountLink.this, "Coming on next update", Toast.LENGTH_SHORT).show();


    }

    private void fringerprintIsEnabaled(String first) {
       boolean isEnable = SharedPreferencesManager.isFingerprintLockEnabled();

       if(isEnable){
           startActivity(new Intent(this,LockscreenActivity.class));

       }else {
           searchUser(first);
       }
    }

    public void searchUser(String phoneNumber) {
        if (!NetworkHelper.isConnected(AccountLink.this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,SplashActivity.class);
            startActivity(intent);
            return;
        }
        showProgress();
        getDisposables().add(getFireManager().fetchAndSaveUserByPhone(phoneNumber).subscribe(user -> {
            if (user == null) {
                hideProgress();
                SnackbarUtil.showDoesNotFireAppSnackbar(AccountLink.this);
            } else {
                hideProgress();
                startChatActivityWithDifferentUser(user);

            }
        }, throwable -> {
            hideProgress();
            SnackbarUtil.showDoesNotFireAppSnackbar(AccountLink.this);
        }));


    }

    private void onInvalidLink() {
        Toast.makeText(this, getString(R.string.invalid_account_link), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
    public void showProgress() {
        dialogBuilder = new AlertDialog.Builder(AccountLink.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        b = dialogBuilder.create();
        b.show();
    }
    public void hideProgress() {
        b.dismiss();
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);

    }
    private void startChatActivityWithDifferentUser(User user) {
        Intent intent = new Intent(AccountLink.this, ChatActivity.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean enablePresence() {
        return false;
    }
}