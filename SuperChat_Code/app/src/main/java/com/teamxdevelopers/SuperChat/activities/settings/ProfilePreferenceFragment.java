/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.settings;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.canhub.cropper.CropImage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.teamxdevelopers.SuperChat.Base;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.ProfilePhotoActivity;
import com.teamxdevelopers.SuperChat.utils.BitmapUtils;
import com.teamxdevelopers.SuperChat.utils.CropImageRequest;
import com.teamxdevelopers.SuperChat.utils.DirManager;
import com.teamxdevelopers.SuperChat.utils.FireConstants;
import com.teamxdevelopers.SuperChat.utils.IntentUtils;
import com.teamxdevelopers.SuperChat.utils.mediastore.MediaStoreUtil;
import com.teamxdevelopers.SuperChat.utils.NetworkHelper;
import com.teamxdevelopers.SuperChat.utils.SharedPreferencesManager;
import com.teamxdevelopers.SuperChat.utils.network.FireManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;


import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;


public class ProfilePreferenceFragment extends PreferenceFragmentCompat implements Base {
    public static final int PICK_IMAGE_REQUEST = 4951;
    private CircleImageView imageViewUserProfile;
    private ImageButton imageButtonChangeUserProfile;
    private ImageButton imageButtonEditUsername;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvPhoneNumber,tvVerifyMyNumber,tvVerifyMyNumberRetry,tvAccLink,tvCopy;
    private CardView cvVerifyApply,cvVerifyProgress,cvVerifyReject,cvVerifyYes;
    private ProgressBar pbVerify;
    private FirebaseAuth mAuth;
    private String uid,isVerify;


    private FireManager fireManager = new FireManager();
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fagment_profile_settings, container, false);

        imageViewUserProfile = view.findViewById(R.id.image_view_user_profile);
        imageButtonChangeUserProfile = view.findViewById(R.id.image_button_change_user_profile);
        tvUsername = view.findViewById(R.id.tv_username);
        imageButtonEditUsername = view.findViewById(R.id.image_button_edit_username);
        tvStatus = view.findViewById(R.id.tv_status);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvCopy=view.findViewById(R.id.tv_copy);
        tvAccLink=view.findViewById(R.id.tv_Acc_link);
        tvVerifyMyNumber=view.findViewById(R.id.txt_verifyMyAccount);
        tvVerifyMyNumberRetry=view.findViewById(R.id.txt_verifyMyAccountRetry);
        cvVerifyApply=view.findViewById(R.id.verifyApply);
        cvVerifyProgress=view.findViewById(R.id.verifyProgress);
        cvVerifyReject=view.findViewById(R.id.verifyReject);
        cvVerifyYes=view.findViewById(R.id.verifyYes);
        pbVerify=view.findViewById(R.id.pbVerify);
        mAuth= FirebaseAuth.getInstance();
        uid=mAuth.getCurrentUser().getUid();




        String userName = SharedPreferencesManager.getUserName();
        String status = SharedPreferencesManager.getStatus();
        String phoneNumber = SharedPreferencesManager.getPhoneNumber();
        final String myPhoto = SharedPreferencesManager.getMyPhoto();
        tvStatus.setText(status);
        tvUsername.setText(userName);
        tvPhoneNumber.setText(phoneNumber);
        tvAccLink.setText("https://"+getResources().getString(R.string.account_link)+"/"+phoneNumber);
        cvVerifyApply.setVisibility(View.GONE);

//        pbVerify.setVisibility(View.VISIBLE);
//        getdata();
        listenToStatusAndName();

        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link=tvAccLink.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(link);
                Toast.makeText(getContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });





//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
//                        .setTitle("Alert")
//                        .setMessage("Do you want logout?")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                if (thisUser != null) {
//                                    // User is signed in
//                                    FirebaseAuth.getInstance().signOut();
//                                    Intent intent=new Intent(getContext(),SplashActivity.class);
//
//                                } else {
//                                    // No user is signed in
//                                    Intent intent = new Intent(getContext(), SplashActivity.class);
//                                    startActivity(intent);
//                                }
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        })
//                        .show();
//            }
//        });


        imageViewUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfilePhotoActivity.class);
                String transName = "profile_photo_trans";

                intent.putExtra(IntentUtils.EXTRA_PROFILE_PATH, myPhoto);
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, transName).toBundle());
            }
        });

        imageButtonChangeUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });

        imageButtonEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(getString(R.string.enter_your_name), new EditTextDialogListener() {
                    @Override
                    public void onOk(final String text) {
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(getActivity(), R.string.username_is_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (NetworkHelper.isConnected(getActivity())) {
                            getDisposables().add(
                                    fireManager.updateMyUserName(text).subscribe(() -> {
                                        SharedPreferencesManager.saveMyUsername(text);
                                        tvUsername.setText(text);
                                    }, throwable -> Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show())
                            );

                        } else {
                            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(getString(R.string.enter_your_status), new EditTextDialogListener() {
                    @Override
                    public void onOk(final String text) {
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(getActivity(), R.string.status_is_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (NetworkHelper.isConnected(getActivity())) {
                            getDisposables().add(
                                    fireManager.updateMyStatus(text).subscribe(() -> {
                                        SharedPreferencesManager.saveMyStatus(text);
                                        tvStatus.setText(text);

                                    }, throwable -> Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show())
                            );


                        } else {
                            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Glide.with(getActivity()).load(new File(myPhoto))
                .into(imageViewUserProfile);

        return view;

    }

    private void listenToStatusAndName(){
        FireConstants.usersRef.child(FireManager.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String newName = snapshot.getValue(String.class);
                        tvUsername.setText(newName);
                        SharedPreferencesManager.saveMyUsername(newName);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FireConstants.usersRef.child(FireManager.getUid()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String newStatus = snapshot.getValue(String.class);
                        tvStatus.setText(newStatus);
                        SharedPreferencesManager.saveMyStatus(newStatus);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void showEditTextDialog(String message, final EditTextDialogListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final EditText edittext = new EditText(getActivity());
        alert.setMessage(message);


        alert.setView(edittext);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


                if (listener != null)
                    listener.onOk(edittext.getText().toString());


            }
        });

        alert.setNegativeButton(R.string.cancel, null);

        alert.show();


    }

    @NotNull
    @Override
    public CompositeDisposable getDisposables() {
        return disposables;
    }


    private interface EditTextDialogListener {
        void onOk(String text);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (resultUri == null) return;


                Bitmap bitmap = MediaStoreUtil.INSTANCE.getBitmapByUri(getActivity(), resultUri);

                if (bitmap == null) {
                    Toast.makeText(getActivity(), "could not get file", Toast.LENGTH_SHORT).show();
                    return;
                }


                final File file = DirManager.generateUserProfileImage();


                BitmapUtils.convertBitmapToJpeg(bitmap, file, 30);

                getDisposables().add(fireManager.updateMyPhoto(file.getPath()).subscribe(tiple -> {
                            Glide.with(getActivity())
                                    .load(file)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(imageViewUserProfile);
                            Toast.makeText(getActivity(), R.string.image_changed, Toast.LENGTH_SHORT).show();
                        }, throwable -> {

                        })
                );


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

