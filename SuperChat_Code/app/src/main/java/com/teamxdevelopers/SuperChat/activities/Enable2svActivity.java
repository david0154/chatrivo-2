/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Enable2svActivity extends AppCompatActivity {

    EditText txt_email,txt_pin,txt_repin;
    Button btn_verify;
    FirebaseAuth mAuth;
    ConstraintLayout layout;
    FirebaseUser user;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable2sv);

        user = FirebaseAuth.getInstance().getCurrentUser();
        txt_email=findViewById(R.id.txt_email);
        txt_pin=findViewById(R.id.txt_pin);
        txt_repin=findViewById(R.id.txt_repin);
        btn_verify=findViewById(R.id.btn_verify);
        layout=findViewById(R.id.layout);
        progressBar=findViewById(R.id.progressBar);
        mAuth=FirebaseAuth.getInstance();


        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
                progressBar.setVisibility(View.VISIBLE);
            }
        });




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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(Enable2svActivity.this, "E-mail send to your mail box", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Enable2svActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }
}