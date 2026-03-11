/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.SplashActivity;
import com.teamxdevelopers.SuperChat.utils.FireConstants;
import com.teamxdevelopers.SuperChat.utils.IntentUtils;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;

public class DelateAccountFragment extends Fragment {
    EditText txtyes;
    RelativeLayout btn_delete;
    ProgressBar pb_delete;
    String text;

    public DelateAccountFragment() {
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
        View view=inflater.inflate(R.layout.fragment_delate_account,container,false);

        txtyes=view.findViewById(R.id.txtyes);
        btn_delete=view.findViewById(R.id.btn_delete);
        pb_delete=view.findViewById(R.id.pb_delete);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text=txtyes.getText().toString();
                btn_delete.setEnabled(false);
                pb_delete.setVisibility(View.VISIBLE);
                cheking();
            }
        });




        return view;
    }

    private void cheking() {
        if(text.matches("yes")){
            delate();
        }else {
            txtyes.setError("Enter 'yes' to delete the account");
            btn_delete.setEnabled(true);
            pb_delete.setVisibility(View.GONE);
        }
    }

    private void delate() {
        Intent intent = new Intent();
        intent.setAction(IntentUtils.ACTION_LOGOUT);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Deleting...");
        progressDialog.show();
        try{
            FireConstants.mainRef.child("deleteUsersRequests")
                    .child(FireManager.getUid()).setValue(true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            FireManager.logoutAndDelete();
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }catch (Exception e){
            progressDialog.dismiss();
            Toast.makeText(getContext(), getString(R.string.error   ), Toast.LENGTH_SHORT).show();
        }
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        assert user != null;
//        user.delete()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            btn_delete.setEnabled(true);
//                            pb_delete.setVisibility(View.GONE);
//                            Log.d(TAG, "User account deleted.");
//                            SharedPreferencesManager.setUserInfoSaved(false);
//                            SharedPreferencesManager.setE2ESaved(false);
//                            startActivity(new Intent(getContext(), SplashActivity.class));
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                btn_delete.setEnabled(true);
//                pb_delete.setVisibility(View.GONE);
//                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}