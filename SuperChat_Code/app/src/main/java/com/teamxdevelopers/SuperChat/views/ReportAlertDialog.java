/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2024 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.utils.FireConstants;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;


public class ReportAlertDialog extends AlertDialog.Builder {
    public ReportAlertDialog(Context context,boolean isGroup,String uid) {
        super(context);
        setTitle(context.getString(R.string.report));
        setMessage(context.getString(R.string.report_info));
        setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Show progress dialog after "Yes" is clicked
                report(context,isGroup,uid);
            }
        });
        setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
    }

    private void report(Context context,boolean isGroup, String uid){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.reporting));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference databaseReference;
        if (isGroup){
            databaseReference = FireConstants.reportedGroups;
        }else {
            databaseReference = FireConstants.reportedUsers;
        }

        databaseReference.child(uid).child("list").child(FireManager.getUid()).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
