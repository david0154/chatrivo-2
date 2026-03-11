/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.teamxdevelopers.SuperChat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;


public class TwoSVFragment extends Fragment {

    EditText txt_email,txt_pin,txt_repin;
    Button btn_verify;
    FirebaseAuth mAuth;
    FrameLayout layout;
    FirebaseUser user;
    ProgressBar progressBar;


    public TwoSVFragment() {
        // Required empty public constructor
    }


    public static TwoSVFragment newInstance(String param1, String param2) {
        TwoSVFragment fragment = new TwoSVFragment();
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
        View view = inflater.inflate(R.layout.fragment_two_s_v, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        txt_email=view.findViewById(R.id.txt_email);
        txt_pin=view.findViewById(R.id.txt_pin);
        txt_repin=view.findViewById(R.id.txt_repin);
        btn_verify=view.findViewById(R.id.btn_verify);
        layout=view.findViewById(R.id.layout);
        progressBar=view.findViewById(R.id.progressBar);
        mAuth=FirebaseAuth.getInstance();

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void validation() {
        String email,pin,repin;
        email=txt_email.getText().toString();
        pin=txt_pin.getText().toString();
        repin=txt_repin.getText().toString();

        if(email.isEmpty()){
            txt_email.setError("Enter Email");
            progressBar.setVisibility(View.GONE);
        }else {
            if(pin.isEmpty()){
                txt_pin.setError("Enter PIN");
                progressBar.setVisibility(View.GONE);

            }else {
                if(repin.isEmpty()){
                    txt_repin.setError("Retype PIN");
                    progressBar.setVisibility(View.GONE);

                }else {
                    if(!(pin.matches(repin))){
                        txt_repin.setError("PINs don't match");
                        progressBar.setVisibility(View.GONE);
                    }else {
                        addemail(email,pin);
                    }
                }
            }
        }
    }

    private void addemail(String email, String pin) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, pin);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            signinFromEmail(email,pin);

                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Log.w(TAG,task.getException().getMessage());
                            Snackbar.make(layout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void signinFromEmail(String email, String pin) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, pin);
        FirebaseUser prevUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser currentUser = task.getResult().getUser();
                        verifingEmail(email);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void verifingEmail(String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "E-mail send to your mail box", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }
}