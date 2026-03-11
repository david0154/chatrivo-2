/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2024 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.model.ExpandableContact;
import com.teamxdevelopers.SuperChat.utils.FireConstants;
import com.teamxdevelopers.SuperChat.utils.NetworkHelper;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;

import java.util.HashMap;
import java.util.Objects;


public class AccountVerificationFragment extends Fragment {

    private Button btnSubmit;
    public AccountVerificationFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_account_verification, container, false);

        btnSubmit = view.findViewById(R.id.btn_submit);

        changeButton();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSubmitAlertDialog();
            }
        });




        return view;
    }

    private void changeButton() {
        if (NetworkHelper.isConnected(getContext())){
            getData();
        }else {
            btnSubmit.setEnabled(false);
            btnSubmit.setText(getString(R.string.no_internet_connection));
        }

    }

    private void getData() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText(getString(R.string.loading));

        FireConstants.usersRef.child(FireManager.getUid()).child("verified").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    boolean bool = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
                    if (bool){
                        changeButtonValue(3);
                    }else {
                        FireConstants.verificationRef.child(FireManager.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    changeButtonValue(1);
                                }else {
                                    changeButtonValue(0);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else {
                    FireConstants.verificationRef.child(FireManager.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                changeButtonValue(1);
                            }else {
                                changeButtonValue(0);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });



    }

    void changeButtonValue( int i){
        if (i == 0){ //submit_a_verification_request
            btnSubmit.setEnabled(true);
            try{
                btnSubmit.setText(getActivity().getString(R.string.submit_a_verification_request));

            }catch (Exception e){

            }

        }else if (i == 1){ //pending
            btnSubmit.setEnabled(false);
            try{
                btnSubmit.setText(getActivity().getString(R.string.pending));
            }catch (Exception e){

            }

        }else { //account_already_verified
            btnSubmit.setEnabled(false);
            try{
                btnSubmit.setText(getActivity().getString(R.string.account_already_verified));

            }catch (Exception e){

            }
        }

    }

    private void showSubmitAlertDialog (){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getString(R.string.submit_a_verification_request));
        alertDialog.setMessage(getString(R.string.please_provide_your_government_issued_id));

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

        alertDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       String text = input.getText().toString();
                        if (text.isEmpty()){
                            Toast.makeText(getContext(),getString(R.string.please_provide_your_government_issued_id), Toast.LENGTH_SHORT).show();
                        }else {
                            submitARequest(input.getText().toString());
                        }
                    }
                });

        alertDialog.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void submitARequest(String string) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getContext().getString(R.string.submiting));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<Object, Object> hashMap = new HashMap<>();

        hashMap.put("gId",string);
        hashMap.put("time",System.currentTimeMillis());

        FireConstants.verificationRef.child(FireManager.getUid()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


}