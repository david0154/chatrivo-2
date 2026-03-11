/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */



package com.teamxdevelopers.SuperChat.activities.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.teamxdevelopers.SuperChat.BuildConfig;
import com.teamxdevelopers.SuperChat.R;
import com.teamxdevelopers.SuperChat.activities.SplashActivity;
import com.teamxdevelopers.SuperChat.utils.IntentUtils;
import com.teamxdevelopers.SuperChat.utils.MyApp;

import org.xml.sax.SAXParseException;

public class AboutFragment extends PreferenceFragmentCompat implements View.OnClickListener {
    private ImageButton emailBtn, websiteBtn, fbBtn,ytBtn;
    private TextView tvAppVersion,tvAbout,tvCompanyName,tvLicensed_company;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);

        initViews(view);

        emailBtn.setOnClickListener(this);
        websiteBtn.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        ytBtn.setOnClickListener(this);

        addValue();

        return view;
    }

    private void addValue() {
        tvAppVersion.setText(BuildConfig.VERSION_NAME);
        tvAbout.setText(getResources().getString(R.string.app_name)+" social media platform");
        tvCompanyName.setText(getResources().getString(R.string.your_company_name));
        
        String LicensedName = tvLicensed_company.getText().toString();

    }


    private void initViews(View view) {

        emailBtn = (ImageButton) view.findViewById(R.id.email_btn);
        websiteBtn = (ImageButton) view.findViewById(R.id.website_btn);
        fbBtn = (ImageButton) view.findViewById(R.id.fb_btn);
        ytBtn=view.findViewById(R.id.yt_btn);
        tvAppVersion=view.findViewById(R.id.tv_app_version);
        tvAbout=view.findViewById(R.id.tv_about);
        tvCompanyName=view.findViewById(R.id.tv_company_name);
        tvLicensed_company=view.findViewById(R.id.tv_licensed_company);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.email_btn:
                intent = IntentUtils.getSendEmailIntent(getActivity());
                break;

            case R.id.website_btn:
                intent = IntentUtils.getOpenWebsiteIntent(MyApp.context().getString(R.string.website));
                break;
        }
        if (intent != null) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}